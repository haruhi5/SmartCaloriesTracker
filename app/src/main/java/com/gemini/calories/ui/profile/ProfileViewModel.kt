package com.gemini.calories.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.calories.data.local.ActivityLevel
import com.gemini.calories.data.local.FitnessGoal
import com.gemini.calories.data.local.Gender
import com.gemini.calories.data.local.UserEntity
import com.gemini.calories.data.manager.CsvExportManager
import com.gemini.calories.data.repository.CalorieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val calorieRepository: CalorieRepository,
    private val csvExportManager: CsvExportManager
) : ViewModel() {

    // Ideally collect currentUser from repo, but for form state we might need separate state.
    // For simplicity, we expose user flow and update functions.
    val currentUser = calorieRepository.getUser()
    
    // Export State
    private val _exportUri = MutableStateFlow<Uri?>(null)
    val exportUri = _exportUri.asStateFlow()

    fun saveProfile(
        name: String,
        height: Float,
        weight: Float,
        age: Int,
        gender: Gender,
        activityLevel: ActivityLevel,
        goal: FitnessGoal
    ) {
        viewModelScope.launch {
            val user = UserEntity(
                name = name,
                height = height,
                weight = weight,
                age = age,
                gender = gender,
                activityLevel = activityLevel,
                goal = goal
            )
            calorieRepository.saveUser(user)
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            val result = csvExportManager.exportToFile()
            if (result.isSuccess) {
                _exportUri.value = result.getOrNull()
            }
        }
    }
    
    fun shareExportedFile(uri: Uri) {
        csvExportManager.shareFile(uri)
    }
}
