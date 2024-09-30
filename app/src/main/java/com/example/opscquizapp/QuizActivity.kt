// File: QuizActivity.kt
package com.example.opscquizapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.opscquizapp.api.ApiClient
import com.example.opscquizapp.models.QuestionFetch
import com.example.opscquizapp.api.QuestionResponse
import com.example.opscquizapp.models.GameState
import com.example.opscquizapp.models.LeaderboardEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuizActivity : AppCompatActivity() {

    private lateinit var questionNumberTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var answersRadioGroup: RadioGroup
    private lateinit var nextButton: Button

    private var questions: List<QuestionFetch> = listOf()
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedCategoryId = 0
    private var selectedCategoryName: String = ""
    private var selectedDifficulty: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        questionNumberTextView = findViewById(R.id.questionNumberTextView)
        questionTextView = findViewById(R.id.questionTextView)
        answersRadioGroup = findViewById(R.id.answersRadioGroup)
        nextButton = findViewById(R.id.nextButton)

        val gameState = intent.getSerializableExtra("gameState") as? GameState

        if (gameState != null) {
            // Continue from saved game state
            selectedCategoryId = gameState.categoryId ?: 0
            selectedCategoryName = gameState.categoryName ?: ""
            selectedDifficulty = gameState.difficulty
            currentQuestionIndex = gameState.currentQuestionIndex ?: 0
            score = gameState.score ?: 0
            questions = gameState.questions ?: emptyList()

            if (questions.isNotEmpty()) {
                displayQuestion()
            } else {
                Toast.makeText(this, "No questions available to continue.", Toast.LENGTH_LONG)
                    .show()
                finish()
            }
        } else {
            // Start a new game
            selectedCategoryId = intent.getIntExtra("category_id", 0)
            selectedCategoryName = intent.getStringExtra("category_name") ?: ""
            selectedDifficulty = intent.getStringExtra("difficulty")
            fetchQuestions()
        }
        nextButton.setOnClickListener {
            checkAnswer()
        }
    }

    private fun fetchQuestions() {
        val call = ApiClient.apiService.getQuestions(
            amount = 10,
            category = selectedCategoryId,
            difficulty = selectedDifficulty,
            type = null
        )

        call.enqueue(object : Callback<QuestionResponse> {
            override fun onResponse(
                call: Call<QuestionResponse>,
                response: Response<QuestionResponse>
            ) {
                if (response.isSuccessful) {
                    questions = response.body()?.results ?: emptyList()
                    if (questions.isNotEmpty()) {
                        displayQuestion()
                    } else {
                        Toast.makeText(
                            this@QuizActivity,
                            "No questions available for the selected options.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@QuizActivity,
                        "Failed to load questions",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                Toast.makeText(
                    this@QuizActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayQuestion() {
        if (currentQuestionIndex >= questions.size) {
            // No more questions
            showResult()
            return
        }

        val question = questions[currentQuestionIndex]

        questionNumberTextView.text = "Question ${currentQuestionIndex + 1}"
        questionTextView.text = android.text.Html.fromHtml(question.question ?: "", android.text.Html.FROM_HTML_MODE_LEGACY)

        answersRadioGroup.removeAllViews()

        val answers = mutableListOf<String>()
        if (question.incorrect_answers != null) {
            answers.addAll(question.incorrect_answers!!)
        }
        if (question.correct_answer != null) {
            answers.add(question.correct_answer!!)
        }
        answers.shuffle()

        for (answer in answers) {
            val radioButton = RadioButton(this)
            radioButton.text = android.text.Html.fromHtml(answer, android.text.Html.FROM_HTML_MODE_LEGACY)
            answersRadioGroup.addView(radioButton)
        }
    }

    private fun checkAnswer() {
        val selectedRadioButtonId = answersRadioGroup.checkedRadioButtonId
        if (selectedRadioButtonId != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
            val selectedAnswer = selectedRadioButton.text.toString()
            val correctAnswer = android.text.Html.fromHtml(questions[currentQuestionIndex].correct_answer).toString()

            if (selectedAnswer == correctAnswer) {
                score++
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show()
            }

            currentQuestionIndex++

            if (currentQuestionIndex < questions.size) {
                displayQuestion()
                saveGameState() // Save progress after each question
            } else {
                showResult()
            }
        } else {
            // Handle no answer is selected
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showResult() {
        // Calculate the result percentage
        val percentage = (score.toDouble() / questions.size.toDouble()) * 100

        // Show the final score
        val resultMessage = "You scored $score out of ${questions.size} (${String.format("%.2f", percentage)}%)"
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Quiz Completed")
        builder.setMessage(resultMessage)
        builder.setPositiveButton("OK") { _, _ ->
            deleteGameState()
            saveQuizResult()
            finish()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun saveQuizResult() {
        val user = auth.currentUser
        if (user != null) {
            val username = user.displayName ?: "Unknown"
            val percentage = (score.toDouble() / questions.size.toDouble()) * 100

            val leaderboardEntry = LeaderboardEntry(
                userId = user.uid,
                username = username,
                score = score,
                totalQuestions = questions.size,
                percentage = percentage,
                categoryId = selectedCategoryId,
                categoryName = selectedCategoryName
            )

            db.collection("leaderboard")
                .add(leaderboardEntry)
                .addOnSuccessListener {
                    Log.d("QuizActivity", "Quiz result saved successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("QuizActivity", "Error saving quiz result", e)
                }
        }

    }
    private fun saveGameState() {
        val user = auth.currentUser
        if (user != null) {
            val gameState = hashMapOf(
                "currentQuestionIndex" to currentQuestionIndex,
                "score" to score,
                "selectedCategoryId" to selectedCategoryId,
                "selectedCategoryName" to selectedCategoryName,
                "questions" to questions.map { question ->
                    hashMapOf(
                        "question" to question.question,
                        "correct_answer" to question.correct_answer,
                        "incorrect_answers" to question.incorrect_answers
                    )
                }
            )

            db.collection("gameStates").document(user.uid)
                .set(gameState)
                .addOnSuccessListener {
                    Log.d("QuizActivity", "Game state saved successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("QuizActivity", "Error saving game state", e)
                }
        }
    }

    private fun deleteGameState() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("gameStates").document(user.uid)
                .delete()
                .addOnSuccessListener {
                    Log.d("QuizActivity", "Game state deleted successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("QuizActivity", "Error deleting game state", e)
                }
        }
    }

    override fun onBackPressed() {
        saveGameState()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveGameState()
    }


    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("QuizAppSettings", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }
}
