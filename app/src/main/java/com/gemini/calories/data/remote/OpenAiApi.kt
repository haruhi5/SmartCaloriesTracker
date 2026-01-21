package com.gemini.calories.data.remote

import com.gemini.calories.data.remote.dto.OpenAiRequest
import com.gemini.calories.data.remote.dto.OpenAiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse
}
