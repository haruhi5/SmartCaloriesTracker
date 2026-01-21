package com.gemini.calories.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.calories.data.local.UserEntity
import com.gemini.calories.data.repository.CalorieRepository
import com.gemini.calories.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val calorieRepository: CalorieRepository
) : ViewModel() {

    val themeMode = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Lazily, "system")

    val isFirstLaunch = settingsRepository.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val currentUser = calorieRepository.getUser()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}
