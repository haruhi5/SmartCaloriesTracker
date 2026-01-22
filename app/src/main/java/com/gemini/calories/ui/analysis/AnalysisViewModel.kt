package com.gemini.calories.ui.analysis

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.calories.data.local.MealType
import com.gemini.calories.data.repository.CalorieRepository
import com.gemini.calories.data.repository.FoodAnalyzerRepository
import com.gemini.calories.domain.model.FoodAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnalysisUiState {
    object Idle : AnalysisUiState()
    object Loading : AnalysisUiState()
    data class Success(val result: FoodAnalysisResult) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analyzerRepository: FoodAnalyzerRepository,
    private val calorieRepository: CalorieRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AnalysisViewModel"
    }

    init {
        Log.d(TAG, "init")
    }

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri = _selectedImageUri.asStateFlow()

    fun onImageSelected(uri: Uri) {
        Log.d(TAG, "onImageSelected: $uri")
        _selectedImageUri.value = uri
        _uiState.value = AnalysisUiState.Idle
    }

    fun analyzeImage(imageData: ByteArray) {
        Log.d(TAG, "analyzeImage size=${imageData.size}")
        viewModelScope.launch {
            _uiState.value = AnalysisUiState.Loading
            Log.d(TAG, "state=Loading")
            analyzerRepository.analyze(imageData)
                .onSuccess { result ->
                    Log.d(TAG, "analysis success foods=${result.foods.size}")
                    _uiState.value = AnalysisUiState.Success(result)
                }
                .onFailure { error ->
                    Log.e(TAG, "analysis failure", error)
                    _uiState.value = AnalysisUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun saveResult(mealType: MealType) {
        val state = _uiState.value
        if (state is AnalysisUiState.Success) {
            viewModelScope.launch {
                calorieRepository.addFoodEntry(
                    result = state.result,
                    imagePath = _selectedImageUri.value?.toString(),
                    mealType = mealType
                )
                // Navigation back should be handled by UI event or one-time event flow
            }
        }
    }
    
    fun reset() {
        _uiState.value = AnalysisUiState.Idle
        _selectedImageUri.value = null
    }
}
