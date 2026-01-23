package com.gemini.calories.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_API_TYPE = stringPreferencesKey("api_type")
        val KEY_API_KEY = stringPreferencesKey("api_key")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    val apiType: Flow<String> = dataStore.data.map { it[KEY_API_TYPE] ?: "gpt" }
    val apiKey: Flow<String> = dataStore.data.map { it[KEY_API_KEY] ?: "" }
    val geminiApiKey: Flow<String> = dataStore.data.map { it[KEY_GEMINI_API_KEY] ?: "" }
    val themeMode: Flow<String> = dataStore.data.map { it[KEY_THEME_MODE] ?: "system" }
    val unitSystem: Flow<String> = dataStore.data.map { it[KEY_UNIT_SYSTEM] ?: "metric" }
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { it[KEY_FIRST_LAUNCH] ?: true }

    suspend fun setApiType(type: String) {
        dataStore.edit { it[KEY_API_TYPE] = type }
    }

    suspend fun setApiKey(key: String) {
        dataStore.edit { it[KEY_API_KEY] = key }
    }
    
    suspend fun setGeminiApiKey(key: String) {
        dataStore.edit { it[KEY_GEMINI_API_KEY] = key }
    }
    
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME_MODE] = mode }
    }
    
    suspend fun setUnitSystem(system: String) {
        dataStore.edit { it[KEY_UNIT_SYSTEM] = system }
    }

    suspend fun completeOnboarding() {
        dataStore.edit { it[KEY_FIRST_LAUNCH] = false }
    }
}
