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
import com.example.ridesignal.signalPad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.io.path.exists



class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // Initialize Firebase Auth
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()



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

        fetchUserData()


        // Handles opening the 3-dot Options Menu
        binding.btnHomeOptions.setOnClickListener { btnView ->
            showOptionsMenu(btnView)
        }

        // Handles starting the signal pad
        binding.cardLaunch.setOnClickListener {
            val intent = Intent(requireContext(), signalPad::class.java)
            startActivity(intent)
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

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Reference to the specific user's document
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get the firstName field we saved during registration
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val friendCode = document.getString("friendCode")



                        // Update the UI
                        if (!firstName.isNullOrEmpty()) {
                            binding.tvUserGreeting.text = "Welcome $firstName $lastName ($friendCode)!"
                        } else {
                            binding.tvUserGreeting.text = "Welcome!"
                        }
                    }
                }
                .addOnFailureListener {
                    // Fallback if network fails
                    binding.tvUserGreeting.text = "Welcome!"
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

