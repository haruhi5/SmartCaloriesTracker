package com.gemini.calories.data.repository

import android.util.Base64
import com.gemini.calories.data.remote.OpenAiApi
import com.gemini.calories.data.remote.dto.Content
import com.gemini.calories.data.remote.dto.ImageUrl
import com.gemini.calories.data.remote.dto.Message
import com.gemini.calories.data.remote.dto.OpenAiRequest
import com.gemini.calories.domain.model.FoodAnalysisResult
import com.gemini.calories.domain.model.FoodItem
import com.gemini.calories.domain.repository.FoodAnalyzer
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GptFoodAnalyzer @Inject constructor(
    private val api: OpenAiApi,
    private val settingsRepository: SettingsRepository
) : FoodAnalyzer {

    override suspend fun analyze(imageData: ByteArray): Result<FoodAnalysisResult> {
        return Result.failure(Exception("Use analyzeWithKey() instead - this method requires API key to be passed via repository"))
    }
    
    suspend fun analyzeWithKey(imageData: ByteArray, apiKey: String): Result<FoodAnalysisResult> {
         return try {
            val base64Image = Base64.encodeToString(imageData, Base64.NO_WRAP)
            val prompt = """
                You are a nutrition expert. Analyze this image and identify all food items.
                Return a JSON object with:
                - foods: list of objects {name, calories(int), protein(float,g), carbs(float,g), fat(float,g), portion, confidence(0.0-1.0)}
                - totalCalories: int
                - confidence: float (overall)
                - notes: string (optional)
                
                Strictly return ONLY JSON.
            """.trimIndent()

            val request = OpenAiRequest(
                messages = listOf(
                    Message(
                        role = "user",
                        content = listOf(
                            Content.Text(text = prompt),
                            Content.Image(image_url = ImageUrl("data:image/jpeg;base64,$base64Image"))
                        )
                    )
                )
            )

            val response = api.chatCompletion("Bearer $apiKey", request)
            val jsonContent = response.choices.firstOrNull()?.message?.content 
                ?: return Result.failure(Exception("Empty response"))
            
            // Clean up code blocks if present
            val cleanJson = jsonContent.replace("```json", "").replace("```", "").trim()
            
            // We need to define a Serializable class for parsing the response JSON
            // For now, let's assuming manual parsing or create a DTO for the result.
            // Let's use a helper method to parse.
            val result = parseJson(cleanJson)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseJson(jsonString: String): FoodAnalysisResult {
        // Simple manual parsing or use kotlinx.serialization
        // For robustness, usually we define a DTO.
        // Let's assume we use a Serializable DTO in the domain folder or here.
        // Since I didn't create a Response DTO for the content, I will use a regex/simple parsing for now
        // to save context, or strictly I should have created a DTO.
        // Let's fallback to specific DTOs.
        
        // REFACTOR: I should create a response DTO for the JSON content.
        // But for this step, let's use a hypothetical parser.
        
        // Ideally: Json.decodeFromString<FoodAnalysisResultDto>(jsonString).toDomain()
        // I will implement a quick DTO here or assume it works.
        // Let's create `FoodAnalysisResultDto` in the same file for convenience for now.
        
        return Json { ignoreUnknownKeys = true }.decodeFromString<FoodAnalysisResultDto>(jsonString).toDomain()
    }
}

@kotlinx.serialization.Serializable
data class FoodAnalysisResultDto(
    val foods: List<FoodItemDto>,
    val totalCalories: Int,
    val confidence: Float,
    val notes: String? = null
) {
    fun toDomain() = FoodAnalysisResult(
        foods = foods.map { it.toDomain() },
        totalCalories = totalCalories,
        confidence = confidence,
        notes = notes
    )
}

@kotlinx.serialization.Serializable
data class FoodItemDto(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val portion: String,
    val confidence: Float
) {
    fun toDomain() = FoodItem(
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        portion = portion,
        confidence = confidence
    )
}
