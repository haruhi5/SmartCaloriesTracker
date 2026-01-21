package com.gemini.calories.domain.repository

import com.gemini.calories.domain.model.FoodAnalysisResult

interface FoodAnalyzer {
    suspend fun analyze(imageData: ByteArray): Result<FoodAnalysisResult>
}
