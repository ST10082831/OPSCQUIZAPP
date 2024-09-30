package com.example.opscquizapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.opscquizapp.models.LeaderboardEntry

class LeaderboardAdapter(
    context: Context,
    private val entries: List<LeaderboardEntry>
) : ArrayAdapter<LeaderboardEntry>(context, 0, entries) {

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: inflater.inflate(
            R.layout.item_leaderboard_entry, parent, false
        )

        val entry = entries[position]

        val rankTextView = listItemView.findViewById<TextView>(R.id.rankTextView)
        val usernameTextView = listItemView.findViewById<TextView>(R.id.usernameTextView)
        val scoreTextView = listItemView.findViewById<TextView>(R.id.scoreTextView)
        val categoryTextView = listItemView.findViewById<TextView>(R.id.categoryTextView)

        rankTextView.text = "${position + 1}"
        usernameTextView.text = entry.username ?: "Unknown"
        scoreTextView.text = "Score: ${entry.score}/${entry.totalQuestions} (${String.format("%.2f", entry.percentage)}%)"
        categoryTextView.text = "Category: ${entry.categoryName}"

        return listItemView
    }
}
