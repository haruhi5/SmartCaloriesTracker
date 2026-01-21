package com.gemini.calories.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.calories.data.local.DailyStats
import com.gemini.calories.data.local.FoodEntryEntity
import com.gemini.calories.data.repository.CalorieRepository
import com.gemini.calories.domain.util.TdeeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val calorieRepository: CalorieRepository
) : ViewModel() {

    val todayEntries = calorieRepository.getTodayEntries()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val user = calorieRepository.getUser()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val todayCalories = todayEntries.map { entries -> entries.sumOf { it.calories } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val targetCalories = user.map { user ->
        if (user != null) {
            val bmr = TdeeCalculator.calculateBmr(user.weight, user.height, user.age, user.gender)
            val tdee = TdeeCalculator.calculateTdee(bmr, user.activityLevel)
            TdeeCalculator.calculateTargetCalories(tdee, user.goal)
        } else {
            2000 // Default
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 2000)
    
    val macros = todayEntries.map { entries -> 
        Triple(
            entries.sumOf { it.protein.toDouble() }.toFloat(),
            entries.sumOf { it.carbs.toDouble() }.toFloat(),
            entries.sumOf { it.fat.toDouble() }.toFloat()
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, Triple(0f, 0f, 0f))
}
