package com.quizapp.tork

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quizapp.tork.databinding.ActivitySignUpBinding
import com.quizapp.tork.model.User

class SignUpScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var name: String
    private var auth = FirebaseAuth.getInstance()
    private var database = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUp.setOnClickListener {
            validateAndCreateUser()
        }

        binding.loginIn.setOnClickListener {
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateAndCreateUser() {
        email = binding.sEmail.text.toString().trim()
        password = binding.sPsw.text.toString().trim()
        name = binding.sName.text.toString().trim()

        // Check if any field is empty
        if (name.isEmpty()) {
            binding.sName.error = "Name is required"
            binding.sName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.sEmail.error = "Email is required"
            binding.sEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.sPsw.error = "Password is required"
            binding.sPsw.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.sPsw.error = "Password must be at least 6 characters"
            binding.sPsw.requestFocus()
            return
        }

        createUser()
    }

    private fun createUser() {
        val user = User(name, email, password)

        // Create user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid

                    database.collection("users")
                        .document(uid!!)
                        .set(user)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Successfully Registered. Please log in.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginScreen::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}
