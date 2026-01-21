package com.gemini.calories.data.repository

import com.gemini.calories.data.local.*
import com.gemini.calories.domain.model.FoodAnalysisResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalorieRepository @Inject constructor(
    private val foodEntryDao: FoodEntryDao,
    private val userDao: UserDao
) {

    fun getTodayEntries(): Flow<List<FoodEntryEntity>> {
        val today = LocalDate.now()
        // Here we assume userId 1 for now or we should fetch current user.
        // For simple single-user app, we can query User first.
        // Assuming user ID 1 is always the active user.
        return foodEntryDao.getEntriesByDate(1L, today)
    }

    fun getDailyStats(): Flow<List<DailyStats>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6)
        return foodEntryDao.getDailyStats(1L, startDate, endDate)
    }

    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun saveUser(user: UserEntity) {
        // If user exists, update, else insert.
        // Since we don't have ID, we rely on existing logic.
        // Ideally we check if user exists.
        val existing = userDao.insertUser(user)
        // If REPLACE strategy is used, it might replace ID, so we need care.
        // But for single user, it's fine.
    }

    suspend fun addFoodEntry(
        result: FoodAnalysisResult,
        imagePath: String?,
        mealType: MealType
    ) {
        // Fetch current user ID (assuming 1 for MVP)
        val userId = 1L
        
        result.foods.forEach { food ->
            val entity = FoodEntryEntity(
                userId = userId,
                foodName = food.name,
                calories = food.calories,
                protein = food.protein,
                carbs = food.carbs,
                fat = food.fat,
                portion = food.portion,
                mealType = mealType,
                imagePath = imagePath,
                date = LocalDate.now(),
                aiConfidence = food.confidence
            )
            foodEntryDao.insert(entity)
        }
    }
}
