package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// ─── GEMINI REST DATA CLASSES (MOSHI COMPATIBLE) ─────────────────────────

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class PartResponse(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ContentResponse(
    val parts: List<PartResponse>
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: ContentResponse
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

// ─── RETROFIT SERVICE INTERFACE ───────────────────────────────────────────

interface RetrofitGeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// ─── GEMINI API SERVICE WRAPPER ───────────────────────────────────────────

class GeminiApiService {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(RetrofitGeminiService::class.java)

    /**
     * Converts a bitmap image to base64 representation.
     */
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap to max 1024 to optimize payload
        val maxDim = 1024
        val scaledBitmap = if (width > maxDim || height > maxDim) {
            val ratio = width.toFloat() / height.toFloat()
            val (newWidth, newHeight) = if (ratio > 1.0) {
                maxDim to (maxDim / ratio).toInt()
            } else {
                (maxDim * ratio).toInt() to maxDim
            }
            Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
        } else {
            this
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * ─── FOOD IMAGE SCANNER ───
     * Scans an image of food and returns nutritional breakdown as a FoodScanResult.
     */
    suspend fun scanFoodImage(imageBitmap: Bitmap): Result<FoodScanResult> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return Result.failure(Exception("Gemini API key is missing! Please configure it in AI Studio Secrets panel."))
        }

        val prompt = """
        You are an expert nutritionist and AI food analyst.
        Analyze the food image provided and identify ALL visible food items.
        
        Return ONLY a valid JSON object matching the schema below. Do NOT wrap in markdown blocks like ```json ... ```. No extra explanations.
        
        JSON schema:
        {
          "foods": [
            {
              "name": "English name of the food item",
              "calories": 150.0,
              "protein": 5.0,
              "carbs": 20.0,
              "fat": 3.2,
              "serving_estimate": "e.g. 1 cup, 200g, 1 piece",
              "emoji": "🍔"
            }
          ],
          "total_calories": 150.0,
          "total_protein": 5.0,
          "total_carbs": 20.0,
          "total_fat": 3.2,
          "confidence": 0.95,
          "notes": "Any brief dietary tips or warnings"
        }

        Rules:
        - Calorie values must be in kcal, and macros in grams.
        - If a Filipino dish is detected (e.g. Adobo, Sinigang, Pandesal), use its common Filipino name.
        - Be realistic with portion sizes based on visual context.
        """.trimIndent()

        val base64Image = try {
            imageBitmap.toBase64()
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to process image: ${e.message}"))
        }

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("AI returned an empty response."))

            Log.d("GeminiScan", "Raw Response: $jsonText")

            // Parse response safely using Moshi
            val cleanJson = cleanJsonString(jsonText)
            val adapter = moshi.adapter(FoodScanResult::class.java)
            val result = adapter.fromJson(cleanJson)
                ?: return Result.failure(Exception("Failed to parse nutrition JSON."))
            
            Result.success(result)
        } catch (e: Exception) {
            Log.e("GeminiScan", "Scan failed", e)
            Result.failure(e)
        }
    }

    /**
     * ─── DIET CHATBOT ───
     * Chats with the user based on their current user profile and chat history.
     */
    suspend fun sendChatMessage(
        userMessage: String,
        userProfile: UserProfile,
        chatHistory: List<ChatMessage>
    ): Result<String> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return Result.failure(Exception("Gemini API key is missing! Please configure it in AI Studio Secrets panel."))
        }

        val systemPrompt = """
        You are NutriBot 🌿, a warm, science-based expert AI diet assistant in the NutriTrack AI app.
        
        USER PROFILE:
        - Name: ${userProfile.displayName}
        - Age: ${userProfile.age}
        - Gender: ${userProfile.gender}
        - Current Weight: ${userProfile.weightKg} kg
        - Target Weight: ${userProfile.targetWeightKg} kg
        - Height: ${userProfile.heightCm} cm
        - Goal: ${userProfile.goalType} weight
        - Daily Calorie Target: ${userProfile.dailyCalorieTarget.toInt()} kcal
        - Activity Level: ${userProfile.activityLevel}

        YOUR ROLE:
        - Provide personalized, encouraging nutritional guidance.
        - Understand Filipino cuisine (Adobo, Sinigang, Pancit, Lumpia, Halo-Halo, Pandesal, street foods) and recommend local food choices.
        - Give clear macro breakdowns (protein, carbs, fat) and cost-effective tips.
        - Keep answers friendly, concise (under 150 words), and structured with bullet points.
        - Never provide medical diagnoses. Suggest consulting a doctor for medical issues.
        """.trimIndent()

        // Construct history contents
        val contents = mutableListOf<Content>()
        // Add recent history limit to last 10 messages to save context tokens
        chatHistory.takeLast(10).forEach { msg ->
            val roleName = if (msg.role == MessageRole.USER) "user" else "model"
            contents.add(
                Content(
                    parts = listOf(Part(text = msg.content)),
                    role = roleName
                )
            )
        }
        // Add the current user message at the end
        contents.add(Content(parts = listOf(Part(text = userMessage)), role = "user"))

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val aiText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("NutriBot had no response. Please try again!"))
            Result.success(aiText.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper to clean up any leading/trailing markdown JSON characters.
     */
    private fun cleanJsonString(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```")) {
            // Remove markdown code block wrappers
            clean = clean.replace(Regex("^```[a-zA-Z]*"), "")
            clean = clean.replace(Regex("```$"), "")
        }
        return clean.trim()
    }
}
