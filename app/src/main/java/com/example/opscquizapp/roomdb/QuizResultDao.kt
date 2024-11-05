package com.example.opscquizapp.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuizResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity)

    @Query("SELECT * FROM quiz_results WHERE isSynced = 0")
    suspend fun getUnsyncedResults(): List<QuizResultEntity>

    @Query("UPDATE quiz_results SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markResultsAsSynced(ids: List<Int>)
}