package com.example.ridesignal.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ridesignal.R
import com.example.ridesignal.adapters.FriendAdapter
import com.example.ridesignal.databinding.FragmentFriendsBinding
import com.example.ridesignal.models.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FriendsFragment : Fragment(R.layout.fragment_friends) {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val friendsList = mutableListOf<Friend>()
    private lateinit var adapter: FriendAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFriendsBinding.bind(view)

        setupRecyclerView()
        fetchFriends()
    }

    private fun setupRecyclerView() {
        adapter = FriendAdapter(friendsList)
        binding.rvFriendsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriendsList.adapter = adapter
    }

    private fun fetchFriends() {
        val currentUserId = auth.currentUser?.uid ?: return

        // We fetch from: users -> {myUid} -> friends
        db.collection("users").document(currentUserId).collection("friends")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                friendsList.clear()
                for (doc in value!!) {
                    val friend = doc.toObject(Friend::class.java)
                    friendsList.add(friend)
                }
                adapter.notifyDataSetChanged()

                // Show/Hide "No friends yet" text if you have one
                if (friendsList.isEmpty()) {
                    binding.tvNoFriends.visibility = View.VISIBLE
                } else {
                    binding.tvNoFriends.visibility = View.GONE
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}