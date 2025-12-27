package com.quizapp.tork

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.quizapp.tork.databinding.ActivityLoginScreenBinding

class LoginScreen : AppCompatActivity() {

    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private var auth = FirebaseAuth.getInstance()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Automatically log out any existing user
        auth.signOut()

        // Initialize ViewBinding
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)) // Replace with your web client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google Sign-In button click listener
        binding.googleSignInButton.setOnClickListener {
            googleSignIn()
        }

        // Handle login button click
        binding.lLoginIn.setOnClickListener {
            authLogin()
        }

        // Handle password visibility toggle
        binding.lPsw.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.lPsw.right - binding.lPsw.compoundDrawables[2].bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Navigate to SignupActivity when Register button is clicked
        binding.registerButton.setOnClickListener {
            val intent = Intent(this, SignUpScreen::class.java)
            startActivity(intent)
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    private fun googleSignIn() {
        // Sign out any previously signed-in Google account
        googleSignInClient.signOut().addOnCompleteListener {
            // Launch the Google Sign-In intent
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(Exception::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeScreen::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Firebase Auth failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun togglePasswordVisibility() {
        val padlockDrawable = binding.lPsw.compoundDrawables[0]

        if (isPasswordVisible) {
            binding.lPsw.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.lPsw.setCompoundDrawablesWithIntrinsicBounds(
                padlockDrawable, null,
                resources.getDrawable(R.drawable.ic_visibility_off, null), null
            )
            isPasswordVisible = false
        } else {
            binding.lPsw.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.lPsw.setCompoundDrawablesWithIntrinsicBounds(
                padlockDrawable, null,
                resources.getDrawable(R.drawable.ic_visibility, null), null
            )
            isPasswordVisible = true
        }

        binding.lPsw.setSelection(binding.lPsw.text.length)
    }

    private fun authLogin() {
        val email = binding.lEmail.text.toString().trim()
        val password = binding.lPsw.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeScreen::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}
