package com.billybobbain.wellnest.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
                    This is a text message from an elderly person with vision problems. The message has spacing and typing errors but conveys important information. Please clarify what they're trying to say while preserving their voice and tone. Don't make it sound robotic - keep it natural and conversational.

                    Original message:
                    $originalText

                    Please provide only the clarified version of the message, nothing else.
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
}
