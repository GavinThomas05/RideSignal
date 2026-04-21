package com.example.ridesignal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ridesignal.adapters.FriendAdapter
import com.example.ridesignal.adapters.SelectGroupAdapter
import com.example.ridesignal.databinding.ActivitySignalPadBinding
import com.example.ridesignal.databinding.DialogSelectGroupBinding
import com.example.ridesignal.models.Friend
import com.example.ridesignal.models.Group
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

        // Show the selection dialog immediately on launch
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

        dialogBinding.rvSelectGroup.layoutManager = LinearLayoutManager(this)


        db.collection("groups")
            .whereArrayContains("members", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                val groups = snapshot.toObjects(Group::class.java)

                if (groups.isEmpty()) {
                    Toast.makeText(this, "Join a group first!", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                // In signalPad.kt, update this line:
                dialogBinding.rvSelectGroup.adapter =
                    SelectGroupAdapter(groups, db) { selectedGroup ->
                        currentGroupId = selectedGroup.groupId
                        currentGroupName = selectedGroup.groupName
                        binding.tvGroupSubtitle.text = "$currentGroupName"
                        dialog.dismiss()
                    }
            }

        dialogBinding.btnCancelSelect.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }


}