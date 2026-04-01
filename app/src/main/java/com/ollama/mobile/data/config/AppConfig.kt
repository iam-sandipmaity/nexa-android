package com.ollama.mobile.data.config

import android.content.Context
import com.ollama.mobile.BuildConfig

object AppConfig {
    private const val PREFS_NAME = "ollama_mobile_config"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_API_KEY = "api_key"
    private const val DEFAULT_BASE_URL = "https://ollama.com/"

    private var initialized = false
    private lateinit var appContext: Context

    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        initialized = true
        seedFromBuildConfigIfNeeded()
    }

    private fun prefs() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBaseUrl(): String {
        ensureInitialized()
        return prefs().getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun updateBaseUrl(url: String) {
        ensureInitialized()
        val normalizedUrl = url.trim().ifBlank { DEFAULT_BASE_URL }
        prefs().edit().putString(KEY_BASE_URL, normalizedUrl).apply()
    }

    fun getApiKey(): String {
        ensureInitialized()
        return prefs().getString(KEY_API_KEY, "") ?: ""
    }

    fun updateApiKey(apiKey: String) {
        ensureInitialized()
        prefs().edit().putString(KEY_API_KEY, apiKey.trim()).apply()
    }

    fun hasApiKey(): Boolean = getApiKey().isNotBlank()

    fun getAppContext(): Context {
        ensureInitialized()
        return appContext
    }

    private fun seedFromBuildConfigIfNeeded() {
        val prefs = prefs()
        val hasSavedBaseUrl = !prefs.getString(KEY_BASE_URL, "").isNullOrBlank()
        val hasSavedApiKey = !prefs.getString(KEY_API_KEY, "").isNullOrBlank()

        if (!hasSavedBaseUrl) {
            prefs.edit().putString(KEY_BASE_URL, BuildConfig.OLLAMA_BASE_URL.ifBlank { DEFAULT_BASE_URL }).apply()
        }

        if (!hasSavedApiKey && BuildConfig.OLLAMA_API_KEY.isNotBlank()) {
            prefs.edit().putString(KEY_API_KEY, BuildConfig.OLLAMA_API_KEY).apply()
        }
    }

    private fun ensureInitialized() {
        check(initialized) { "AppConfig.init(context) must be called before use." }
    }
}
