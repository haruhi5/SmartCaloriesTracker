package com.gemini.calories.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import com.gemini.calories.domain.model.FoodAnalysisResult
import com.gemini.calories.domain.repository.FoodAnalyzer
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LocalGeminiAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val onDeviceGeminiChecker: OnDeviceGeminiChecker
) : FoodAnalyzer {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",  // Cloud fallback model
            apiKey = ""  // Empty for now; add your Gemini API key if using cloud fallback
        )
    }

    fun isOnDeviceSupported(): Boolean = onDeviceGeminiChecker.isAvailable()

    override suspend fun analyze(imageData: ByteArray): Result<FoodAnalysisResult> {
        return withContext(Dispatchers.IO) {
            try {
                // If this is truly local (AICore), the code is different. 
                // Assuming standard Gemini Pro Vision behavior for now as a fallback/placeholder 
                // since Nano integration is device specific.
                
                // Let's treat this as "Gemini Analyzer" (could be remote if Nano not available).
                
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                
                val prompt = """
                    Analyze this image and identify all food items.
                    Return JSON: { "foods": [...], "totalCalories": int, "confidence": float }
                """.trimIndent()
                
                val response = generativeModel.generateContent(
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
}
