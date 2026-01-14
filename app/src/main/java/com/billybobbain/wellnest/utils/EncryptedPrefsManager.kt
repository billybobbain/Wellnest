package com.billybobbain.wellnest.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages encrypted storage for sensitive data like API keys.
 * Uses Android Keystore-backed encryption to protect data at rest.
 *
 * Security features:
 * - Keys encrypted with AES256_GCM
 * - Values encrypted with AES256_GCM
 * - Hardware-backed keystore (when available)
 * - Data never stored in plaintext
 * - Passes Zimperium MTD security scans
 */
object EncryptedPrefsManager {
    private const val PREFS_FILENAME = "wellnest_secure_prefs"
    private const val KEY_CLAUDE_API_KEY = "claude_api_key"
    private const val KEY_AI_ENABLED = "ai_clarification_enabled"

    private var encryptedPrefs: SharedPreferences? = null

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        if (encryptedPrefs == null) {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedPrefs = EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return encryptedPrefs!!
    }

    /**
     * Store the Claude API key securely.
     * The key is encrypted with hardware-backed keystore.
     */
    fun setClaudeApiKey(context: Context, apiKey: String?) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().apply {
            if (apiKey != null) {
                putString(KEY_CLAUDE_API_KEY, apiKey)
            } else {
                remove(KEY_CLAUDE_API_KEY)
            }
            apply()
        }
    }

    /**
     * Retrieve the encrypted Claude API key.
     * Returns null if no key is stored.
     */
    fun getClaudeApiKey(context: Context): String? {
        return try {
            getEncryptedPrefs(context).getString(KEY_CLAUDE_API_KEY, null)
        } catch (e: Exception) {
            // If decryption fails (e.g., key was tampered with), return null
            null
        }
    }

    /**
     * Check if AI clarification is enabled
     */
    fun isAiEnabled(context: Context): Boolean {
        return getEncryptedPrefs(context).getBoolean(KEY_AI_ENABLED, false)
    }

    /**
     * Set AI clarification enabled status
     */
    fun setAiEnabled(context: Context, enabled: Boolean) {
        getEncryptedPrefs(context).edit().apply {
            putBoolean(KEY_AI_ENABLED, enabled)
            apply()
        }
    }

    /**
     * Clear all encrypted data
     */
    fun clearAll(context: Context) {
        getEncryptedPrefs(context).edit().clear().apply()
    }
}
