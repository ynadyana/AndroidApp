package com.quizapp.tork

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var logoutButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        nameEditText = view.findViewById(R.id.name)
        emailEditText = view.findViewById(R.id.email)
        passwordEditText = view.findViewById(R.id.psw)
        updateButton = view.findViewById(R.id.Update)
        deleteAccountButton = view.findViewById(R.id.delete_account_button)
        logoutButton = view.findViewById(R.id.logout_button)

        // Load user data
        loadUserData()

        // Set update button click listener
        updateButton.setOnClickListener {
            updateProfile()
        }

        // Set delete account button click listener
        deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        // Set logout button click listener
        logoutButton.setOnClickListener {
            logout()
        }

        return view
    }

    private fun logout() {
        // Create an alert dialog to confirm logout
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to log out?")
        builder.setPositiveButton("Yes") { _, _ ->
            FirebaseAuth.getInstance().signOut() // Logs out the user
            val intent = Intent(requireContext(), LoginScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear activity stack
            startActivity(intent)
            requireActivity().finish() // Close the current activity
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Dismiss the dialog and stay on the same page
        }
        builder.show()
    }

    private fun showDeleteAccountConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                promptForPasswordToDelete()
            }
            .setNegativeButton("No", null)
        builder.show()
    }

    private fun promptForPasswordToDelete() {
        val passwordInput = EditText(requireContext())
        passwordInput.hint = "Enter your password"
        passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Enter Password to Confirm")
            .setView(passwordInput)
            .setPositiveButton("Delete") { _, _ ->
                val password = passwordInput.text.toString()
                if (password.isNotEmpty()) {
                    reauthenticateAndDeleteAccount(password)
                } else {
                    Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun reauthenticateAndDeleteAccount(password: String) {
        val user = auth.currentUser
        user?.let {
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    firestore.collection("users").document(user.uid).delete()
                        .addOnSuccessListener {
                            user.delete()
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(requireContext(), LoginScreen::class.java)
                                    startActivity(intent)
                                    requireActivity().finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to delete account: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to delete user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Re-authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("name") ?: ""
                        val email = documentSnapshot.getString("email") ?: ""
                        val password = documentSnapshot.getString("password") ?: ""

                        nameEditText.setText(name)
                        emailEditText.setText(email)
                        passwordEditText.setText(password)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfile() {
        val user = auth.currentUser
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedFields = mutableMapOf<String, Any>()
        updatedFields["name"] = name
        updatedFields["email"] = email
        updatedFields["password"] = password

        user?.let {
            firestore.collection("users").document(it.uid).update(updatedFields)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            if (it.email != email) {
                it.updateEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Email updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error updating email: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            it.updatePassword(password)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error updating password: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
