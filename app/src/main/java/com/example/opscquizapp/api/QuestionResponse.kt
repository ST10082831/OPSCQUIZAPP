package com.example.opscquizapp.api

import com.example.opscquizapp.models.QuestionFetch

data class QuestionResponse(
    val response_code: Int,
    val results: List<QuestionFetch>
)

data class Question(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    val correct_answer: List<String>,
    val incorrect_answers: List<String>
)