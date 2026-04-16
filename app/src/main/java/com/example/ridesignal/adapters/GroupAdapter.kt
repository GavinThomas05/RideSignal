package com.example.ridesignal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ridesignal.databinding.ItemGroupBinding
import com.example.ridesignal.models.Friend
import com.example.ridesignal.models.Group
import com.google.firebase.firestore.FirebaseFirestore

class GroupAdapter(
    private val groups: List<Group>,
    private val onLeaveGroupClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class GroupViewHolder(val binding: ItemGroupBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.binding.tvGroupName.text = group.groupName
        holder.binding.tvMemberCount.text = "${group.members.size} members"

        // Setup the nested RecyclerView for members
        val memberList = mutableListOf<Friend>()
        val memberAdapter = FriendAdapter(memberList)
        holder.binding.rvGroupMembers.layoutManager =
            LinearLayoutManager(holder.itemView.context)
        holder.binding.rvGroupMembers.adapter = memberAdapter

        // Fetch user data for all member UIDs
        if (group.members.isNotEmpty()) {
            db.collection("users")
                .whereIn("uid", group.members.take(10)) // Firestore limit is 10 for whereIn
                .get()
                .addOnSuccessListener { snapshots ->
                    memberList.clear()
                    val friends = snapshots.toObjects(Friend::class.java)
                    memberList.addAll(friends)
                    memberAdapter.notifyDataSetChanged()
                }
                // Log error to see why it's failing
                .addOnFailureListener { e ->
                    android.util.Log.e("GroupAdapter", "Error fetching members", e)
                }
        }
    }


    override fun getItemCount() = groups.size
}