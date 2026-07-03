package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── ENTITIES ─────────────────────────────────────────────────────────────

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,             // "YYYY-MM-DD"
    val mealCategory: String,     // "Breakfast" | "Lunch" | "Dinner" | "Snacks"
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val servingSize: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val weightKg: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val uid: String = "guest",
    val email: String,
    val displayName: String,
    val age: Int,
    val gender: String,
    val heightCm: Double,
    val weightKg: Double,
    val targetWeightKg: Double,
    val goalType: String,
    val activityLevel: String,
    val bmr: Double,
    val tdee: Double,
    val dailyCalorieTarget: Double,
    val dailyWaterGoalMl: Int,
    val onboardingComplete: Boolean
)

// ─── DAOS ─────────────────────────────────────────────────────────────────

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getFoodLogsByDate(date: String): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllFoodLogs(): Flow<List<FoodLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(log: FoodLogEntity)

    @Delete
    suspend fun deleteFoodLog(log: FoodLogEntity)

    @Query("SELECT SUM(calories) FROM food_logs WHERE date = :date")
    fun getCaloriesSumByDate(date: String): Flow<Double?>
}

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC")
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE date = :date LIMIT 1")
    suspend fun getWeightLogByDate(date: String): WeightLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLogEntity)
}

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    fun getWaterLogByDate(date: String): Flow<WaterLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    fun getUserProfile(uid: String = "guest"): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getUserProfileOnce(uid: String = "guest"): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)
}

// ─── DATABASE ─────────────────────────────────────────────────────────────

@Database(
    entities = [
        FoodLogEntity::class,
        WeightLogEntity::class,
        WaterLogEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NutriDatabase : RoomDatabase() {
    abstract fun foodLogDao(): FoodLogDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun userProfileDao(): UserProfileDao
}
