package com.ollama.mobile.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.repository.ModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "https://ollama.com/",
    val apiKey: String = "",
    val isTestingConnection: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Unknown
)

enum class ConnectionStatus {
    Unknown, Connected, Failed
}

class SettingsViewModel(
    private val repository: ModelRepository = ModelRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            baseUrl = repository.getBaseUrl(),
            apiKey = repository.getApiKey()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
}
