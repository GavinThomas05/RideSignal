package com.example.ridesignal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ridesignal.databinding.ItemFriendBinding // You'll need this layoutimport com.example.ridesignal.models.Friend
import com.example.ridesignal.models.Friend

class FriendAdapter(private val friends: List<Friend>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    class FriendViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.binding.tvFriendName.text = "${friend.firstName} ${friend.lastName}"
        holder.binding.tvFriendCode.text = "Code: ${friend.friendCode}"
    }

    override fun getItemCount() = friends.size
}