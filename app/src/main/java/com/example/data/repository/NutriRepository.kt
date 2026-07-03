package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NutriRepository(
    private val database: NutriDatabase
) {
    private val foodLogDao = database.foodLogDao()
    private val weightLogDao = database.weightLogDao()
    private val waterLogDao = database.waterLogDao()
    private val userProfileDao = database.userProfileDao()

    // ─── USER PROFILE ────────────────────────────────────────────────────────

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile().map { entity ->
        entity?.let {
            UserProfile(
                uid = it.uid,
                email = it.email,
                displayName = it.displayName,
                age = it.age,
                gender = it.gender,
                heightCm = it.heightCm,
                weightKg = it.weightKg,
                targetWeightKg = it.targetWeightKg,
                goalType = it.goalType,
                activityLevel = it.activityLevel,
                bmr = it.bmr,
                tdee = it.tdee,
                dailyCalorieTarget = it.dailyCalorieTarget,
                dailyWaterGoalMl = it.dailyWaterGoalMl,
                onboardingComplete = it.onboardingComplete
            )
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.insertUserProfile(
            UserProfileEntity(
                uid = profile.uid,
                email = profile.email,
                displayName = profile.displayName,
                age = profile.age,
                gender = profile.gender,
                heightCm = profile.heightCm,
                weightKg = profile.weightKg,
                targetWeightKg = profile.targetWeightKg,
                goalType = profile.goalType,
                activityLevel = profile.activityLevel,
                bmr = profile.bmr,
                tdee = profile.tdee,
                dailyCalorieTarget = profile.dailyCalorieTarget,
                dailyWaterGoalMl = profile.dailyWaterGoalMl,
                onboardingComplete = profile.onboardingComplete
            )
        )
    }

    suspend fun getUserProfileOnce(): UserProfile? {
        val entity = userProfileDao.getUserProfileOnce() ?: return null
        return UserProfile(
            uid = entity.uid,
            email = entity.email,
            displayName = entity.displayName,
            age = entity.age,
            gender = entity.gender,
            heightCm = entity.heightCm,
            weightKg = entity.weightKg,
            targetWeightKg = entity.targetWeightKg,
            goalType = entity.goalType,
            activityLevel = entity.activityLevel,
            bmr = entity.bmr,
            tdee = entity.tdee,
            dailyCalorieTarget = entity.dailyCalorieTarget,
            dailyWaterGoalMl = entity.dailyWaterGoalMl,
            onboardingComplete = entity.onboardingComplete
        )
    }

    // ─── FOOD LOGS ───────────────────────────────────────────────────────────

    fun getFoodLogsForDate(date: String): Flow<List<FoodLogEntity>> {
        return foodLogDao.getFoodLogsByDate(date)
    }

    fun getAllFoodLogs(): Flow<List<FoodLogEntity>> {
        return foodLogDao.getAllFoodLogs()
    }

    suspend fun addFoodLog(
        date: String,
        category: String,
        name: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        servingSize: String
    ) {
        foodLogDao.insertFoodLog(
            FoodLogEntity(
                date = date,
                mealCategory = category,
                foodName = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                servingSize = servingSize
            )
        )
    }

    suspend fun deleteFoodLog(log: FoodLogEntity) {
        foodLogDao.deleteFoodLog(log)
    }

    fun getCaloriesSum(date: String): Flow<Double?> {
        return foodLogDao.getCaloriesSumByDate(date)
    }

    // ─── WEIGHT LOGS ─────────────────────────────────────────────────────────

    fun getAllWeightLogs(): Flow<List<WeightLogEntity>> {
        return weightLogDao.getAllWeightLogs()
    }

    suspend fun addWeightLog(date: String, weightKg: Double) {
        weightLogDao.insertWeightLog(
            WeightLogEntity(date = date, weightKg = weightKg)
        )
        // Also update the current profile weight if logged
        val profile = getUserProfileOnce()
        if (profile != null) {
            saveUserProfile(profile.copy(weightKg = weightKg))
        }
    }

    // ─── WATER LOGS ──────────────────────────────────────────────────────────

    fun getWaterLog(date: String): Flow<WaterLogEntity?> {
        return waterLogDao.getWaterLogByDate(date)
    }

    suspend fun updateWaterLog(date: String, amountMl: Int) {
        waterLogDao.insertWaterLog(
            WaterLogEntity(date = date, amountMl = amountMl)
        )
    }
}
