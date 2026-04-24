package com.example.ridesignal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ridesignal.databinding.ItemGroupInviteBinding
import com.example.ridesignal.models.GroupInvitation

class GroupInviteAdapter(
    private val invites: List<GroupInvitation>,
    private val onAccept: (GroupInvitation) -> Unit,
    private val onDecline: (GroupInvitation) -> Unit
) : RecyclerView.Adapter<GroupInviteAdapter.InviteViewHolder>() {

    class InviteViewHolder(val binding: ItemGroupInviteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val binding = ItemGroupInviteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InviteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val invite = invites[position]

        holder.binding.tvGroupNameDisplay.text = invite.groupName
        holder.binding.tvInvitedByDisplay.text = "Invited by: ${invite.fromName}"

        holder.binding.btnAcceptInvite.setOnClickListener { onAccept(invite) }
        holder.binding.btnDeclineInvite.setOnClickListener { onDecline(invite) }
    }

    override fun getItemCount() = invites.size
}
