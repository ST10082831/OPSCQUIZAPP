// File: MainMenuActivity.kt
package com.example.opscquizapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainMenuActivity : AppCompatActivity() {

    private lateinit var newGameButton: Button
    private lateinit var continueGameButton: Button
    private lateinit var settingsButton: Button
    private lateinit var leaderboardButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Implement attachBaseContext for language settings
        setContentView(R.layout.activity_main_menu)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        newGameButton = findViewById(R.id.newGameButton)
        continueGameButton = findViewById(R.id.continueGameButton)
        settingsButton = findViewById(R.id.settingsButton)
        leaderboardButton = findViewById(R.id.leaderboardButton)

        leaderboardButton.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }
        newGameButton.setOnClickListener {
            val intent = Intent(this, CategorySelectionActivity::class.java)
            startActivity(intent)
        }

        continueGameButton.setOnClickListener {
            val intent = Intent(this, ContinueGameActivity::class.java)
            startActivity(intent)
        }

        checkForSavedGame()


        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onResume() {
        super.onResume()
        checkForSavedGame()
    }

    private fun checkForSavedGame() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("gameStates")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    continueGameButton.isEnabled = document.exists()
                }
                .addOnFailureListener {
                    continueGameButton.isEnabled = false
                }
        } else {
            continueGameButton.isEnabled = false
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("QuizAppSettings", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }
}

