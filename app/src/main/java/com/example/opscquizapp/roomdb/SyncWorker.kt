package com.example.opscquizapp.roomdb

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.opscquizapp.roomdb.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val dbLocal = AppDatabase.getDatabase(appContext)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun doWork(): Result {
        val unsyncedResults = dbLocal.quizResultDao().getUnsyncedResults()
        if (unsyncedResults.isNotEmpty()) {
            try {
                for (result in unsyncedResults) {
                    val leaderboardEntry = hashMapOf(
                        "userId" to result.userId,
                        "score" to result.score,
                        "totalQuestions" to result.totalQuestions,
                        "percentage" to result.percentage,
                        "categoryId" to result.categoryId,
                        "categoryName" to result.categoryName,
                        "timestamp" to result.timestamp
                    )

                    firestore.collection("leaderboard")
                        .add(leaderboardEntry)
                        .await()

                    // Mark as synced
                    dbLocal.quizResultDao().markResultsAsSynced(listOf(result.id))
                }
                return Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                return Result.retry()
            }
        } else {
            return Result.success()
        }
    }
}