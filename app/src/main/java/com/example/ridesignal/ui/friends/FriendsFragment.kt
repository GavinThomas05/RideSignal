package com.example.ridesignal.ui.friends

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ridesignal.R
import com.example.ridesignal.adapters.FriendAdapter
import com.example.ridesignal.adapters.FriendRequestAdapter
import com.example.ridesignal.databinding.FragmentFriendsBinding
import com.example.ridesignal.models.Friend
import com.example.ridesignal.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment(R.layout.fragment_friends) {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 1. DATA LISTS
    private val friendsList = mutableListOf<Friend>()
    private val requestsList = mutableListOf<FriendRequest>()

    // 2. ADAPTERS (Naming them specifically to avoid confusion)
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var requestAdapter: FriendRequestAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFriendsBinding.bind(view)

        // Initialize UI components
        setupRecyclerViews()

        // Fetch Data
        fetchFriends()
        fetchRequests()

        // 3. Logic for the new "Confirm Add" button in the header
        binding.btnConfirmAdd.setOnClickListener {
            val code = binding.etFriendCode.text.toString().trim().uppercase()
            if (code.length == 6) {
                sendFriendRequest(code)
                binding.etFriendCode.text?.clear()
            } else {
                Toast.makeText(context, "Please enter a 6-character code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerViews() {
        // Setup My Friends List
        friendAdapter = FriendAdapter(friendsList)
        binding.rvFriendsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriendsList.adapter = friendAdapter

        // Setup Pending Requests List
        requestAdapter = FriendRequestAdapter(requestsList,
            onAccept = { request -> acceptFriendRequest(request) },
            onDecline = { request -> declineFriendRequest(request) }
        )
        binding.rvPendingRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPendingRequests.adapter = requestAdapter
    }

    private fun sendFriendRequest(code: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // 1. Find user with this code
        db.collection("users").whereEqualTo("friendCode", code).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val targetUserDoc = documents.documents[0]
                val targetUid = targetUserDoc.id

                if (targetUid == currentUserId) {
                    Toast.makeText(context, "You cannot add yourself", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 2. Get current user's info to send
                db.collection("users").document(currentUserId).get().addOnSuccessListener { myDoc ->
                    val request = FriendRequest(
                        fromUid = currentUserId,
                        fromName = "${myDoc.getString("firstName")} ${myDoc.getString("lastName")}",
                        fromFriendCode = myDoc.getString("friendCode") ?: ""
                    )

                    // 3. Add to target user's "requests" sub-collection
                    db.collection("users").document(targetUid)
                        .collection("requests").document(currentUserId).set(request)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error searching for user", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchFriends() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId).collection("friends")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                friendsList.clear()
                for (doc in value!!) {
                    val friend = doc.toObject(Friend::class.java)
                    friendsList.add(friend)
                }
                friendAdapter.notifyDataSetChanged()

                binding.tvNoFriends.visibility = if (friendsList.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun fetchRequests() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("requests")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                requestsList.clear()
                value?.forEach { doc ->
                    val request = doc.toObject(FriendRequest::class.java).copy(requestId = doc.id)
                    requestsList.add(request)
                }
                requestAdapter.notifyDataSetChanged()

                binding.tvNoRequests.visibility = if (requestsList.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun acceptFriendRequest(request: FriendRequest) {
        val myUid = auth.currentUser?.uid ?: return

        // 1. Data for both users
        val friendForMe = Friend(uid = request.fromUid, firstName = request.fromName, friendCode = request.fromFriendCode)

        db.collection("users").document(myUid).get().addOnSuccessListener { myDoc ->
            val meAsFriend = Friend(
                uid = myUid,
                firstName = "${myDoc.getString("firstName")} ${myDoc.getString("lastName")}",
                friendCode = myDoc.getString("friendCode") ?: ""
            )

            val batch = db.batch()

            // Add to my friends
            val myFriendsRef = db.collection("users").document(myUid).collection("friends").document(request.fromUid)
            batch.set(myFriendsRef, friendForMe)

            // Add to their friends
            val theirFriendsRef = db.collection("users").document(request.fromUid).collection("friends").document(myUid)
            batch.set(theirFriendsRef, meAsFriend)

            // Delete the request
            val requestRef = db.collection("users").document(myUid).collection("requests").document(request.fromUid)
            batch.delete(requestRef)

            batch.commit().addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Friend Added!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun declineFriendRequest(request: FriendRequest) {
        val myUid = auth.currentUser?.uid ?: return
        db.collection("users").document(myUid).collection("requests").document(request.fromUid).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}