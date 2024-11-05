// File: LeaderboardActivity.kt
package com.example.opscquizapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.opscquizapp.models.LeaderboardEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var leaderboardListView: ListView
    private lateinit var progressBar: ProgressBar

    private lateinit var leaderboardEntries: List<LeaderboardEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        db = FirebaseFirestore.getInstance()
        leaderboardListView = findViewById(R.id.leaderboardListView)
        progressBar = findViewById(R.id.leaderboardProgressBar)

        fetchLeaderboardEntries()
    }

    private fun fetchLeaderboardEntries() {
        progressBar.visibility = View.VISIBLE

        db.collection("leaderboard")
            .orderBy("percentage", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                leaderboardEntries = result.toObjects(LeaderboardEntry::class.java)
                displayLeaderboard()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e("LeaderboardActivity", "Error fetching leaderboard entries", e)
                Toast.makeText(this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayLeaderboard() {
        val adapter = LeaderboardAdapter(this, leaderboardEntries)
        leaderboardListView.adapter = adapter
        leaderboardListView.visibility = View.VISIBLE
    }


}
