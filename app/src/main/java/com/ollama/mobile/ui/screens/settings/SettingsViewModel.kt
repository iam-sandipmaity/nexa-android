package com.ollama.mobile.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "http://localhost:11434/",
    val isTestingConnection: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Unknown
)

enum class ConnectionStatus {
    Unknown, Connected, Failed
}

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateBaseUrl(url: String) {
        _uiState.value = _uiState.value.copy(baseUrl = url)
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTestingConnection = true,
                connectionStatus = ConnectionStatus.Unknown
            )
            
            delay(1000)
            
            val isSuccess = _uiState.value.baseUrl.isNotBlank()
            _uiState.value = _uiState.value.copy(
                isTestingConnection = false,
                connectionStatus = if (isSuccess) ConnectionStatus.Connected else ConnectionStatus.Failed
            )
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            // Save settings to DataStore
        }
    }
}
