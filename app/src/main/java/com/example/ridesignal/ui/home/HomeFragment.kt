package com.example.ridesignal.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ridesignal.LoginActivity
import com.example.ridesignal.databinding.FragmentHomeBinding
import com.example.ridesignal.R
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // Initialize Firebase Auth
    private val auth = FirebaseAuth.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    // This is the function sets up the listeners for the buttons
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handles opening the 3-dot Options Menu
        binding.btnHomeOptions.setOnClickListener { btnView ->
            showOptionsMenu(btnView)
        }

        //ADD LISTENERS FOR HOME SCREEN BTNS HERE
    }

    //pop up menu logic
    private fun showOptionsMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.home_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_sign_out -> {
                    // 1. Log out of Firebase
                    auth.signOut()

                    // 2. Give user feedback
                    Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()

                    // 3. Move back to LoginActivity
                    val intent = Intent(requireContext(), LoginActivity::class.java)

                    // IMPORTANT: Clear the activity stack so the user can't click "Back" to get back in
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

