package com.quizapp.tork.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.quizapp.tork.R

class ProfileAdapter(
    private val userList: List<Map<String, Any>>,  // A list of user data as a Map
    private val onProfileUpdated: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameEditText: EditText = view.findViewById(R.id.name)
        val emailEditText: EditText = view.findViewById(R.id.email)
        val passwordEditText: EditText = view.findViewById(R.id.password)
        val updateButton: TextView = view.findViewById(R.id.Update)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_item, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val context = holder.itemView.context
        val userProfile = userList[position]

        // Bind profile data from the Map
        holder.nameEditText.setText(userProfile["name"] as String?)
        holder.emailEditText.setText(userProfile["email"] as String?)
        holder.passwordEditText.setText(userProfile["password"] as String?)

        // Update profile on button click
        holder.updateButton.setOnClickListener {
            val updatedName = holder.nameEditText.text.toString()
            val updatedEmail = holder.emailEditText.text.toString()
            val updatedPassword = holder.passwordEditText.text.toString()

            if (updatedName.isEmpty() || updatedEmail.isEmpty()) {
                Toast.makeText(context, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedUser = userProfile.toMutableMap().apply {
                put("name", updatedName)
                put("email", updatedEmail)
                put("password", updatedPassword)
            }

            val uid = userProfile["uid"] as? String // Assuming "uid" is a field in your Firestore document

            if (uid != null) {
                // Update Firestore
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update(
                        mapOf(
                            "name" to updatedName,
                            "email" to updatedEmail,
                            "password" to updatedPassword
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        onProfileUpdated(updatedUser)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "User ID not found!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
