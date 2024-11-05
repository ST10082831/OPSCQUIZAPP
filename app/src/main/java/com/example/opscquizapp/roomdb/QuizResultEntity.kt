package com.example.opscquizapp.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String?,
    val score: Int?,
    val totalQuestions: Int?,
    val percentage: Double?,
    val categoryId: Int?,
    val categoryName: String?,
    val timestamp: Long?,
    val isSynced: Boolean = false  // Flag to track synchronization
)