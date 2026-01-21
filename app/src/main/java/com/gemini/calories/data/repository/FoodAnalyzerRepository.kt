package com.gemini.calories.data.repository

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

    override suspend fun analyze(imageData: ByteArray): Result<FoodAnalysisResult> {
        val apiType = settingsRepository.apiType.first()
        return if (apiType == "gpt") {
            val key = settingsRepository.apiKey.first()
            if (key.isBlank()) return Result.failure(Exception("API Key not found"))
            gptAnalyzer.analyzeWithKey(imageData, key)
        } else {
            geminiAnalyzer.analyze(imageData)
        }
    }
}
