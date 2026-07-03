package com.example.domain

object CalorieEngine {

    /**
     * Mifflin-St Jeor Equation:
     * Male:   BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) + 5
     * Female: BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) - 161
     */
    fun calculateBmr(weightKg: Double, heightCm: Double, age: Int, gender: String): Double {
        val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
        return if (gender.lowercase() == "male") {
            base + 5.0
        } else {
            base - 161.0
        }
    }

    /**
     * TDEE Activity Multipliers:
     * Sedentary   → BMR × 1.2
     * Light       → BMR × 1.375
     * Moderate    → BMR × 1.55
     * Active      → BMR × 1.725
     * Very Active → BMR × 1.9
     */
    fun calculateTdee(bmr: Double, activityLevel: String): Double {
        val multiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2
            "light", "lightly active" -> 1.375
            "moderate", "moderately active" -> 1.55
            "active", "very active" -> 1.725
            "very active", "athlete" -> 1.9
            else -> 1.55 // default moderate
        }
        return bmr * multiplier
    }

    /**
     * Goal Adjustments:
     * Lose Weight   → TDEE - 500 kcal
     * Maintain      → TDEE
     * Gain Weight   → TDEE + 300 kcal
     * Enforces healthy minimum boundaries (1200 kcal/day for females, 1500 kcal/day for males).
     */
    fun calculateCalorieTarget(tdee: Double, goalType: String, gender: String): Double {
        val target = when (goalType.lowercase()) {
            "lose", "lose weight" -> tdee - 500.0
            "gain", "gain weight", "gain muscle" -> tdee + 300.0
            else -> tdee
        }
        val minTarget = if (gender.lowercase() == "male") 1500.0 else 1200.0
        return if (target < minTarget) minTarget else target
    }
}
