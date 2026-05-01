package com.ollama.mobile.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.config.AppConfig
import com.ollama.mobile.data.repository.ChatHistoryRepository
import com.ollama.mobile.data.repository.ModelRepository
import com.ollama.mobile.data.repository.OfflineModelRepository
import com.ollama.mobile.data.inference.LocalInferenceEngine
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.domain.model.DownloadedOfflineModel
import com.ollama.mobile.domain.model.MessageAttachment
import com.ollama.mobile.domain.model.OllamaModelInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    val chatHistory: List<ChatHistoryRepository.ChatHistoryEntry> = emptyList(),
    val pendingAttachments: List<MessageAttachment> = emptyList()
)

class ChatViewModel(
    private val repository: ModelRepository = ModelRepository(),
    private val offlineRepository: OfflineModelRepository = OfflineModelRepository(),
    private val chatHistoryRepository: ChatHistoryRepository = AppConfig.getChatHistoryRepository()
) : ViewModel() {

    private data class StreamProcessResult(
        val updatedText: String,
        val shouldStop: Boolean
    )

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
                _uiState.value = _uiState.value.copy(error = "Failed to delete chat: ${e.message}")
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
                isLibraryModel = false,
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
        _uiState.value = _uiState.value.copy(
            isOfflineModel = false,
            isLibraryModel = false
        )
        checkConnection()
    }

    fun initializeWithChat(modelName: String, existingChatId: String?) {
        _uiState.value = _uiState.value.copy(
            selectedModel = modelName,
            chatId = existingChatId ?: UUID.randomUUID().toString()
        )

        if (existingChatId != null) {
            loadChatHistory(existingChatId)
        }

        if (isOfflineModel(modelName)) {
            _uiState.value = _uiState.value.copy(
                isOfflineModel = true,
                isLibraryModel = false,
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
        _uiState.value = _uiState.value.copy(
            isOfflineModel = false,
            isLibraryModel = false
        )
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
                _uiState.value = _uiState.value.copy(error = "Failed to load chat: ${e.message}")
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
                    error = "Add your Nexa Cloud API key in Settings to start chatting."
                )
                return@launch
            }

            repository.checkConnection().fold(
                onSuccess = { connected ->
                    _uiState.value = _uiState.value.copy(
                        isConnected = connected,
                        error = if (connected) null else "Couldn't connect to Nexa Cloud."
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isConnected = false,
                        error = "Couldn't connect to Nexa Cloud."
                    )
                }
            )
        }
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun addAttachment(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val fileName = getFileNameFromUri(context, uri)
                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                val fileSize = contentResolver.openInputStream(uri)?.use { it.available().toLong() } ?: 0L
                
                val attachment = MessageAttachment(
                    uri = uri.toString(),
                    fileName = fileName,
                    mimeType = mimeType,
                    fileSize = fileSize
                )
                
                val currentAttachments = _uiState.value.pendingAttachments.toMutableList()
                currentAttachments.add(attachment)
                _uiState.value = _uiState.value.copy(pendingAttachments = currentAttachments)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to add file: ${e.message}")
            }
        }
    }

    fun removeAttachment(index: Int) {
        val currentAttachments = _uiState.value.pendingAttachments.toMutableList()
        if (index in currentAttachments.indices) {
            currentAttachments.removeAt(index)
            _uiState.value = _uiState.value.copy(pendingAttachments = currentAttachments)
        }
    }

    fun clearAttachments() {
        _uiState.value = _uiState.value.copy(pendingAttachments = emptyList())
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex("_display_name")
                if (displayNameIndex != -1) {
                    val displayName = it.getString(displayNameIndex)
                    if (!displayName.isNullOrBlank()) return displayName
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast("/") ?: "unknown_file"
    }

    private fun readUriContent(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun encodeImageToBase64(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                android.util.Base64.encodeToString(inputStream.readBytes(), android.util.Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        val attachments = _uiState.value.pendingAttachments
        if ((text.isBlank() && attachments.isEmpty()) || _uiState.value.isLoading) return

        val imageAttachments = attachments.filter { it.mimeType.startsWith("image/") }
        val context = AppConfig.getAppContext()

        if (isOfflineModel(_uiState.value.selectedModel)) {
            if (!_uiState.value.isModelLoaded) {
                _uiState.value = _uiState.value.copy(error = "Model still loading...")
                return
            }

            if (imageAttachments.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "Offline image analysis isn't supported by the current on-device engine yet. Use a vision-capable cloud model for image uploads."
                )
                return
            }

            val contentWithFileContext = buildMessageContent(
                context = context,
                text = text,
                attachments = attachments
            )

            val userMessage = ChatMessage(
                role = "user",
                content = contentWithFileContext,
                attachments = attachments
            )
            val newMessages = _uiState.value.messages + userMessage

            _uiState.value = _uiState.value.copy(
                messages = newMessages,
                inputText = "",
                isLoading = true,
                streamingResponse = "",
                error = null,
                pendingAttachments = emptyList()
            )

            saveChatToHistory()

            streamingJob?.cancel()
            streamingJob = viewModelScope.launch {
                try {
                    val responseBuilder = StringBuilder()
                    var shouldStopGeneration = false

                    inferenceEngine?.chat(newMessages)?.collect { token ->
                        val current = processStreamingToken(responseBuilder.toString(), token)
                        responseBuilder.clear()
                        responseBuilder.append(current.updatedText)
                        _uiState.value = _uiState.value.copy(
                            streamingResponse = responseBuilder.toString()
                        )

                        if (current.shouldStop) {
                            shouldStopGeneration = true
                            inferenceEngine?.stop()
                            return@collect
                        }
                    }

                    val fullResponse = sanitizeModelArtifacts(responseBuilder.toString())
                    if (fullResponse.isBlank() && shouldStopGeneration) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            streamingResponse = ""
                        )
                        return@launch
                    }

                    val assistantMessage = ChatMessage(
                        role = "assistant",
                        content = fullResponse
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isLoading = false,
                        streamingResponse = ""
                    )

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
                error = "Add your Nexa Cloud API key in Settings before chatting.",
                isConnected = false
            )
            return
        }

        if (imageAttachments.isNotEmpty() && !supportsVision(_uiState.value.selectedModel)) {
            _uiState.value = _uiState.value.copy(
                error = "This cloud model doesn't support image inputs. Choose a vision-capable model such as Gemma 3, Llama 3.2 Vision, or Llama 4."
            )
            return
        }

        val inaccessibleImages = imageAttachments.filter {
            encodeImageToBase64(context, Uri.parse(it.uri)).isNullOrBlank()
        }
        if (inaccessibleImages.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "One or more images couldn't be read. Please re-attach them and try again."
            )
            return
        }

        val contentWithFileContext = buildMessageContent(
            context = context,
            text = text,
            attachments = attachments
        )

        val userMessage = ChatMessage(
            role = "user",
            content = contentWithFileContext,
            attachments = attachments
        )
        val newMessages = _uiState.value.messages + userMessage

        _uiState.value = _uiState.value.copy(
            messages = newMessages,
            inputText = "",
            isLoading = true,
            error = null,
            pendingAttachments = emptyList()
        )

        saveChatToHistory()

        viewModelScope.launch {
            repository.chat(_uiState.value.selectedModel, newMessages).fold(
                onSuccess = { result ->
                    val assistantMessage = ChatMessage(
                        role = result.message.role,
                        content = sanitizeModelArtifacts(result.message.content)
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isLoading = false,
                        isConnected = true
                    )
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
                val history = chatHistoryRepository.getAllHistory().first()
                _uiState.value = _uiState.value.copy(chatHistory = history)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save chat: ${e.message}")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        saveChatToHistory()
        streamingJob?.cancel()
        inferenceEngine?.free()
    }

    fun getChatHistory(callback: (List<ChatHistoryRepository.ChatHistoryEntry>) -> Unit) {
        viewModelScope.launch {
            try {
                val history = chatHistoryRepository.getAllHistory().first()
                callback(history)
            } catch (e: Exception) {
                callback(emptyList())
            }
        }
    }

    private fun isOfflineModel(modelName: String): Boolean = modelName.startsWith("offline:")

    private fun isLibraryModel(modelName: String): Boolean = modelName.startsWith("library:")

    private fun buildMessageContent(
        context: Context,
        text: String,
        attachments: List<MessageAttachment>
    ): String {
        val attachmentContext = attachments.mapNotNull { attachment ->
            when {
                attachment.mimeType.startsWith("text/") -> {
                    val content = readUriContent(context, Uri.parse(attachment.uri))
                    if (content != null) "[File: ${attachment.fileName}\nContent: ${content.take(2000)}]"
                    else "[File: ${attachment.fileName}]"
                }
                attachment.mimeType.startsWith("image/") -> null
                else -> "[File attached: ${attachment.fileName} (${attachment.mimeType})]"
            }
        }.joinToString("\n")

        val parts = listOfNotNull(
            attachmentContext.takeIf { it.isNotBlank() },
            text.takeIf { it.isNotBlank() }
        )

        return when {
            parts.isNotEmpty() -> parts.joinToString("\n\n")
            attachments.any { it.mimeType.startsWith("image/") } -> "Please analyze the attached image."
            else -> ""
        }
    }

    private fun supportsVision(modelName: String): Boolean {
        val normalized = modelName
            .removePrefix("library:")
            .removeSuffix("-cloud")
            .lowercase()

        return normalized.contains("vision") ||
            normalized.contains("llava") ||
            normalized.contains("bakllava") ||
            normalized.contains("minicpm-v") ||
            normalized.contains("minicpmv") ||
            normalized.startsWith("gemma3") ||
            normalized.startsWith("llama4") ||
            normalized.contains("qwen2.5-vl") ||
            normalized.contains("qwen2.5vl")
    }

    private fun processStreamingToken(currentText: String, incomingToken: String): StreamProcessResult {
        val appended = currentText + incomingToken
        val normalized = sanitizeModelArtifacts(appended)

        val stopMarkers = listOf(
            "<|im_end|>",
            "<|eot_id|>",
            "<|end|>",
            "<|end_of_text|>",
            "<end_of_turn>",
            "<start_of_turn>user",
            "<|im_start|>user",
            "<|start_header_id|>user<|end_header_id|>"
        )

        val stopIndex = stopMarkers
            .map { marker -> normalized.indexOf(marker) }
            .filter { it >= 0 }
            .minOrNull() ?: -1

        return if (stopIndex >= 0) {
            StreamProcessResult(
                updatedText = sanitizeModelArtifacts(normalized.substring(0, stopIndex)),
                shouldStop = true
            )
        } else {
            val repeatedAssistantIndex = findRepeatedAssistantStart(normalized)
            if (repeatedAssistantIndex >= 0) {
                StreamProcessResult(
                    updatedText = sanitizeModelArtifacts(normalized.substring(0, repeatedAssistantIndex)),
                    shouldStop = true
                )
            } else {
                val repeatedBlockIndex = findRepeatedBlockStart(normalized)
                if (repeatedBlockIndex >= 0) {
                    StreamProcessResult(
                        updatedText = sanitizeModelArtifacts(normalized.substring(0, repeatedBlockIndex)),
                        shouldStop = true
                    )
                } else {
                    StreamProcessResult(updatedText = normalized, shouldStop = false)
                }
            }
        }
    }

    private fun sanitizeModelArtifacts(raw: String): String {
        return raw
            .replace("<|im_start|>", "")
            .replace("<|im_end|>", "")
            .replace("<|eot_id|>", "")
            .replace("<|end|>", "")
            .replace("<|assistant|>", "")
            .replace("<|user|>", "")
            .replace("<start_of_turn>", "")
            .replace("<end_of_turn>", "")
            .replace("<|start_header_id|>", "")
            .replace("<|end_header_id|>", "")
    }

    private fun findRepeatedAssistantStart(text: String): Int {
        if (text.length < 80) return -1
        val matches = Regex("\\nassistant\\s*\\n", RegexOption.IGNORE_CASE).findAll(text).map { it.range.first }.toList()
        return if (matches.size >= 2) matches[1] else -1
    }

    private fun findRepeatedBlockStart(text: String): Int {
        val cleaned = text.trimStart()
        if (cleaned.length < 240) return -1

        val sampleLength = minOf(140, cleaned.length / 2)
        val seed = cleaned.take(sampleLength)
        val repeatAt = cleaned.indexOf(seed, startIndex = sampleLength)
        return if (repeatAt >= 0) {
            val trimmedOffset = text.length - cleaned.length
            trimmedOffset + repeatAt
        } else {
            -1
        }
    }
}
