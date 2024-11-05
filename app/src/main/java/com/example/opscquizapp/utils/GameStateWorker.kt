package com.example.opscquizapp.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.opscquizapp.models.GameState
import com.example.opscquizapp.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GameStateWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        val user = auth.currentUser
        if (user != null) {
            db.collection("gameStates").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val gameState = document.toObject(GameState::class.java)
                        if (gameState != null && gameState.questions != null && gameState.questions!!.isNotEmpty()) {
                            NotificationUtils.showContinueGameNotification(applicationContext)
                        } else {
                            NotificationUtils.cancelContinueGameNotification(applicationContext)
                        }
                    } else {
                        NotificationUtils.cancelContinueGameNotification(applicationContext)
                    }
                }
                .addOnFailureListener { e ->
                    // Log the error
                    // Optionally, retry or handle accordingly
                }
        }

        return Result.success()
    }
}