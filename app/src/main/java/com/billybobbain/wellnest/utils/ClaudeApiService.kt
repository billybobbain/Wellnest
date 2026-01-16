package com.billybobbain.wellnest.utils

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Service for calling Claude API to clarify garbled text messages.
 * Uses Claude Haiku for cost efficiency (~$0.0002 per message).
 *
 * Privacy: User's API key is used, so they control their data and costs.
 */
object ClaudeApiService {
    private const val CLAUDE_API_URL = "https://api.anthropic.com/v1/messages"
    private const val CLAUDE_VERSION = "2023-06-01"
    private const val MODEL = "claude-3-haiku-20240307" // Cost-efficient model

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Serializable
    data class ClaudeRequest(
        val model: String,
        val max_tokens: Int,
        val messages: List<Message>
    )

    @Serializable
    data class Message(
        val role: String,
        val content: String
    )

    @Serializable
    data class ClaudeResponse(
        val content: List<Content>,
        val stop_reason: String? = null
    )

    @Serializable
    data class Content(
        val text: String
    )

    @Serializable
    data class ErrorResponse(
        val error: ErrorDetail
    )

    @Serializable
    data class ErrorDetail(
        val type: String,
        val message: String
    )

    /**
     * Clarify a garbled message using Claude API.
     *
     * @param originalText The garbled/fragmented text to clarify
     * @param apiKey User's Claude API key
     * @return Clarified text, or throws exception on error
     */
    suspend fun clarifyMessage(originalText: String, apiKey: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    This is a text message from an elderly person with vision problems. The message has spacing and typing errors but conveys important information.

                    Your task: Rewrite the message below to fix typos and spacing while preserving their natural voice and tone.

                    CRITICAL: Output ONLY the corrected message text. Do not include any preamble, commentary, explanation, or phrases like "Here's what they're saying" or "Let me clarify". Just output the fixed message as if you were the person sending it.

                    Original message:
                    $originalText
                """.trimIndent()

                val requestBody = ClaudeRequest(
                    model = MODEL,
                    max_tokens = 1024,
                    messages = listOf(
                        Message(
                            role = "user",
                            content = prompt
                        )
                    )
                )

                val jsonBody = json.encodeToString(requestBody)
                val request = Request.Builder()
                    .url(CLAUDE_API_URL)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", CLAUDE_VERSION)
                    .header("content-type", "application/json")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    val errorMessage = try {
                        val errorResponse = json.decodeFromString<ErrorResponse>(responseBody ?: "")
                        errorResponse.error.message
                    } catch (e: Exception) {
                        "API request failed with code ${response.code}"
                    }
                    return@withContext Result.failure(Exception(errorMessage))
                }

                if (responseBody == null) {
                    return@withContext Result.failure(Exception("Empty response from API"))
                }

                val claudeResponse = json.decodeFromString<ClaudeResponse>(responseBody)
                val clarifiedText = claudeResponse.content.firstOrNull()?.text
                    ?: return@withContext Result.failure(Exception("No content in response"))

                Result.success(clarifiedText)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to clarify message: ${e.message}"))
            }
        }
    }

    /**
     * Test if an API key is valid by making a minimal API call.
     *
     * @param apiKey The API key to test
     * @return Result with success or error message
     */
    suspend fun testApiKey(apiKey: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = ClaudeRequest(
                    model = MODEL,
                    max_tokens = 10,
                    messages = listOf(
                        Message(
                            role = "user",
                            content = "Hi"
                        )
                    )
                )

                val jsonBody = json.encodeToString(requestBody)
                val request = Request.Builder()
                    .url(CLAUDE_API_URL)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", CLAUDE_VERSION)
                    .header("content-type", "application/json")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Result.success("API key is valid!")
                } else {
                    val errorBody = response.body?.string()
                    val errorMessage = try {
                        val errorResponse = json.decodeFromString<ErrorResponse>(errorBody ?: "")
                        errorResponse.error.message
                    } catch (e: Exception) {
                        "Invalid API key or network error (code ${response.code})"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Connection failed: ${e.message}"))
            }
        }
    }

    /**
     * Extract medication information from an image using Claude's vision capabilities.
     *
     * @param imageFile The image file containing medication list
     * @param apiKey User's Claude API key
     * @return JSON string with extracted medication data
     */
    suspend fun extractMedicationsFromImage(imageFile: File, apiKey: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Read and encode image
                val imageBytes = imageFile.readBytes()
                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                // Determine media type
                val mediaType = when (imageFile.extension.lowercase()) {
                    "png" -> "image/png"
                    "jpg", "jpeg" -> "image/jpeg"
                    "gif" -> "image/gif"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }

                val prompt = """
                    Extract ALL medications from this image. This is a medication list printout.

                    Return ONLY a valid JSON array of medication objects. Each object should have these fields:
                    - drugName (string, required): The medication name
                    - dosage (string, optional): Strength and form (e.g., "10mg tablet")
                    - frequency (string, optional): How often to take (e.g., "twice daily")
                    - prescribingDoctor (string, optional): Doctor's name
                    - diagnosis (string, optional): What it's for/indication
                    - notes (string, optional): Any additional instructions or notes

                    CRITICAL: Output ONLY the JSON array, no preamble, no explanation, no markdown code blocks.

                    Example format:
                    [{"drugName":"Aspirin","dosage":"81mg tablet","frequency":"once daily","prescribingDoctor":"Smith","diagnosis":"Heart health","notes":"Take with food"}]
                """.trimIndent()

                // Build request JSON manually for vision
                val requestJson = JsonObject(mapOf(
                    "model" to JsonPrimitive(MODEL),
                    "max_tokens" to JsonPrimitive(2048),
                    "messages" to JsonArray(listOf(
                        JsonObject(mapOf(
                            "role" to JsonPrimitive("user"),
                            "content" to JsonArray(listOf(
                                JsonObject(mapOf(
                                    "type" to JsonPrimitive("image"),
                                    "source" to JsonObject(mapOf(
                                        "type" to JsonPrimitive("base64"),
                                        "media_type" to JsonPrimitive(mediaType),
                                        "data" to JsonPrimitive(base64Image)
                                    ))
                                )),
                                JsonObject(mapOf(
                                    "type" to JsonPrimitive("text"),
                                    "text" to JsonPrimitive(prompt)
                                ))
                            ))
                        ))
                    ))
                ))

                val jsonBody = requestJson.toString()
                val request = Request.Builder()
                    .url(CLAUDE_API_URL)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", CLAUDE_VERSION)
                    .header("content-type", "application/json")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    val errorMessage = try {
                        val errorResponse = json.decodeFromString<ErrorResponse>(responseBody ?: "")
                        errorResponse.error.message
                    } catch (e: Exception) {
                        "API request failed with code ${response.code}"
                    }
                    return@withContext Result.failure(Exception(errorMessage))
                }

                if (responseBody == null) {
                    return@withContext Result.failure(Exception("Empty response from API"))
                }

                val claudeResponse = json.decodeFromString<ClaudeResponse>(responseBody)
                val extractedJson = claudeResponse.content.firstOrNull()?.text
                    ?: return@withContext Result.failure(Exception("No content in response"))

                Result.success(extractedJson)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to extract medications: ${e.message}"))
            }
        }
    }
}
