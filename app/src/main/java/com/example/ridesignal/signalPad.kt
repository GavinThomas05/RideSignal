package com.example.ridesignal

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ridesignal.adapters.SelectGroupAdapter
import com.example.ridesignal.databinding.ActivitySignalPadBinding
import com.example.ridesignal.databinding.DialogSelectGroupBinding
import com.example.ridesignal.models.Group
import com.example.ridesignal.models.SignalMessage
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class signalPad : AppCompatActivity() {

    private lateinit var binding: ActivitySignalPadBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Active group info
    private var currentGroupId: String? = null
    private var currentGroupName: String? = null

    // Signal listener and timestamp tracking
    private var signalListener: ListenerRegistration? = null
    private var lastProcessedTimestamp: Long = System.currentTimeMillis()

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

                dialogBinding.rvSelectGroup.adapter =
                    SelectGroupAdapter(groups, db) { selectedGroup ->
                        currentGroupId = selectedGroup.groupId
                        currentGroupName = selectedGroup.groupName
                        binding.tvGroupSubtitle.text = currentGroupName

                        // Start listening for signals as soon as a group is selected
                        startListeningForSignals()

                        dialog.dismiss()
                    }
            }

        dialogBinding.btnCancelSelect.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
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
            broadcastSignal("Stop", "#EF4444", "ic_stop")
        }

        // 2. SLOW DOWN
        binding.btnSlowDown.setOnClickListener {
            broadcastSignal("Slow Down", "#F59E0B", "ic_arrow_down")
        }

        // 3. SPEED UP
        binding.btnSpeedUp.setOnClickListener {
            broadcastSignal("Speed Up", "#10B981", "ic_arrow_up")
        }

        // 4. HAZARD
        binding.btnHazard.setOnClickListener {
            broadcastSignal("Hazard\nOn Road", "#EF4444", "ic_hazard")
        }

        // 5. TURN LEFT
        binding.btnTurnLeft.setOnClickListener {
            broadcastSignal("Turn Left", "#F59E0B", "ic_turn_left")
        }

        // 6. TURN RIGHT
        binding.btnTurnRight.setOnClickListener {
            broadcastSignal("Turn Right", "#F59E0B", "ic_turn_right")
        }

        // 7. FUEL STOP
        binding.btnFuelStop.setOnClickListener {
            broadcastSignal("Fuel Stop", "#3B82F6", "ic_fuel_stop")
        }

        // 8. REFRESH BREAK
        binding.btnRefresh.setOnClickListener {
            broadcastSignal("Refreshment\nBreak", "#3B82F6", "ic_refresh")
        }

        // 9. POLICE AHEAD
        binding.btnPolice.setOnClickListener {
            broadcastSignal("Police\nAhead", "#EF4444", "ic_police")
        }

        // 10. INDICATOR ON
        binding.btnIndicator.setOnClickListener {
            broadcastSignal("Indicator\nStill On", "#F59E0B", "ic_bulb")
        }

        // 11. FOLLOW ME
        binding.btnFollowMe.setOnClickListener {
            broadcastSignal("Follow Me", "#10B981", "ic_follow_me")
        }

        // 12. LEAVING GROUP
        binding.btnLeaveGroup.setOnClickListener {
            broadcastSignal("Leaving Group", "#8B5CF6", "ic_split")
        }
    }

    private fun showSignalPopup(signalName: String, sender: String, colorHex: String, iconRes: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_signal_popup, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val borderCard = dialogView.findViewById<MaterialCardView>(R.id.popupBorderCard)
        val iconContainer = dialogView.findViewById<MaterialCardView>(R.id.iconContainer)
        val timerBar = dialogView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.signalTimerBar)
        val tvSignal = dialogView.findViewById<TextView>(R.id.tvSignalName)
        val tvSender = dialogView.findViewById<TextView>(R.id.tvSenderName)
        val ivIcon = dialogView.findViewById<ImageView>(R.id.popupIcon)

        val color = Color.parseColor(colorHex)
        borderCard.strokeColor = color
        iconContainer.setCardBackgroundColor(color)
        timerBar.setIndicatorColor(color)

        tvSignal.text = signalName
        tvSender.text = sender
        ivIcon.setImageResource(iconRes)

        dialog.show()

        ObjectAnimator.ofInt(timerBar, "progress", 100, 0).apply {
            duration = 8000
            interpolator = LinearInterpolator()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (dialog.isShowing) dialog.dismiss()
                }
            })
            start()
        }
    }

    private fun startListeningForSignals() {
        val groupId = currentGroupId ?: return
        val currentUserId = auth.currentUser?.uid ?: ""

        signalListener?.remove()

        signalListener = db.collection("groups").document(groupId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    val signal = snapshot.get("lastSignal", SignalMessage::class.java)

                    if (signal != null) {
                        // Logic: Only show if signal is newer than when we opened the pad AND not from us
                        if (signal.timestamp > lastProcessedTimestamp && signal.senderId != currentUserId) {

                            lastProcessedTimestamp = signal.timestamp // to prevent re-triggering

                            val iconRes = resources.getIdentifier(signal.iconName, "drawable", packageName)
                            showSignalPopup(
                                signal.signalType,
                                signal.senderName,
                                signal.colorHex,
                                if (iconRes != 0) iconRes else R.drawable.ic_placeholder
                            )
                        }
                    }
                }
            }
    }

    private fun broadcastSignal(signalName: String, colorHex: String, iconName: String) {
        val groupId = currentGroupId ?: return
        val uid = auth.currentUser?.uid ?: return

        // Fetch User details from Firestore 'users' collection
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val firstName = userDoc.getString("firstName") ?: ""
                val lastName = userDoc.getString("lastName") ?: ""
                val fullName = "$firstName $lastName".trim()
                val senderName = fullName.ifEmpty { "Rider" }

                val signal = SignalMessage(
                    senderId = uid,
                    senderName = senderName,
                    signalType = signalName,
                    colorHex = colorHex,
                    iconName = iconName,
                    timestamp = System.currentTimeMillis()
                )

                db.collection("groups").document(groupId)
                    .update("lastSignal", signal)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Signal sent: ${signalName.replace("\n", " ")}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { Log.e("SignalPad", "Failed to send signal", it) }
            }
            .addOnFailureListener { Log.e("SignalPad", "Failed to fetch user data", it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        signalListener?.remove()
    }
}