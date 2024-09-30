package com.example.opscquizapp.models

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@Keep
@IgnoreExtraProperties
data class GameState(
    var userId: String? = null,
    var categoryId: Int? = null,
    var categoryName: String? = null,
    var difficulty: String? = null,
    var currentQuestionIndex: Int? = null,
    var score: Int? = null,
    var questions: List<QuestionFetch>? = null
) : Serializable {

    constructor() : this(null, null, null, null, null, null, null)
}