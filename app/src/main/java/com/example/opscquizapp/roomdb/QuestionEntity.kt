package com.example.opscquizapp.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String?,
    val type: String?,
    val difficulty: String?,
    val question: String?,
    val correctAnswer: String?,
    val incorrectAnswers: String?, // Stored as JSON String
    val isSynced: Boolean = true   // Flag to track synchronization
)