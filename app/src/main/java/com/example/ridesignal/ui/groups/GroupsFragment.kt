package com.example.ridesignal.ui.groups

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ridesignal.R
import com.example.ridesignal.adapters.GroupAdapter
import com.example.ridesignal.adapters.GroupInviteAdapter
import com.example.ridesignal.adapters.InviteFriendsAdapter
import com.example.ridesignal.databinding.DialogCreateGroupBinding
import com.example.ridesignal.databinding.FragmentGroupsBinding
import com.example.ridesignal.models.Friend
import com.example.ridesignal.models.Group
import com.example.ridesignal.models.GroupInvitation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GroupsFragment : Fragment(R.layout.fragment_groups) {

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val groupsList = mutableListOf<Group>()
    private lateinit var groupAdapter: GroupAdapter

    private val inviteList = mutableListOf<GroupInvitation>()
    private lateinit var inviteAdapter: GroupInviteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGroupsBinding.bind(view)

        setupRecyclerView()
        fetchGroups()
        fetchInvites()

        binding.btnCreateGroup.setOnClickListener {
            showCreateGroupDialog()
        }
    }

    private fun showCreateGroupDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogCreateGroupBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        val currentUserId = auth.currentUser?.uid ?: return
        var selectedUids = listOf<String>()

        // Fetch friends to show in the list
        db.collection("users").document(currentUserId).collection("friends").get()
            .addOnSuccessListener { snapshot ->
                val friends = snapshot.toObjects(Friend::class.java)
                val adapter = InviteFriendsAdapter(friends) { updatedList ->
                    selectedUids = updatedList
                }
                dialogBinding.rvInviteFriends.layoutManager = LinearLayoutManager(requireContext())
                dialogBinding.rvInviteFriends.adapter = adapter
            }

        //  Handle the "Create" button click
        dialogBinding.btnConfirmCreate.setOnClickListener {
            val groupName = dialogBinding.etGroupName.text.toString().trim()

            if (groupName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // DISABLING the button to prevent double-clicks which can cause crashes
            dialogBinding.btnConfirmCreate.isEnabled = false

            // Fetch our profile to get the 'fromName' for the invites
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { userDoc ->
                    // SAFE CHECK: Ensure fields exist to prevent null pointer crash
                    val firstName = userDoc.getString("firstName") ?: "User"
                    val lastName = userDoc.getString("lastName") ?: ""
                    val myName = "$firstName $lastName".trim()

                    val groupId = db.collection("groups").document().id
                    val batch = db.batch()

                    // 1. Create the main Group document
                    val newGroup = Group(
                        groupId = groupId,
                        groupName = groupName,
                        adminUid = currentUserId,
                        members = listOf(currentUserId) // Only the creator starts in the group
                    )
                    batch.set(db.collection("groups").document(groupId), newGroup)

                    // 2. Create invitations for every selected friend
                    selectedUids.forEach { friendUid ->
                        val inviteRef = db.collection("users").document(friendUid)
                            .collection("group_invitations").document(groupId)

                        val invitation = mapOf(
                            "groupId" to groupId,
                            "groupName" to groupName,
                            "fromName" to myName,
                            "fromUid" to currentUserId,
                            "timestamp" to System.currentTimeMillis()
                        )
                        batch.set(inviteRef, invitation)
                    }

                    // 3. Commit all changes at once
                    batch.commit()
                        .addOnSuccessListener {
                            dialog.dismiss()
                            Toast.makeText(requireContext(), "Group created and invites sent!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            dialogBinding.btnConfirmCreate.isEnabled = true
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    dialogBinding.btnConfirmCreate.isEnabled = true
                    Toast.makeText(requireContext(), "Could not fetch user profile", Toast.LENGTH_SHORT).show()
                }
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    private fun setupRecyclerView() {
        // Group List setup
        groupAdapter = GroupAdapter(groupsList) { group ->
            leaveGroup(group)
        }
        binding.rvGroupsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGroupsList.adapter = groupAdapter

        // Pending Invites List setup
        inviteAdapter = GroupInviteAdapter(inviteList,
            onAccept = { invite -> acceptGroupInvite(invite) },
            onDecline = { invite -> declineGroupInvite(invite) }
        )
        binding.rvPendingInvites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPendingInvites.adapter = inviteAdapter
    }

    private fun fetchInvites() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("group_invitations")
            .addSnapshotListener { value, _ ->
                val binding = _binding ?: return@addSnapshotListener

                inviteList.clear()
                value?.forEach { doc ->
                    inviteList.add(GroupInvitation(
                        groupId = doc.getString("groupId") ?: "",
                        groupName = doc.getString("groupName") ?: "",
                        fromName = doc.getString("fromName") ?: "Unknown"
                    ))
                }
                inviteAdapter.notifyDataSetChanged()

                // Toggle visibility of the "No Pending Invites" text
                if (inviteList.isEmpty()) {
                    binding.tvNoPendingInvites.visibility = View.VISIBLE
                    binding.rvPendingInvites.visibility = View.GONE
                } else {
                    binding.tvNoPendingInvites.visibility = View.GONE
                    binding.rvPendingInvites.visibility = View.VISIBLE
                }
            }
    }

    private fun acceptGroupInvite(invite: GroupInvitation) {
        val uid = auth.currentUser?.uid ?: return
        val batch = db.batch()

        // 1. Add current user to the group's 'members' array
        val groupRef = db.collection("groups").document(invite.groupId)
        batch.update(groupRef, "members", FieldValue.arrayUnion(uid))

        // 2. Delete the invitation from user's sub-collection
        val inviteRef = db.collection("users").document(uid)
            .collection("group_invitations").document(invite.groupId)
        batch.delete(inviteRef)

        batch.commit().addOnSuccessListener {
            if (isAdded) {
                Toast.makeText(requireContext(), "Joined ${invite.groupName}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun declineGroupInvite(invite: GroupInvitation) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("group_invitations").document(invite.groupId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Invitation declined", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchGroups() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("groups")
            .whereArrayContains("members", currentUserId)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val binding = _binding ?: return@addSnapshotListener

                groupsList.clear()
                value?.forEach { doc ->
                    val group = doc.toObject(Group::class.java)
                    groupsList.add(group)
                }
                groupAdapter.notifyDataSetChanged()

                if (groupsList.isEmpty()) {
                    binding.tvNoGroups.visibility = View.VISIBLE
                    binding.rvGroupsList.visibility = View.GONE
                } else {
                    binding.tvNoGroups.visibility = View.GONE
                    binding.rvGroupsList.visibility = View.VISIBLE
                }
            }
    }

    private fun leaveGroup(group: Group) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("groups").document(group.groupId)
            .update("members", FieldValue.arrayRemove(currentUserId))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Left ${group.groupName}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error leaving group", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}