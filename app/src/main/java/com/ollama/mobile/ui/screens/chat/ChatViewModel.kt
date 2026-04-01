package com.ollama.mobile.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.repository.ModelRepository
import com.ollama.mobile.domain.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedModel: String = "",
    val isConnected: Boolean = true
)

class ChatViewModel(
    private val repository: ModelRepository = ModelRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun initializeWithModel(modelName: String) {
        _uiState.value = _uiState.value.copy(selectedModel = modelName)
        checkConnection()
    }

    private fun checkConnection() {
        viewModelScope.launch {
            if (!repository.hasApiKey()) {
                _uiState.value = _uiState.value.copy(
                    isConnected = false,
                    error = "Add your Ollama API key in Settings to start chatting."
                )
                return@launch
            }

            repository.checkConnection().fold(
                onSuccess = { connected ->
                    _uiState.value = _uiState.value.copy(
                        isConnected = connected,
                        error = if (connected) null else "Couldn't connect to Ollama Cloud."
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isConnected = false,
                        error = "Couldn't connect to Ollama Cloud."
                    )
                }
            )
        }
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return
        if (!repository.hasApiKey()) {
            _uiState.value = _uiState.value.copy(
                error = "Add your Ollama API key in Settings before chatting.",
                isConnected = false
            )
            return
        }

        val userMessage = ChatMessage(role = "user", content = text)
        val newMessages = _uiState.value.messages + userMessage

        _uiState.value = _uiState.value.copy(
            messages = newMessages,
            inputText = "",
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            repository.chat(_uiState.value.selectedModel, newMessages).fold(
                onSuccess = { result ->
                    val assistantMessage = ChatMessage(
                        role = result.message.role,
                        content = result.message.content
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isLoading = false,
                        isConnected = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnected = false,
                        error = error.message ?: "Failed to get response"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearChat() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }
}
