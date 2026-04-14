package com.example.ridesignal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ridesignal.databinding.ItemFriendRequestBinding
import com.example.ridesignal.models.FriendRequest

// Renamed to FriendRequestAdapter to match your FriendsFragment logic
class FriendRequestAdapter(
    private val requests: List<FriendRequest>,
    private val onAccept: (FriendRequest) -> Unit,
    private val onDecline: (FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder>() {

    // Inner class to hold the ViewBinding for the item layout
    class RequestViewHolder(val binding: ItemFriendRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        // Inflate the item_friend_request.xml layout
        val binding = ItemFriendRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]

        // Ensure these IDs match your item_friend_request.xml
        holder.binding.tvFriendName.text = request.fromName

        holder.binding.btnAccept.setOnClickListener {
            onAccept(request)
        }

        holder.binding.btnDecline.setOnClickListener {
            onDecline(request)
        }
    }

    override fun getItemCount() = requests.size
}