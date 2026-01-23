package com.gemini.calories.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.calories.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val apiKey = settingsRepository.apiKey
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val apiType = settingsRepository.apiType
        .stateIn(viewModelScope, SharingStarted.Lazily, "gpt")

    val geminiApiKey = settingsRepository.geminiApiKey
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setApiKey(key)
        }
    }
    
    fun setApiType(type: String) {
        viewModelScope.launch {
            settingsRepository.setApiType(type)
        }
    }

    fun saveGeminiApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setGeminiApiKey(key)
        }
    }
}
