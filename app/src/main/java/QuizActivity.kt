package com.quizapp.tork

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.quizapp.tork.databinding.ActivityQuizBinding
import com.bumptech.glide.Glide
import com.quizapp.tork.model.Question


class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private var index = 0
    private var crct = 0
    private var time: CountDownTimer? = null
    private var question = ArrayList<Question>()
    private var quest = Question()

    private var correctSound: MediaPlayer? = null
    private var wrongSound: MediaPlayer? = null

    private var isQuizFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize MediaPlayer for sounds
        correctSound = MediaPlayer.create(this, R.raw.correct)
        wrongSound = MediaPlayer.create(this, R.raw.wrong)

        val catId = intent.getStringExtra("catId")
        val database = FirebaseFirestore.getInstance()

        database.collection("categories")
            .document(catId!!)
            .collection("Questions")
            .orderBy("index")
            .limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "No questions available for this category.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    for (snapshot: DocumentSnapshot in querySnapshot) {
                        val q: Question = snapshot.toObject(Question::class.java)!!
                        question.add(q)
                    }
                    setQuestion()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load questions: ${exception.message}", Toast.LENGTH_LONG).show()
                finish()
            }

        startTimer()
    }

    private fun setQuestion() {
        if (index < question.size) {
            enableOptions()
            binding.quesCounter.text = String.format("%d/%d", (index + 1), question.size)
            quest = question[index]
            binding.ques.text = quest.ques
            binding.op1.text = quest.op1
            binding.op2.text = quest.op2
            binding.op3.text = quest.op3
            binding.op4.text = quest.op4
            binding.progressBar.progress = index
            binding.progressBar.max = question.size

            // Load the question image if available
            if (quest.image.isNullOrEmpty()) {
                binding.questionImage.visibility = View.GONE
            } else {
                binding.questionImage.visibility = View.VISIBLE
                Glide.with(this).load(quest.image).into(binding.questionImage)
            }
        }
    }

    private fun checkAnswer(textView: TextView) {
        disableOptions()
        val selectedAns = textView.text.toString()
        if (selectedAns == quest.ans) {
            crct++
            textView.background = ContextCompat.getDrawable(this, R.drawable.correct_option)
            correctSound?.start()
        } else {
            showAns()
            textView.background = ContextCompat.getDrawable(this, R.drawable.wrong_option)
            wrongSound?.start()
        }
    }

    private fun reset() {
        binding.op1.background = ContextCompat.getDrawable(this, R.drawable.option_background)
        binding.op2.background = ContextCompat.getDrawable(this, R.drawable.option_background)
        binding.op3.background = ContextCompat.getDrawable(this, R.drawable.option_background)
        binding.op4.background = ContextCompat.getDrawable(this, R.drawable.option_background)
    }

    private fun startTimer() {
        time = object : CountDownTimer(70000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timer.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {

                if (!isQuizFinished) {
                    val intent = Intent(this@QuizActivity, ResultActivity::class.java)
                    intent.putExtra("correct", crct)
                    intent.putExtra("total", question.size)
                    startActivity(intent)
                    finish()
                }
            }
        }
        time?.start()
    }

    private fun showAns() {
        when (quest.ans) {
            binding.op1.text.toString() -> binding.op1.background =
                ContextCompat.getDrawable(this, R.drawable.correct_option)
            binding.op2.text.toString() -> binding.op2.background =
                ContextCompat.getDrawable(this, R.drawable.correct_option)
            binding.op3.text.toString() -> binding.op3.background =
                ContextCompat.getDrawable(this, R.drawable.correct_option)
            binding.op4.text.toString() -> binding.op4.background =
                ContextCompat.getDrawable(this, R.drawable.correct_option)
        }
    }

    private fun enableOptions() {
        binding.op1.isClickable = true
        binding.op2.isClickable = true
        binding.op3.isClickable = true
        binding.op4.isClickable = true
    }

    private fun disableOptions() {
        binding.op1.isClickable = false
        binding.op2.isClickable = false
        binding.op3.isClickable = false
        binding.op4.isClickable = false
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.op_1, R.id.op_2, R.id.op_3, R.id.op_4 -> {
                val selected: TextView = view as TextView
                checkAnswer(selected)
                Toast.makeText(this, "Option Selected", Toast.LENGTH_SHORT).show()
            }

            R.id.next -> {
                reset()
                if (index < question.size - 1) {
                    index++
                    setQuestion()
                } else {
                    isQuizFinished = true // ✅ Mark the quiz as finished
                    time?.cancel() // ✅ Cancel the timer
                    val intent = Intent(this@QuizActivity, ResultActivity::class.java)
                    intent.putExtra("correct", crct)
                    intent.putExtra("total", question.size)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayer resources
        correctSound?.release()
        wrongSound?.release()
        time?.cancel()
    }
}
