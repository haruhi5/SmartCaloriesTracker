package com.gemini.calories.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface FoodEntryDao {
    
    @Query("SELECT * FROM food_entries WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getEntriesByDate(userId: Long, date: LocalDate): Flow<List<FoodEntryEntity>>
    
    @Query("SELECT * FROM food_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, timestamp DESC")
    fun getEntriesByDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<FoodEntryEntity>>
    
    @Query("SELECT SUM(calories) FROM food_entries WHERE userId = :userId AND date = :date")
    fun getTotalCaloriesByDate(userId: Long, date: LocalDate): Flow<Int?>
    
    @Query("SELECT date, SUM(calories) as totalCalories, SUM(protein) as totalProtein, SUM(carbs) as totalCarbs, SUM(fat) as totalFat FROM food_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate GROUP BY date ORDER BY date ASC")
    fun getDailyStats(userId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<DailyStats>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodEntryEntity): Long
    
    @Update
    suspend fun update(entry: FoodEntryEntity)
    
    @Delete
    suspend fun delete(entry: FoodEntryEntity)
    
    @Query("SELECT * FROM food_entries WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllEntriesForExport(userId: Long): List<FoodEntryEntity>
}

data class DailyStats(
    val date: LocalDate,
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float
)
