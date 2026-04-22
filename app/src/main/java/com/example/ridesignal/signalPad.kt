package com.example.ridesignal

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.dismiss
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ridesignal.adapters.FriendAdapter
import com.example.ridesignal.adapters.SelectGroupAdapter
import com.example.ridesignal.databinding.ActivitySignalPadBinding
import com.example.ridesignal.databinding.DialogSelectGroupBinding
import com.example.ridesignal.models.Friend
import com.example.ridesignal.models.Group
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView


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
        setupSignalButtons()

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

    private fun setupSignalButtons() {
        // 1. STOP
        binding.btnStop.setOnClickListener {
            showSignalPopup("Stop", "Mike Johnson", "#EF4444", R.drawable.ic_stop)
        }

        // 2. SLOW DOWN
        binding.btnSlowDown.setOnClickListener {
            showSignalPopup("Slow Down", "Mike Johnson", "#F59E0B", R.drawable.ic_arrow_down)
        }

        // 3. SPEED UP
        binding.btnSpeedUp.setOnClickListener {
            showSignalPopup("Speed Up", "Mike Johnson", "#10B981", R.drawable.ic_arrow_up)
        }

        // 4. HAZARD
        binding.btnHazard.setOnClickListener {
            showSignalPopup("Hazard\nOn Road", "Mike Johnson", "#EF4444", R.drawable.ic_hazard)
        }

        // 5. TURN LEFT
        binding.btnHazard.setOnClickListener {
            showSignalPopup("Turn Left", "Mike Johnson", "#EF4444", R.drawable.ic_turn_left)
        }
        // 6. TURN RIGHT
        binding.btnHazard.setOnClickListener {
            showSignalPopup("Turn Right", "Mike Johnson", "#EF4444", R.drawable.ic_turn_right)
        }
        // 7. FUEL STOP
        binding.btnHazard.setOnClickListener {
            showSignalPopup("Fuel Stop", "Mike Johnson", "#EF4444", R.drawable.ic_fuel_stop)
        }
        // 8. REFRESH BREAK
        binding.btnHazard.setOnClickListener {
            showSignalPopup("Refreshment\n Break", "Mike Johnson", "#EF4444", R.drawable.ic_refresh)
        }
        // 9. POLICE AHEAD
        binding.btnHazard.setOnClickListener {
            showSignalPopup("Police\n Ahead", "Mike Johnson", "#EF4444", R.drawable.ic_police)
        }
        // 10. INDICATOR ON
        binding.btnHazard.setOnClickListener {
            showSignalPopup("Indicator\n Still On", "Mike Johnson", "#EF4444", R.drawable.ic_bulb)
        }
        // 11. FOLLOW ME
        binding.btnHazard.setOnClickListener {
            showSignalPopup("Follow Me", "Mike Johnson", "#EF4444", R.drawable.ic_follow_me)
        }
        // 12. LEAVING GROUP
        binding.btnHazard.setOnClickListener {
            showSignalPopup("Leaving Group", "Mike Johnson", "#EF4444", R.drawable.ic_split)
        }

    }

    // Show the group selection dialog
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

    // Show the signal message popup
    private fun showSignalPopup(signalName: String, sender: String, colorHex: String, iconRes: Int) {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_signal_popup, null)

        // Create the Dialog with a transparent background theme
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Find the views inside the popup
        val borderCard = dialogView.findViewById<MaterialCardView>(R.id.popupBorderCard)
        val iconContainer = dialogView.findViewById<MaterialCardView>(R.id.iconContainer)
        val timerBar = dialogView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.signalTimerBar)
        val tvSignal = dialogView.findViewById<TextView>(R.id.tvSignalName)
        val tvSender = dialogView.findViewById<TextView>(R.id.tvSenderName)
        val ivIcon = dialogView.findViewById<ImageView>(R.id.popupIcon)

        // Apply the colors dynamically
        val color = Color.parseColor(colorHex)
        borderCard.strokeColor = color
        iconContainer.setCardBackgroundColor(color)
        timerBar.setIndicatorColor(color)

        // Set the text and icon
        tvSignal.text = signalName
        tvSender.text = sender
        ivIcon.setImageResource(iconRes)

        dialog.show()

        // ANIMATION: Shrink the progress bar from 100 to 0 over 8 seconds
        ObjectAnimator.ofInt(timerBar, "progress", 100, 0).apply {
            duration = 8000 // 8 seconds display time
            interpolator = LinearInterpolator()

            // When the timer hits 0, close the dialog
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
            })
            start()
        }
    }

}