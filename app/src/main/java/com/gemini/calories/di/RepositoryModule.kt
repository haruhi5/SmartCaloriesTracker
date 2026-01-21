package com.gemini.calories.di

import com.gemini.calories.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    // SettingsRepository is already provided by @Inject constructor, 
    // but we might need to bind interfaces if we had one.
    // For now, this module is a placeholder for future repositories.
}
