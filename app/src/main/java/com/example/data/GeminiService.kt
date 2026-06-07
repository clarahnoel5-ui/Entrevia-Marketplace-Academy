package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    // System instruction to guide the tutor chatbot's voice
    private const val SYSTEM_PROMPT = """
        You are 'Entrevia AI Business Coach', a highly compassionate, professional, and world-class e-commerce growth expert.
        Your mission is to help beginners, especially busy parents, shy entrepreneurs, and newcomers start and scale automated online businesses with premium confidence.
        Do not use overwhelming technical jargon. Keep advice super welcoming, broken down into numbered steps, actionable, and full of positive encouragement.
        Help the user brainstorm physical/digital products, write copy, structure marketing titles, understand Amazon/TikTok seller guidelines, or compose complete 1-page business plans.
        Address them with warm professional respect, acknowledging that taking the first step as an entrepreneur is extremely brave.
    """

    suspend fun generateResponse(userPrompt: String, chatHistory: List<Pair<String, Boolean>> = emptyList()): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is placeholder or empty!")
            return@withContext "I want to assist you, but the Gemini API Key is missing. Please add a valid API key into the 'Secrets' panel in Google AI Studio to enable the Entrevia AI chatbot coach!"
        }

        try {
            val requestJson = JSONObject()

            // 1. System instructions
            val systemCol = JSONObject()
            val partsArr = JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT))
            systemCol.put("parts", partsArr)
            requestJson.put("systemInstruction", systemCol)

            // 2. Chat Contents (including history)
            val contentsArray = JSONArray()

            // Map history turns
            chatHistory.forEach { (text, isUser) ->
                val turnObj = JSONObject()
                turnObj.put("role", if (isUser) "user" else "model")
                val turnParts = JSONArray().put(JSONObject().put("text", text))
                turnObj.put("parts", turnParts)
                contentsArray.put(turnObj)
            }

            // Put current user prompt
            val currentUserTurn = JSONObject()
            currentUserTurn.put("role", "user")
            val currentParts = JSONArray().put(JSONObject().put("text", userPrompt))
            currentUserTurn.put("parts", currentParts)
            contentsArray.put(currentUserTurn)

            requestJson.put("contents", contentsArray)

            // Optional: Lower temperature for professional business facts
            val config = JSONObject()
            config.put("temperature", 0.7)
            requestJson.put("generationConfig", config)

            val requestBodyString = requestJson.toString()
            val requestBody = requestBodyString.toRequestBody(mediaTypeJson)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "Unsuccessful response from Gemini. Code: $code, Error: $errorBody")
                    
                    return@withContext "I apologize, but my server received an error ($code). Let's try re-prompting or check if you have a valid Internet connection!"
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext "I received an empty business memo response. Please try rephrasing your marketplace question!"
                }

                // Parse the response
                val rootJson = JSONObject(responseBody)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val firstPart = parts.getJSONObject(0)
                            val textResult = firstPart.optString("text", "")
                            if (textResult.isNotEmpty()) {
                                return@withContext textResult
                            }
                        }
                    }
                }
                
                return@withContext "I heard your query, but could not formulate a constructive answer. Try matching a more specific topic like 'Amazon private label'!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content: ${e.message}", e)
            return@withContext "Connection Issue: ${e.localizedMessage ?: "Unable to contact the AI Coach servers."}. Please verify your network and try again!"
        }
    }
}
