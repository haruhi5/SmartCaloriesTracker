package com.gemini.calories.data.repository

import android.util.Log
import com.gemini.calories.domain.model.FoodAnalysisResult
import com.gemini.calories.domain.repository.FoodAnalyzer
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodAnalyzerRepository @Inject constructor(
    private val gptAnalyzer: GptFoodAnalyzer,
    private val geminiAnalyzer: LocalGeminiAnalyzer,
    private val settingsRepository: SettingsRepository
) : FoodAnalyzer {

    companion object {
        private const val TAG = "FoodAnalyzerRepo"
    }

    override suspend fun analyze(imageData: ByteArray): Result<FoodAnalysisResult> {
        Log.d(TAG, "analyze start, image size=${imageData.size}")
        return try {
            val apiType = settingsRepository.apiType.first()
            Log.d(TAG, "apiType=$apiType")
            val result = if (apiType == "gpt") {
                val key = settingsRepository.apiKey.first()
                if (key.isBlank()) {
                    Log.e(TAG, "API Key missing")
                    return Result.failure(Exception("API Key not found"))
                }
                Log.d(TAG, "using GPT analyzer")
                gptAnalyzer.analyzeWithKey(imageData, key)
            } else {
                Log.d(TAG, "using Gemini analyzer")
                geminiAnalyzer.analyze(imageData)
            }
            result.onSuccess { Log.d(TAG, "analysis success") }
            result.onFailure { Log.e(TAG, "analysis failed", it) }
            result
        } catch (e: Exception) {
            Log.e(TAG, "analyze error", e)
            Result.failure(e)
        }
    }
}
