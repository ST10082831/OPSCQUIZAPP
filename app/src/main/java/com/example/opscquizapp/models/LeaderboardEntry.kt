package com.example.opscquizapp.models

import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.Date

data class LeaderboardEntry(
var userId: String? = null,
var username: String? = null,
var score: Int? = null,
var totalQuestions: Int? = null,
var percentage: Double? = null,
var categoryId: Int? = null,
var categoryName: String? = null,
@ServerTimestamp var timestamp: Date? = null

) : Serializable {
    constructor() : this(null, null, null, null, null, null, null, null)
}