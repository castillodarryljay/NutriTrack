package com.example.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NutriTrackApp
import com.example.data.local.FoodLogEntity
import com.example.data.local.WaterLogEntity
import com.example.data.local.WeightLogEntity
import com.example.data.model.*
import com.example.data.remote.GeminiApiService
import com.example.domain.CalorieEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NutriViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as NutriTrackApp).repository
    private val geminiService = GeminiApiService()

    private val _currentDate = MutableStateFlow(getTodayDateString())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // ─── AUTH STATE ──────────────────────────────────────────────────────────

    private val sharedPrefs = application.getSharedPreferences("nutri_track_prefs", android.content.Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun login(email: String, pass: String): Boolean {
        if (email.contains("@") && pass.length >= 6) {
            _isLoggedIn.value = true
            sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
            _authError.value = null
            viewModelScope.launch {
                // Pre-populate with default if none exists
                val existing = repository.getUserProfileOnce()
                if (existing == null) {
                    repository.saveUserProfile(UserProfile(email = email, displayName = email.substringBefore("@")))
                }
            }
            return true
        } else {
            _authError.value = "Invalid email or password (min 6 characters)."
            return false
        }
    }

    fun register(name: String, email: String, pass: String): Boolean {
        if (name.isNotBlank() && email.contains("@") && pass.length >= 6) {
            _isLoggedIn.value = true
            sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
            _authError.value = null
            viewModelScope.launch {
                repository.saveUserProfile(UserProfile(email = email, displayName = name))
            }
            return true
        } else {
            _authError.value = "Please fill in all fields (password min 6 characters)."
            return false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
    }

    // ─── USER PROFILE ────────────────────────────────────────────────────────

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile(onboardingComplete = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile(onboardingComplete = false))

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun completeOnboarding(
        gender: String,
        age: Int,
        height: Double,
        weight: Double,
        targetWeight: Double,
        goal: String,
        activity: String
    ) {
        val bmr = CalorieEngine.calculateBmr(weight, height, age, gender)
        val tdee = CalorieEngine.calculateTdee(bmr, activity)
        val targetCalories = CalorieEngine.calculateCalorieTarget(tdee, goal, gender)

        val updatedProfile = UserProfile(
            uid = "guest",
            email = userProfile.value.email,
            displayName = userProfile.value.displayName.ifBlank { "Nutri Member" },
            age = age,
            gender = gender,
            heightCm = height,
            weightKg = weight,
            targetWeightKg = targetWeight,
            goalType = goal,
            activityLevel = activity,
            bmr = bmr,
            tdee = tdee,
            dailyCalorieTarget = targetCalories,
            dailyWaterGoalMl = 2500,
            onboardingComplete = true
        )

        updateProfile(updatedProfile)
    }

    // ─── MEALS & FOOD LOGGING ────────────────────────────────────────────────

    val todayFoodLogs: StateFlow<List<FoodLogEntity>> = _currentDate
        .flatMapLatest { date -> repository.getFoodLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCaloriesConsumed: StateFlow<Double> = todayFoodLogs
        .map { logs -> logs.sumOf { it.calories } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalProteinConsumed: StateFlow<Double> = todayFoodLogs
        .map { logs -> logs.sumOf { it.protein } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCarbsConsumed: StateFlow<Double> = todayFoodLogs
        .map { logs -> logs.sumOf { it.carbs } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalFatConsumed: StateFlow<Double> = todayFoodLogs
        .map { logs -> logs.sumOf { it.fat } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun logFoodItem(
        category: String,
        name: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        servingSize: String
    ) {
        viewModelScope.launch {
            repository.addFoodLog(
                date = _currentDate.value,
                category = category,
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                servingSize = servingSize
            )
        }
    }

    fun removeFoodLog(log: FoodLogEntity) {
        viewModelScope.launch {
            repository.deleteFoodLog(log)
        }
    }

    // ─── WEIGHT TRACKING ─────────────────────────────────────────────────────

    val weightHistory: StateFlow<List<WeightLogEntity>> = repository.getAllWeightLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun logWeight(weight: Double) {
        viewModelScope.launch {
            repository.addWeightLog(_currentDate.value, weight)
        }
    }

    // ─── WATER TRACKING ──────────────────────────────────────────────────────

    val todayWaterIntake: StateFlow<Int> = _currentDate
        .flatMapLatest { date -> repository.getWaterLog(date) }
        .map { it?.amountMl ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val current = todayWaterIntake.value
            repository.updateWaterLog(_currentDate.value, current + amountMl)
        }
    }

    fun resetWater() {
        viewModelScope.launch {
            repository.updateWaterLog(_currentDate.value, 0)
        }
    }

    // ─── AI FOOD SCANNER STATE ───────────────────────────────────────────────

    private val _scannerLoading = MutableStateFlow(false)
    val scannerLoading: StateFlow<Boolean> = _scannerLoading.asStateFlow()

    private val _scanResult = MutableStateFlow<FoodScanResult?>(null)
    val scanResult: StateFlow<FoodScanResult?> = _scanResult.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    fun scanFoodBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _scannerLoading.value = true
            _scanError.value = null
            _scanResult.value = null

            val result = geminiService.scanFoodImage(bitmap)
            result.onSuccess {
                _scanResult.value = it
            }.onFailure {
                _scanError.value = it.message ?: "Could not scan food image."
                Log.e("ScanFood", "Scan failed", it)
            }
            _scannerLoading.value = false
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
        _scanError.value = null
    }

    // ─── AI DIET CHAT STATE ──────────────────────────────────────────────────

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "Hello! I'm NutriBot 🌿, your AI nutritionist assistant. Ask me anything about diet, weight loss, macros, or local Filipino meals!",
                role = MessageRole.ASSISTANT
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    fun sendChatMessage(content: String) {
        if (content.isBlank()) return

        val userMsg = ChatMessage(content = content, role = MessageRole.USER)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _chatLoading.value = true
            val profile = userProfile.value
            val history = _chatMessages.value

            val response = geminiService.sendChatMessage(content, profile, history)
            response.onSuccess {
                val assistantMsg = ChatMessage(content = it, role = MessageRole.ASSISTANT)
                _chatMessages.value = _chatMessages.value + assistantMsg
            }.onFailure {
                val errorMsg = ChatMessage(
                    content = "Sorry, I had trouble reaching the servers: ${it.message ?: "Please check your connection and try again."}",
                    role = MessageRole.ASSISTANT
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            }
            _chatLoading.value = false
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                content = "Chat cleared! I'm ready to help you plan your next healthy meal. Ask me anything! 🌿",
                role = MessageRole.ASSISTANT
            )
        )
    }

    // ─── INTERMITTENT FASTING TIMER STATE ────────────────────────────────────

    private val _isFasting = MutableStateFlow(false)
    val isFasting: StateFlow<Boolean> = _isFasting.asStateFlow()

    private val _fastingProtocol = MutableStateFlow("16:8") // "16:8", "18:6", "20:4"
    val fastingProtocol: StateFlow<String> = _fastingProtocol.asStateFlow()

    private val _fastingStartTime = MutableStateFlow<Long?>(null)
    val fastingStartTime: StateFlow<Long?> = _fastingStartTime.asStateFlow()

    fun toggleFasting() {
        if (_isFasting.value) {
            _isFasting.value = false
            _fastingStartTime.value = null
        } else {
            _isFasting.value = true
            _fastingStartTime.value = System.currentTimeMillis()
        }
    }

    fun changeFastingProtocol(protocol: String) {
        _fastingProtocol.value = protocol
    }

    // ─── DAILY STEPS SIMULATION ──────────────────────────────────────────────

    private val _stepsCount = MutableStateFlow(4230)
    val stepsCount: StateFlow<Int> = _stepsCount.asStateFlow()

    fun addSteps(amount: Int) {
        _stepsCount.value = _stepsCount.value + amount
    }

    fun getCaloriesBurnedFromSteps(): Double {
        return _stepsCount.value * 0.04
    }

    // ─── HELPER FUNCTIONS ────────────────────────────────────────────────────

    fun changeSelectedDate(dateString: String) {
        _currentDate.value = dateString
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    init {
        // Preload guest stats in Room database so UI is instantly responsive
        viewModelScope.launch {
            val existing = repository.getUserProfileOnce()
            if (existing == null) {
                repository.saveUserProfile(UserProfile())
            }
            // Seed a starting weight log for chart demo if empty
            val logs = repository.getAllWeightLogs().first()
            if (logs.isEmpty()) {
                val cal = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                for (i in 6 downTo 0) {
                    cal.time = Date()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    val dateStr = sdf.format(cal.time)
                    repository.addWeightLog(dateStr, 72.5 - (6 - i) * 0.4)
                }
            }
        }
    }
}
