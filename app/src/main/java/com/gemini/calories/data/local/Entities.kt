package com.gemini.calories.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val height: Float,          // cm
    val weight: Float,          // kg
    val age: Int,
    val gender: Gender,
    val activityLevel: ActivityLevel,
    val goal: FitnessGoal,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class Gender { MALE, FEMALE }
enum class ActivityLevel { SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE }
enum class FitnessGoal { MAINTAIN, MUSCLE_GAIN, FAT_LOSS }

@Entity(
    tableName = "food_entries",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("date")]
)
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val foodName: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val portion: String,
    val mealType: MealType,
    val imagePath: String?,
    val date: LocalDate,        // Used for grouping by date
    val timestamp: Long = System.currentTimeMillis(),
    val isManualEntry: Boolean = false,
    val aiConfidence: Float? = null
)

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }
