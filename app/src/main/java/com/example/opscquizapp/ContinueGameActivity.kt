// File: ContinueGameActivity.kt
package com.example.opscquizapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.opscquizapp.models.GameState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

class ContinueGameActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkForSavedGame()
    }

    private fun checkForSavedGame() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("gameStates").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        try {
                            val gameState = document.toObject(GameState::class.java)
                            if (gameState != null && gameState.questions != null && gameState.questions!!.isNotEmpty()) {
                                continueGame(gameState)
                            } else {
                                showNoSavedGameMessage()
                            }
                        } catch (e: Exception) {
                            Log.e("ContinueGameActivity", "Error deserializing game state", e)
                            showErrorFetchingGameMessage()
                        }
                    } else {
                        showNoSavedGameMessage()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ContinueGameActivity", "Error fetching game state", e)
                    showErrorFetchingGameMessage()
                }
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    private fun continueGame(gameState: GameState) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("gameState", gameState as Serializable)
        startActivity(intent)
        finish()
    }

    private fun showNoSavedGameMessage() {
        Toast.makeText(this, "No saved game found.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showErrorFetchingGameMessage() {
        Toast.makeText(this, "Error fetching saved game.", Toast.LENGTH_SHORT).show()
        finish()
    }
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("QuizAppSettings", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }
}

