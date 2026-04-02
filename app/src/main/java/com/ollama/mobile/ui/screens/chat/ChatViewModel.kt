package com.ollama.mobile.ui.screens.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.config.AppConfig
import com.ollama.mobile.data.repository.ChatHistoryRepository
import com.ollama.mobile.data.repository.ModelRepository
import com.ollama.mobile.data.repository.OfflineModelRepository
import com.ollama.mobile.data.inference.LocalInferenceEngine
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.domain.model.DownloadedOfflineModel
import com.ollama.mobile.domain.model.OllamaModelInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedModel: String = "",
    val isConnected: Boolean = true,
    val isOfflineModel: Boolean = false,
    val isLibraryModel: Boolean = false,
    val isModelLoaded: Boolean = false,
    val streamingResponse: String = "",
    val chatId: String? = null,
    val availableModels: List<OllamaModelInfo> = emptyList(),
    val downloadedModels: List<DownloadedOfflineModel> = emptyList(),
    val chatHistory: List<ChatHistoryRepository.ChatHistoryEntry> = emptyList()
)

class ChatViewModel(
    private val repository: ModelRepository = ModelRepository(),
    private val offlineRepository: OfflineModelRepository = OfflineModelRepository(),
    private val chatHistoryRepository: ChatHistoryRepository = AppConfig.getChatHistoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var inferenceEngine: LocalInferenceEngine? = null
    private var streamingJob: Job? = null

    init {
        loadAvailableModels()
    }

    private fun loadAvailableModels() {
        viewModelScope.launch {
            try {
                val cloudResult = repository.getAvailableModels()
                val cloudModels = cloudResult.getOrNull() ?: repository.getFallbackModels()
                val downloadedModels = offlineRepository.getDownloadedModels()
                val history = chatHistoryRepository.getAllHistory().first()
                _uiState.value = _uiState.value.copy(
                    availableModels = cloudModels,
                    downloadedModels = downloadedModels,
                    chatHistory = history
                )
            } catch (e: Exception) {
                val downloadedModels = offlineRepository.getDownloadedModels()
                val history = try { chatHistoryRepository.getAllHistory().first() } catch (_: Exception) { emptyList() }
                _uiState.value = _uiState.value.copy(
                    availableModels = repository.getFallbackModels(),
                    downloadedModels = downloadedModels,
                    chatHistory = history
                )
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            try {
                chatHistoryRepository.deleteChat(chatId)
                val history = chatHistoryRepository.getAllHistory().first()
                _uiState.value = _uiState.value.copy(chatHistory = history)
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }

    fun refreshModels() {
        loadAvailableModels()
    }

    fun initializeWithModel(modelName: String) {
        _uiState.value = _uiState.value.copy(
            selectedModel = modelName,
            chatId = UUID.randomUUID().toString()
        )
        
        if (isOfflineModel(modelName)) {
            _uiState.value = _uiState.value.copy(
                isOfflineModel = true,
                isConnected = false,
                error = null
            )
            loadOfflineModel(modelName)
            return
        }
        
        if (isLibraryModel(modelName)) {
            _uiState.value = _uiState.value.copy(
                isOfflineModel = false,
                isConnected = true,
                isLibraryModel = true,
                error = null
            )
            checkConnection()
            return
        }
        checkConnection()
    }

    fun initializeWithChat(modelName: String, existingChatId: String?) {
        _uiState.value = _uiState.value.copy(
            selectedModel = modelName,
            chatId = existingChatId ?: UUID.randomUUID().toString()
        )

        if (existingChatId != null && chatHistoryRepository != null) {
            loadChatHistory(existingChatId)
        }

        if (isOfflineModel(modelName)) {
            _uiState.value = _uiState.value.copy(
                isOfflineModel = true,
                isConnected = false,
                error = null
            )
            loadOfflineModel(modelName)
            return
        }

        if (isLibraryModel(modelName)) {
            _uiState.value = _uiState.value.copy(
                isOfflineModel = false,
                isConnected = true,
                isLibraryModel = true,
                error = null
            )
            checkConnection()
            return
        }
        checkConnection()
    }

    private fun loadChatHistory(chatId: String) {
        viewModelScope.launch {
            try {
                val chat = chatHistoryRepository.getChatById(chatId).first()
                if (chat != null) {
                    _uiState.value = _uiState.value.copy(
                        messages = chat.messages,
                        selectedModel = chat.modelName
                    )
                }
            } catch (e: Exception) {
                // Ignore errors loading history
            }
        }
    }

    private fun loadOfflineModel(modelName: String) {
        viewModelScope.launch {
            try {
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
                
                val modelFile = java.io.File(model.localPath)
                if (!modelFile.exists()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Model file not found at: ${model.localPath}"
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = "Loading ${model.displayName}..."
                )
                
                inferenceEngine = LocalInferenceEngine()
                
                inferenceEngine!!.loadModel(model.localPath).collect { progress ->
                    if (progress < 0) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load model"
                        )
                    } else if (progress >= 1f) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isModelLoaded = true,
                            error = null
                        )
                    }
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
                streamingResponse = "",
                error = null
            )
            
            // Save chat history immediately after user message
            saveChatToHistory()
            
            // Start streaming response
            streamingJob?.cancel()
            streamingJob = viewModelScope.launch {
                try {
                    val responseBuilder = StringBuilder()
                    
                    inferenceEngine?.chat(newMessages)?.collect { token ->
                        responseBuilder.append(token)
                        _uiState.value = _uiState.value.copy(
                            streamingResponse = responseBuilder.toString()
                        )
                    }
                    
                    // Done streaming
                    val fullResponse = responseBuilder.toString()
                    val assistantMessage = ChatMessage(
                        role = "assistant",
                        content = fullResponse
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isLoading = false,
                        streamingResponse = ""
                    )
                    
                    // Save chat history after assistant response
                    saveChatToHistory()
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        streamingResponse = "",
                        error = "Inference error: ${e.message}"
                    )
                }
            }
            return
        }
        
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

        // Save chat history immediately after user message
        saveChatToHistory()

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
                    // Save chat history after assistant response
                    saveChatToHistory()
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

    fun stopGeneration() {
        streamingJob?.cancel()
        inferenceEngine?.stop()
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            streamingResponse = ""
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearChat() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }

    fun startNewChat(modelName: String) {
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            chatId = UUID.randomUUID().toString(),
            selectedModel = modelName,
            streamingResponse = "",
            isLoading = false,
            error = null
        )
    }

    fun saveChatToHistory() {
        val chatId = _uiState.value.chatId ?: return
        val modelName = _uiState.value.selectedModel
        val messages = _uiState.value.messages

        if (modelName.isEmpty()) return

        viewModelScope.launch {
            try {
                chatHistoryRepository.saveChat(
                    ChatHistoryRepository.ChatHistoryEntry(
                        id = chatId,
                        modelName = modelName,
                        messages = messages
                    )
                )
                // Refresh history list in UI
                val history = chatHistoryRepository.getAllHistory().first()
                _uiState.value = _uiState.value.copy(chatHistory = history)
            } catch (e: Exception) {
                // Ignore save errors
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        saveChatToHistory()
        streamingJob?.cancel()
        inferenceEngine?.free()
    }

    fun getChatHistory(): List<ChatHistoryRepository.ChatHistoryEntry> {
        return try {
            runBlocking { chatHistoryRepository.getAllHistory().first() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun isOfflineModel(modelName: String): Boolean = modelName.startsWith("offline:")
    
    private fun isLibraryModel(modelName: String): Boolean = modelName.startsWith("library:")
}