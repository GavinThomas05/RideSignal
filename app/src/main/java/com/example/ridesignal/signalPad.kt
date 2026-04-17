package com.example.ridesignal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ridesignal.databinding.ActivitySignalPadBinding
import com.example.ridesignal.databinding.DialogSelectGroupBinding
import com.example.ridesignal.models.Group // <--- CORRECT IMPORT
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class signalPad : AppCompatActivity() {

    private lateinit var binding: ActivitySignalPadBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Active group info
    private var currentGroupId: String? = null
    private var currentGroupName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignalPadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showGroupSelectionDialog()

        binding.btnOptions.setOnClickListener {
            showMenu()
        }
    }

    private fun showMenu() {
        val popup = PopupMenu(this, binding.btnOptions)
        popup.menuInflater.inflate(R.menu.signal_pad_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_exit) {
                finish()
                true
            } else false
        }
        popup.show()
    }

    private fun showGroupSelectionDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogSelectGroupBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)

        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("groups")
            .whereArrayContains("members", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                // Ensure this uses your models.Group
                val groups = snapshot.toObjects(Group::class.java)

                if (groups.isEmpty()) {
                    Toast.makeText(this, "Join a group first!", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                dialogBinding.rvSelectGroup.layoutManager = LinearLayoutManager(this)
                dialogBinding.rvSelectGroup.adapter = SelectGroupAdapter(groups) { selectedGroup ->
                    // FIX: matched variable name to currentGroupId
                    currentGroupId = selectedGroup.groupId
                    currentGroupName = selectedGroup.groupName
                    binding.tvSignalTitle.text = "Signal Pad: $currentGroupName"
                    dialog.dismiss()
                }
            }

        dialogBinding.btnCancelSelect.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    inner class SelectGroupAdapter(
        private val list: List<Group>, // FIX: Use models.Group
        private val onClick: (Group) -> Unit
    ) : RecyclerView.Adapter<SelectGroupAdapter.VH>() {

        // find IDs for UI components
        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.tvGroupName)
            val memberCount: TextView = view.findViewById(R.id.tvMemberCount)
            val btnSelectGroup: TextView = view.findViewById(R.id.btnSelectGroup)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_select_group, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val group = list[position]
            val count = group.members.size
            holder.name.text = group.groupName

            //members count
            holder.memberCount.text = "$count members"

            // Button click listener for the select button
            holder.btnSelectGroup.setOnClickListener { onClick(group) }
        }

        override fun getItemCount() = list.size
    }
}