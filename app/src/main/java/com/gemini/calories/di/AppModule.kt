package com.gemini.calories.di

import android.content.Context
import androidx.room.Room
import com.gemini.calories.data.local.AppDatabase
import com.gemini.calories.data.local.FoodEntryDao
import com.gemini.calories.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_calories.db"
        ).build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideFoodEntryDao(database: AppDatabase): FoodEntryDao {
        return database.foodEntryDao()
    }
}
