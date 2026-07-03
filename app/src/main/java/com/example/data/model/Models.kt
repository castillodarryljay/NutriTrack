package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class UserProfile(
    val uid: String = "guest",
    val email: String = "guest@example.com",
    val displayName: String = "Fit Guest",
    val age: Int = 28,
    val gender: String = "Male",          // "Male" | "Female"
    val heightCm: Double = 170.0,
    val weightKg: Double = 70.0,
    val targetWeightKg: Double = 65.0,
    val goalType: String = "Lose",        // "Lose" | "Maintain" | "Gain"
    val activityLevel: String = "Moderate",   // "Sedentary" | "Light" | "Moderate" | "Active" | "Very Active"
    val bmr: Double = 1630.0,
    val tdee: Double = 2240.0,
    val dailyCalorieTarget: Double = 1740.0,
    val dailyWaterGoalMl: Int = 2500,
    val onboardingComplete: Boolean = false
)

@JsonClass(generateAdapter = true)
data class FoodScanResult(
    val foods: List<FoodItem>,
    @Json(name = "total_calories") val totalCalories: Double,
    @Json(name = "total_protein") val totalProtein: Double,
    @Json(name = "total_carbs") val totalCarbs: Double,
    @Json(name = "total_fat") val totalFat: Double,
    val confidence: Float,
    val notes: String = ""
)

@JsonClass(generateAdapter = true)
data class FoodItem(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    @Json(name = "serving_estimate") val servingEstimate: String = "1 serving",
    val emoji: String = "🍽️"
)

enum class MessageRole {
    @Json(name = "USER") USER,
    @Json(name = "ASSISTANT") ASSISTANT
}

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)

data class DailyLogSummary(
    val date: String,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val waterIntakeMl: Int,
    val weightKg: Double?
)
