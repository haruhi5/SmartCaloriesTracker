package com.gemini.calories.domain.util

import com.gemini.calories.data.local.ActivityLevel
import com.gemini.calories.data.local.FitnessGoal
import com.gemini.calories.data.local.Gender

object TdeeCalculator {
    fun calculateBmr(weight: Float, height: Float, age: Int, gender: Gender): Float {
        val base = 10 * weight + 6.25f * height - 5 * age
        return when (gender) {
            Gender.MALE -> base + 5
            Gender.FEMALE -> base - 161
        }
    }

    fun calculateTdee(bmr: Float, activityLevel: ActivityLevel): Float {
        val factor = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2f
            ActivityLevel.LIGHT -> 1.375f
            ActivityLevel.MODERATE -> 1.55f
            ActivityLevel.ACTIVE -> 1.725f
            ActivityLevel.VERY_ACTIVE -> 1.9f
        }
        return bmr * factor
    }

    fun calculateTargetCalories(tdee: Float, goal: FitnessGoal): Int {
        val adjustment = when (goal) {
            FitnessGoal.MAINTAIN -> 0
            FitnessGoal.MUSCLE_GAIN -> 300
            FitnessGoal.FAT_LOSS -> -500
        }
        return (tdee + adjustment).toInt()
    }
}
