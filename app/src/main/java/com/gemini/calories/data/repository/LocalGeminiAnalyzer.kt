package com.gemini.calories.data.repository

import android.graphics.BitmapFactory
import com.gemini.calories.domain.model.FoodAnalysisResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LocalGeminiAnalyzer @Inject constructor() {

    suspend fun analyzeWithKey(imageData: ByteArray, apiKey: String): Result<FoodAnalysisResult> {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                
                val prompt = """
                    Analyze this image and identify all food items.
                    Return JSON: { "foods": [...], "totalCalories": int, "confidence": float }
                """.trimIndent()
                
                val model = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )

                val response = model.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                
                val text = response.text ?: return@withContext Result.failure(Exception("Empty response"))
                val cleanJson = text.replace("```json", "").replace("```", "").trim()
                
                // Reuse DTO from GptFoodAnalyzer or move DTOs to common file.
                // Since they are in the same package, I can reuse FoodAnalysisResultDto if it's internal/public.
                // I'll assume I can copy/paste or access it.
                
                val result = Json { ignoreUnknownKeys = true }
                    .decodeFromString<FoodAnalysisResultDto>(cleanJson)
                    .toDomain()
                    
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Kept only to satisfy any interface expectations; prefer analyzeWithKey.
    suspend fun analyze(imageData: ByteArray): Result<FoodAnalysisResult> =
        Result.failure(Exception("Use analyzeWithKey() and provide a Gemini API key."))
}
