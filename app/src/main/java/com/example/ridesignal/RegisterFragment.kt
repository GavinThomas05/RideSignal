import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ridesignal.MainActivity
import com.example.ridesignal.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    //initialize firebase auth
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Register Button Logic
        binding.btnRegister.setOnClickListener {
            val email = binding.etRegEmail.text.toString().trim()
            val password = binding.etRegPassword.text.toString().trim()
            val confirmPassword = binding.etRegConfirmPassword.text.toString().trim()

            // Basic Validation of fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create User in Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid

                    // Save User Data to Firestore
                    val userMap = hashMapOf(
                        "uid" to userId,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    userId?.let {
                        db.collection("users").document(it)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_SHORT).show()
                                // Go to Main App
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                                activity?.finish()
                            }
                    }
                }
                // Handle Errors
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }        }

        // Link to Login Fragment
        binding.tvBackToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}