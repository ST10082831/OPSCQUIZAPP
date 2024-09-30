package com.example.opscquizapp.models

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@Keep
@IgnoreExtraProperties
data class QuestionFetch(
    var category: String? = null,
    var type: String? = null,
    var difficulty: String? = null,
    var question: String? = null,
    var correct_answer: String? = null,
    var incorrect_answers: List<String>? = null
) : Serializable {
    constructor() : this(null, null, null, null, null, null)
}
