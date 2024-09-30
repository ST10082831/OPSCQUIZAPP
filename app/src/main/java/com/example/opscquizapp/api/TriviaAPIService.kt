package com.example.opscquizapp.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApiService {
    @GET("api_category.php")
    fun getCategories(): Call<CategoryResponse>

    @GET("api.php")
    fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String?,
        @Query("type") type: String?
    ): Call<QuestionResponse>
}