package com.ollama.mobile.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.repository.ModelRepository
import com.ollama.mobile.data.repository.OfflineModelRepository
import com.ollama.mobile.data.inference.LocalInferenceEngine
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
    val isConnected: Boolean = true,
    val isOfflineModel: Boolean = false,
    val isModelLoaded: Boolean = false
)

class ChatViewModel(
    private val repository: ModelRepository = ModelRepository(),
    private val offlineRepository: OfflineModelRepository = OfflineModelRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var inferenceEngine: LocalInferenceEngine? = null

    fun initializeWithModel(modelName: String) {
        _uiState.value = _uiState.value.copy(selectedModel = modelName)
        
        if (isOfflineModel(modelName)) {
            _uiState.value = _uiState.value.copy(
                isOfflineModel = true,
                isConnected = false,
                error = null
            )
            // Try to load the model for local inference
            loadOfflineModel(modelName)
            return
        }
        checkConnection()
    }

    private fun loadOfflineModel(modelName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = "Loading offline model..."
                )
                
                // Get the downloaded model info
                val modelId = modelName.removePrefix("offline:")
                val downloadedModels = offlineRepository.getDownloadedModels()
                val model = downloadedModels.firstOrNull { it.id == modelId }
                
                if (model == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Model not found. Please download first."
                    )
                    return@launch
                }
                
                // Initialize inference engine
                inferenceEngine = LocalInferenceEngine()
                
                // Load the model
                val success = inferenceEngine!!.loadModel(model.localPath)
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isModelLoaded = true,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load model"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
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
        
        // Handle offline model inference
        if (isOfflineModel(_uiState.value.selectedModel)) {
            if (!_uiState.value.isModelLoaded) {
                _uiState.value = _uiState.value.copy(
                    error = "Model still loading..."
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
                try {
                    // Get response from local inference
                    val response = inferenceEngine?.chat(newMessages) 
                        ?: "Error: Inference engine not available"
                    
                    val assistantMessage = ChatMessage(
                        role = "assistant",
                        content = response
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isLoading = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Inference error: ${e.message}"
                    )
                }
            }
            return
        }
        
        // Cloud model inference
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
    
    override fun onCleared() {
        super.onCleared()
        inferenceEngine?.free()
    }

    private fun isOfflineModel(modelName: String): Boolean = modelName.startsWith("offline:")
}
