package com.ollama.mobile.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.domain.model.OllamaModel
import com.ollama.mobile.domain.usecase.ChatUseCase
import com.ollama.mobile.domain.usecase.GetModelsUseCase
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
    val availableModels: List<OllamaModel> = emptyList(),
    val isConnected: Boolean = true
)

class ChatViewModel(
    private val chatUseCase: ChatUseCase = ChatUseCase(),
    private val getModelsUseCase: GetModelsUseCase = GetModelsUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun initializeWithModel(modelName: String) {
        _uiState.value = _uiState.value.copy(selectedModel = modelName)
        loadModels()
    }

    fun loadModels() {
        viewModelScope.launch {
            getModelsUseCase().fold(
                onSuccess = { models ->
                    _uiState.value = _uiState.value.copy(
                        availableModels = models,
                        isConnected = true,
                        error = null
                    )
                    if (_uiState.value.selectedModel.isEmpty() && models.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            selectedModel = models.first().name
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isConnected = false,
                        error = "Cannot connect to Ollama. Make sure it's running."
                    )
                }
            )
        }
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun selectModel(modelName: String) {
        _uiState.value = _uiState.value.copy(selectedModel = modelName)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(role = "user", content = text)
        val newMessages = _uiState.value.messages + userMessage

        _uiState.value = _uiState.value.copy(
            messages = newMessages,
            inputText = "",
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val allMessages = newMessages
            chatUseCase(_uiState.value.selectedModel, allMessages).fold(
                onSuccess = { result ->
                    val assistantMessage = ChatMessage(
                        role = result.message.role,
                        content = result.message.content
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
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
