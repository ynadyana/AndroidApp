package com.quizapp.tork

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quizapp.tork.databinding.FragmentWalletBinding
import com.quizapp.tork.model.User
import com.quizapp.tork.model.Withdraw

class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        val view = binding.root

        var user = User()
        val database = FirebaseFirestore.getInstance()

        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
            return view
        }

        // Fetch user data
        database.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val fetchedUser = documentSnapshot.toObject(User::class.java)
                if (fetchedUser != null) {
                    user = fetchedUser
                    binding.currentCoin.text = user.points?.toString() ?: "0"
                } else {
                    Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching user data: ${it.message}", Toast.LENGTH_LONG).show()
            }

        // Handle withdraw request
        binding.send.setOnClickListener {
            val paypal = binding.paypal.text.toString().trim()
            if (paypal.isEmpty()) {
                Toast.makeText(context, "Please enter your PayPal email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userCoins = user.points ?: 0 // Default to 0 if null
            if (userCoins > 5) {
                val req = Withdraw(uid, paypal, user.name ?: "Anonymous")

                database.collection("withdraw")
                    .document(uid)
                    .set(req)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Request sent successfully", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to send request: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(context, "You need more than 5 coins to withdraw", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
