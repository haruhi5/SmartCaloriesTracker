package com.gemini.calories.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()
    
    @TypeConverter
    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
    
    @TypeConverter
    fun fromGender(gender: Gender): String = gender.name
    
    @TypeConverter
    fun toGender(value: String): Gender = Gender.valueOf(value)

    @TypeConverter
    fun fromActivityLevel(level: ActivityLevel): String = level.name
    
    @TypeConverter
    fun toActivityLevel(value: String): ActivityLevel = ActivityLevel.valueOf(value)

    @TypeConverter
    fun fromFitnessGoal(goal: FitnessGoal): String = goal.name
    
    @TypeConverter
    fun toFitnessGoal(value: String): FitnessGoal = FitnessGoal.valueOf(value)

    @TypeConverter
    fun fromMealType(type: MealType): String = type.name
    
    @TypeConverter
    fun toMealType(value: String): MealType = MealType.valueOf(value)
}

@Database(entities = [UserEntity::class, FoodEntryEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodEntryDao(): FoodEntryDao
}
