package com.ollama.mobile.data.config

import android.content.Context
import com.ollama.mobile.BuildConfig
import com.ollama.mobile.data.repository.ChatHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppConfig {
    private const val PREFS_NAME = "ollama_mobile_config"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_FONT_SIZE = "font_size"
    private const val DEFAULT_BASE_URL = "https://ollama.com/"
    
    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    const val FONT_SMALL = 0
    const val FONT_MEDIUM = 1
    const val FONT_LARGE = 2

    private val _fontSizeFlow = MutableStateFlow(FONT_MEDIUM)
    val fontSizeFlow: StateFlow<Int> = _fontSizeFlow.asStateFlow()

    private var initialized = false
    private lateinit var appContext: Context
    
    private val _themeModeFlow = MutableStateFlow(THEME_SYSTEM)
    val themeModeFlow: StateFlow<Int> = _themeModeFlow.asStateFlow()

    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        initialized = true
        seedFromBuildConfigIfNeeded()
        _themeModeFlow.value = getThemeMode()
        _fontSizeFlow.value = getFontSize()
    }

    private fun prefs() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getChatHistoryRepository(): ChatHistoryRepository {
        ensureInitialized()
        return ChatHistoryRepository(appContext)
    }

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
    
    fun getThemeMode(): Int {
        ensureInitialized()
        return prefs().getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }
    
    fun setThemeMode(mode: Int) {
        ensureInitialized()
        prefs().edit().putInt(KEY_THEME_MODE, mode).apply()
        _themeModeFlow.value = mode
    }
    
    fun getFontSize(): Int {
        ensureInitialized()
        return prefs().getInt(KEY_FONT_SIZE, FONT_MEDIUM)
    }
    
    fun setFontSize(size: Int) {
        ensureInitialized()
        prefs().edit().putInt(KEY_FONT_SIZE, size).apply()
        _fontSizeFlow.value = size
    }
    
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
