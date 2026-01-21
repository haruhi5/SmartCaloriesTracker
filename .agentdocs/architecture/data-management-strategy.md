# 数据管理策略 (Data Management Strategy)

## 概述

Smart Calories Tracker 采用 **Room + DataStore** 双存储方案，Room 负责结构化数据（用户信息、食物记录），DataStore 负责轻量级设置项。所有数据均存储在本地，支持完整的 CSV 导出功能。

## 1. 数据库架构 (Room Database)

### 1.1 Entity 设计

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val height: Float,          // cm
    val weight: Float,          // kg
    val age: Int,
    val gender: Gender,
    val activityLevel: ActivityLevel,
    val goal: FitnessGoal,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class Gender { MALE, FEMALE }
enum class ActivityLevel { SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE }
enum class FitnessGoal { MAINTAIN, MUSCLE_GAIN, FAT_LOSS }

@Entity(
    tableName = "food_entries",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("date")]
)
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val foodName: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val portion: String,
    val mealType: MealType,
    val imagePath: String?,
    val date: LocalDate,        // 用于按日期分组
    val timestamp: Long = System.currentTimeMillis(),
    val isManualEntry: Boolean = false,
    val aiConfidence: Float? = null
)

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }
```

### 1.2 DAO 设计

```kotlin
@Dao
interface FoodEntryDao {
    
    @Query("SELECT * FROM food_entries WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getEntriesByDate(userId: Long, date: LocalDate): Flow<List<FoodEntryEntity>>
    
    @Query("SELECT * FROM food_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, timestamp DESC")
    fun getEntriesByDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<FoodEntryEntity>>
    
    @Query("SELECT SUM(calories) FROM food_entries WHERE userId = :userId AND date = :date")
    fun getTotalCaloriesByDate(userId: Long, date: LocalDate): Flow<Int?>
    
    @Query("""
        SELECT date, SUM(calories) as totalCalories, SUM(protein) as totalProtein, 
               SUM(carbs) as totalCarbs, SUM(fat) as totalFat
        FROM food_entries 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
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
```

### 1.3 TypeConverters

```kotlin
class Converters {
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()
    
    @TypeConverter
    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
    
    @TypeConverter
    fun fromGender(gender: Gender): String = gender.name
    
    @TypeConverter
    fun toGender(value: String): Gender = Gender.valueOf(value)
    
    // 其他枚举类型转换...
}
```

## 2. TDEE 计算逻辑

### 2.1 BMR 计算 (Mifflin-St Jeor 公式)

```kotlin
object TdeeCalculator {
    
    /**
     * 计算基础代谢率 (BMR)
     * 男性: BMR = 10 × 体重(kg) + 6.25 × 身高(cm) - 5 × 年龄 + 5
     * 女性: BMR = 10 × 体重(kg) + 6.25 × 身高(cm) - 5 × 年龄 - 161
     */
    fun calculateBmr(weight: Float, height: Float, age: Int, gender: Gender): Float {
        val base = 10 * weight + 6.25f * height - 5 * age
        return when (gender) {
            Gender.MALE -> base + 5
            Gender.FEMALE -> base - 161
        }
    }
    
    /**
     * 计算每日总能量消耗 (TDEE)
     * TDEE = BMR × 活动因子
     */
    fun calculateTdee(bmr: Float, activityLevel: ActivityLevel): Float {
        val factor = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2f      // 久坐不动
            ActivityLevel.LIGHT -> 1.375f        // 轻度活动 (每周1-3天)
            ActivityLevel.MODERATE -> 1.55f      // 中度活动 (每周3-5天)
            ActivityLevel.ACTIVE -> 1.725f       // 高度活动 (每周6-7天)
            ActivityLevel.VERY_ACTIVE -> 1.9f    // 剧烈活动 (体力劳动/双倍训练)
        }
        return bmr * factor
    }
    
    /**
     * 根据目标计算建议摄入量
     */
    fun calculateTargetCalories(tdee: Float, goal: FitnessGoal): Int {
        val adjustment = when (goal) {
            FitnessGoal.MAINTAIN -> 0
            FitnessGoal.MUSCLE_GAIN -> 300       // 热量盈余 300 kcal
            FitnessGoal.FAT_LOSS -> -500         // 热量赤字 500 kcal
        }
        return (tdee + adjustment).toInt()
    }
}
```

## 3. Repository 层设计

```kotlin
class CalorieRepository @Inject constructor(
    private val foodEntryDao: FoodEntryDao,
    private val userDao: UserDao
) {
    
    fun getTodayEntries(userId: Long): Flow<List<FoodEntry>> {
        val today = LocalDate.now()
        return foodEntryDao.getEntriesByDate(userId, today)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getTodayCalories(userId: Long): Flow<Int> {
        val today = LocalDate.now()
        return foodEntryDao.getTotalCaloriesByDate(userId, today)
            .map { it ?: 0 }
    }
    
    fun getWeeklyStats(userId: Long): Flow<List<DailyStats>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6)
        return foodEntryDao.getDailyStats(userId, startDate, endDate)
    }
    
    suspend fun addFoodEntry(
        userId: Long,
        analysisResult: FoodAnalysisResult,
        imagePath: String?,
        mealType: MealType
    ) {
        analysisResult.foods.forEach { food ->
            foodEntryDao.insert(
                FoodEntryEntity(
                    userId = userId,
                    foodName = food.name,
                    calories = food.calories,
                    protein = food.protein,
                    carbs = food.carbs,
                    fat = food.fat,
                    portion = food.portion,
                    mealType = mealType,
                    imagePath = imagePath,
                    date = LocalDate.now(),
                    aiConfidence = food.confidence
                )
            )
        }
    }
}
```

## 4. CSV 导出功能

### 4.1 导出格式

```csv
Date,Time,Meal Type,Food Name,Calories,Protein(g),Carbs(g),Fat(g),Portion
2024-01-15,08:30,Breakfast,Oatmeal with Banana,350,12.5,58.0,8.2,1 bowl
2024-01-15,12:00,Lunch,Grilled Chicken Salad,420,35.0,15.0,22.0,1 plate
2024-01-15,19:00,Dinner,Pasta with Tomato Sauce,550,18.0,85.0,12.0,1 serving
```

### 4.2 导出实现

```kotlin
class CsvExportManager @Inject constructor(
    private val foodEntryDao: FoodEntryDao,
    private val context: Context
) {
    
    suspend fun exportToFile(userId: Long): Result<Uri> {
        return withContext(Dispatchers.IO) {
            try {
                val entries = foodEntryDao.getAllEntriesForExport(userId)
                val csvContent = buildCsvContent(entries)
                
                val fileName = "calories_export_${LocalDate.now()}.csv"
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                file.writeText(csvContent)
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                Result.success(uri)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun buildCsvContent(entries: List<FoodEntryEntity>): String {
        val header = "Date,Time,Meal Type,Food Name,Calories,Protein(g),Carbs(g),Fat(g),Portion\n"
        val rows = entries.joinToString("\n") { entry ->
            val dateTime = Instant.ofEpochMilli(entry.timestamp)
                .atZone(ZoneId.systemDefault())
            "${entry.date}," +
            "${dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))}," +
            "${entry.mealType}," +
            "\"${entry.foodName.replace("\"", "\"\"")}\"," +
            "${entry.calories}," +
            "${entry.protein}," +
            "${entry.carbs}," +
            "${entry.fat}," +
            "\"${entry.portion.replace("\"", "\"\"")}\""
        }
        return header + rows
    }
    
    fun shareFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Calories Data"))
    }
}
```

## 5. 数据同步与备份策略

### 5.1 自动备份

```kotlin
class BackupManager @Inject constructor(
    private val database: AppDatabase,
    private val context: Context
) {
    
    suspend fun createBackup(): File {
        // 关闭 WAL 模式确保数据完整
        database.query("PRAGMA wal_checkpoint(FULL)", null)
        
        val dbFile = context.getDatabasePath("calories_tracker.db")
        val backupFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "backup_${System.currentTimeMillis()}.db"
        )
        
        dbFile.copyTo(backupFile, overwrite = true)
        return backupFile
    }
    
    suspend fun restoreFromBackup(backupFile: File) {
        database.close()
        val dbFile = context.getDatabasePath("calories_tracker.db")
        backupFile.copyTo(dbFile, overwrite = true)
        // 重新打开数据库
    }
}
```

## 6. 性能优化

1. **分页加载**: 历史记录使用 Paging 3 库实现懒加载。
2. **索引优化**: 在 `date` 和 `userId` 字段建立索引加速查询。
3. **Flow 响应式**: 所有查询返回 Flow，UI 自动响应数据变化。
4. **协程调度**: 数据库操作在 `Dispatchers.IO` 执行，避免阻塞主线程。
