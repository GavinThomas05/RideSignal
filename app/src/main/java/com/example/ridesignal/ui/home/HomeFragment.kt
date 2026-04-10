package com.example.ridesignal.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ridesignal.databinding.FragmentHomeBinding
import com.example.ridesignal.R
//import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

        // Handles the 3-dot Options Menu
        binding.btnHomeOptions.setOnClickListener { btnView ->
            showOptionsMenu(btnView)
        }

        //Add listeners for remaining buttons here
    }

    private fun showOptionsMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.home_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                //sign out from firebase
                R.id.action_sign_out -> {
                    // add sign out logic here
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

