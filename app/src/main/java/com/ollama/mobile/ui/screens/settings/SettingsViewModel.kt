package com.ollama.mobile.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.config.AppConfig
import com.ollama.mobile.data.repository.ChatHistoryRepository
import com.ollama.mobile.data.repository.ModelRepository
import com.ollama.mobile.data.repository.OfflineModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "https://ollama.com/",
    val apiKey: String = "",
    val isTestingConnection: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Unknown,
    val cacheSize: String = "0 MB",
    val chatCount: Int = 0,
    val appVersion: String = "1.0.0"
)

enum class ConnectionStatus {
    Unknown, Connected, Failed
}

class SettingsViewModel(
    private val repository: ModelRepository = ModelRepository(),
    private val offlineRepository: OfflineModelRepository = OfflineModelRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            baseUrl = repository.getBaseUrl(),
            apiKey = repository.getApiKey()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCacheInfo()
    }

    private fun loadCacheInfo() {
        viewModelScope.launch {
            try {
                val historyRepo = AppConfig.getChatHistoryRepository()
                val history = historyRepo.getAllHistory().first()
                _uiState.value = _uiState.value.copy(
                    chatCount = history.size
                )
            } catch (e: Exception) {
                // Ignore
            }
            
            try {
                val cacheSize = calculateCacheSize()
                _uiState.value = _uiState.value.copy(cacheSize = cacheSize)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun calculateCacheSize(): String {
        return try {
            val context = AppConfig.getAppContext()
            val cacheDir = context.cacheDir
            val size = cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            when {
                size >= 1_000_000_000 -> String.format("%.1f GB", size / 1_000_000_000.0)
                size >= 1_000_000 -> String.format("%.0f MB", size / 1_000_000.0)
                else -> String.format("%d KB", size / 1000)
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun updateBaseUrl(url: String) {
        repository.updateBaseUrl(url)
        _uiState.value = _uiState.value.copy(
            baseUrl = url,
            connectionStatus = ConnectionStatus.Unknown
        )
    }

    fun updateApiKey(apiKey: String) {
        repository.updateApiKey(apiKey)
        _uiState.value = _uiState.value.copy(
            apiKey = apiKey,
            connectionStatus = ConnectionStatus.Unknown
        )
    }

    fun testConnection() {
        viewModelScope.launch {
            if (_uiState.value.apiKey.isBlank()) {
                _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.Failed)
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isTestingConnection = true,
                connectionStatus = ConnectionStatus.Unknown
            )

            repository.checkConnection().fold(
                onSuccess = { connected ->
                    _uiState.value = _uiState.value.copy(
                        isTestingConnection = false,
                        connectionStatus = if (connected) ConnectionStatus.Connected else ConnectionStatus.Failed
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isTestingConnection = false,
                        connectionStatus = ConnectionStatus.Failed
                    )
                }
            )
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                val context = AppConfig.getAppContext()
                val cacheDir = context.cacheDir
                cacheDir.deleteRecursively()
                cacheDir.mkdirs()
                _uiState.value = _uiState.value.copy(cacheSize = "0 MB")
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                val historyRepo = AppConfig.getChatHistoryRepository()
                historyRepo.clearAllHistory()
                _uiState.value = _uiState.value.copy(chatCount = 0)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
