package com.example.ridesignal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ridesignal.databinding.ItemInviteFriendCheckboxBinding
import com.example.ridesignal.models.Friend

class InviteFriendsAdapter(
    private val friends: List<Friend>,
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<InviteFriendsAdapter.InviteViewHolder>() {

    private val selectedUids = mutableSetOf<String>()

    class InviteViewHolder(val binding: ItemInviteFriendCheckboxBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val binding = ItemInviteFriendCheckboxBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InviteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val friend = friends[position]
        holder.binding.tvFriendName.text = friend.firstName
        holder.binding.tvFriendAvatar.text = friend.firstName.take(1).uppercase()

        // Handle checkbox state
        holder.binding.cbInvite.setOnCheckedChangeListener(null) // Prevent recursive calls
        holder.binding.cbInvite.isChecked = selectedUids.contains(friend.uid)

        holder.binding.cbInvite.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedUids.add(friend.uid)
            } else {
                selectedUids.remove(friend.uid)
            }
            onSelectionChanged(selectedUids.toList())
        }
    }

    override fun getItemCount() = friends.size
}