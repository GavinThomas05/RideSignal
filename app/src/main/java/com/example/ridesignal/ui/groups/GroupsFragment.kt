package com.example.ridesignal.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.ridesignal.R
import com.example.ridesignal.databinding.FragmentGroupsBinding
import com.example.ridesignal.databinding.DialogCreateGroupBinding
import com.example.ridesignal.models.Friend
import com.example.ridesignal.models.Group
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ridesignal.adapters.InviteFriendsAdapter

class GroupsFragment : Fragment(R.layout.fragment_groups) {

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGroupsBinding.bind(view) // Wait, use FragmentGroupsBinding.bind(view)

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

        // 1. Fetch friends to show in the list
        db.collection("users").document(currentUserId).collection("friends").get()
            .addOnSuccessListener { snapshot ->
                val friends = snapshot.toObjects(Friend::class.java)

                val adapter = InviteFriendsAdapter(friends) { updatedList ->
                    selectedUids = updatedList
                }

                dialogBinding.rvInviteFriends.layoutManager = LinearLayoutManager(context)
                dialogBinding.rvInviteFriends.adapter = adapter

            }

        // Handle the "Create" button click
        dialogBinding.btnConfirmCreate.setOnClickListener {
            val groupName = dialogBinding.etGroupName.text.toString().trim()
            if (groupName.isEmpty()) {
                Toast.makeText(context, "Enter a group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val groupId = db.collection("groups").document().id
            val members = selectedUids + currentUserId // Admin is always a member

            val newGroup = Group(
                groupId = groupId,
                groupName = groupName,
                adminUid = currentUserId,
                members = members
            )

            // Save group to the 'groups' collection
            db.collection("groups").document(groupId).set(newGroup)
                .addOnSuccessListener {
                    dialog.dismiss()
                    Toast.makeText(context, "Group Created!", Toast.LENGTH_SHORT).show()
                }
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}