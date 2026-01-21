package com.gemini.calories.domain.model

data class FoodAnalysisResult(
    val foods: List<FoodItem>,
    val totalCalories: Int,
    val confidence: Float,
    val notes: String? = null
)

data class FoodItem(
    val name: String,
    val calories: Int,
    val protein: Float, // grams
    val carbs: Float,   // grams
    val fat: Float,     // grams
    val portion: String,
    val confidence: Float
)
