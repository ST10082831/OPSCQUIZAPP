package com.example.opscquizapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.opscquizapp.databinding.ActivityMainBinding
import com.example.opscquizapp.models.GameState
import com.example.opscquizapp.utils.GameStateWorker
import com.example.opscquizapp.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.io.Serializable
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // Declare the Firestore instance
    private var gameStateListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // If user is logged in, navigate to MainMenuActivity
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()
            return // Exit onCreate to prevent further execution
        }

        // Navigate to ContinueGameActivity if there's a saved game
        val intent = Intent(this, ContinueGameActivity::class.java)
        startActivity(intent)

        // Navigate to LoginActivity when login button is clicked
        binding.loginButton.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        // Navigate to RegisterActivity when register button is clicked
        binding.registerButton.setOnClickListener {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
        }

        // Create notification channel
        NotificationUtils.createNotificationChannel(this)

        // Check for saved game and notify
        listenForGameState()

        scheduleGameStateWorker()
    }
    private fun scheduleGameStateWorker() {
        val workRequest = PeriodicWorkRequestBuilder<GameStateWorker>(10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
    private fun listenForGameState() {
        val user = auth.currentUser
        if (user != null) {
            gameStateListener = db.collection("gameStates").document(user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MainActivity", "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val gameState = snapshot.toObject(GameState::class.java)
                        if (gameState != null && gameState.questions != null && gameState.questions!!.isNotEmpty()) {
                            NotificationUtils.showContinueGameNotification(this)
                        } else {
                            NotificationUtils.cancelContinueGameNotification(this)
                        }
                    } else {
                        NotificationUtils.cancelContinueGameNotification(this)
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameStateListener?.remove()
    }
}


