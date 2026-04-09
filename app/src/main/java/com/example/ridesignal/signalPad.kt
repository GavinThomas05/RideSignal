package com.example.ridesignal

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.ridesignal.databinding.ActivitySignalPadBinding

class signalPad : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySignalPadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignalPadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOptions.setOnClickListener {
            // This is where you will trigger the "Leave Session" menu later
        }
    }
}