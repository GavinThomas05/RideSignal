package com.example.ridesignal.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ridesignal.R
import com.example.ridesignal.models.Friend
import com.example.ridesignal.models.Group
import com.google.firebase.firestore.FirebaseFirestore

class SelectGroupAdapter(
    private val list: List<Group>,
    private val db: FirebaseFirestore,
    private val onClick: (Group) -> Unit
) : RecyclerView.Adapter<SelectGroupAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvGroupName)
        val memberCount: TextView = view.findViewById(R.id.tvMemberCount)
        val btnSelectGroup: TextView = view.findViewById(R.id.btnSelectGroup)
        val rvMembers: RecyclerView = view.findViewById(R.id.rvGroupMembers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_group, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val group = list[position]
        holder.name.text = group.groupName
        holder.memberCount.text = "${group.members.size} members"

        val memberList = mutableListOf<Friend>()
        val memberAdapter = FriendAdapter(memberList)

        // Setup vertical layout
        holder.rvMembers.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvMembers.adapter = memberAdapter

        if (group.members.isNotEmpty()) {
            val idsToFetch = group.members.take(10)

            db.collection("users")
                .whereIn("uid", idsToFetch)
                .get()
                .addOnSuccessListener { snapshots ->
                    val friends = mutableListOf<Friend>()
                    for (doc in snapshots.documents) {
                        val friend = doc.toObject(Friend::class.java)
                        if (friend != null) friends.add(friend)
                    }
                    memberList.clear()
                    memberList.addAll(friends)
                    memberAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("SelectGroupAdapter", "Error fetching members", e)
                }
        }

        holder.btnSelectGroup.setOnClickListener { onClick(group) }
    }

    override fun getItemCount() = list.size
}