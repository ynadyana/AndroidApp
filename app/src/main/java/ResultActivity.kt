package com.quizapp.tork

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quizapp.tork.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private val POINTS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val correctAnswers = intent.getIntExtra("correct", 0)
        val totalQuestions = intent.getIntExtra("total", 0)
        val points = correctAnswers * POINTS

        // Update UI with score
        binding.score.text = String.format("%d/%d", correctAnswers, totalQuestions)

        // Show earned points
        binding.earnedPoints.text = String.format("You Earned: %d Points", points)

        val percentage = ((correctAnswers.toFloat() / totalQuestions.toFloat()) * 100).toInt()

        // Update Firestore points
        updateFirestorePoints(points)

        // Display stars based on the score percentage
        displayStars(percentage)

        // Navigate to the Homepage and clear the back stack
        binding.home.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun updateFirestorePoints(points: Int) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().uid

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val currentCoins = document.getLong("points") ?: 0
                    val newTotalCoins = currentCoins + points

                    db.collection("users")
                        .document(userId)
                        .update("points", newTotalCoins)
                }
        }
    }

    private fun displayStars(percentage: Int) {
        when {
            percentage > 90 -> {
                binding.scoreTitle.text = "PERFECT"
                binding.scoreTitle.setTextColor(Color.parseColor("#FFFFFFFF"))
                binding.imageView4.setImageResource(R.drawable.winner_trophy)
                binding.star1.visibility = View.VISIBLE
                binding.star2.visibility = View.VISIBLE
                binding.star3.visibility = View.VISIBLE
                binding.star1.setImageResource(R.drawable.star1)
                binding.star2.setImageResource(R.drawable.star2)
                binding.star3.setImageResource(R.drawable.star3)
            }
            percentage in 60..90 -> {
                binding.scoreTitle.text = "GOOD"
                binding.scoreTitle.setTextColor(Color.parseColor("#FFFFFFFF"))
                binding.imageView4.setImageResource(R.drawable.silver)
                binding.star1.visibility = View.VISIBLE
                binding.star2.visibility = View.VISIBLE
                binding.star3.visibility = View.GONE
                binding.star1.setImageResource(R.drawable.star1)
                binding.star2.setImageResource(R.drawable.star2)
            }
            else -> {
                binding.scoreTitle.text = "OOPS"
                binding.scoreTitle.setTextColor(Color.parseColor("#FFFFFFFF"))
                binding.imageView4.setImageResource(R.drawable.sad)
                binding.star1.visibility = View.GONE
                binding.star2.visibility = View.GONE
                binding.star3.visibility = View.GONE
            }
        }
    }
}
