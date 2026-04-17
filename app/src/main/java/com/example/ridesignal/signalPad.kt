package com.example.ridesignal

import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ridesignal.databinding.ActivitySignalPadBinding

class signalPad : AppCompatActivity() {

    private lateinit var binding: ActivitySignalPadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignalPadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // When the 3-dot button is clicked
        binding.btnOptions.setOnClickListener {
            showMenu()
        }
    }

    // Pop up menu logic
    private fun showMenu() {
        val popup = PopupMenu(this, binding.btnOptions)

        popup.menuInflater.inflate(R.menu.signal_pad_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_exit -> {
                    // Close the activity and go back to main
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}