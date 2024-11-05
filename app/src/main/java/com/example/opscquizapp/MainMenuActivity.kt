// File: MainMenuActivity.kt
package com.example.opscquizapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.work.*
import com.example.opscquizapp.roomdb.SyncWorker

class MainMenuActivity : BaseActivity() {

    private lateinit var startQuizButton: MaterialButton
    private lateinit var continueGameButton: MaterialButton
    private lateinit var leaderboardButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Implement attachBaseContext for language settings
        setContentView(R.layout.activity_main_menu)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        startQuizButton = findViewById(R.id.startQuizButton)
        continueGameButton = findViewById(R.id.continueGameButton)
        settingsButton = findViewById(R.id.settingsButton)
        leaderboardButton = findViewById(R.id.leaderboardButton)

        leaderboardButton.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }
        startQuizButton.setOnClickListener {
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
        scheduleDataSync()

    }

    override fun onResume() {
        super.onResume()
        checkForSavedGame()
    }

    private fun scheduleDataSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(syncWorkRequest)
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


}

