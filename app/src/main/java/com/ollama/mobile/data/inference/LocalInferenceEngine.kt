package com.ollama.mobile.data.inference

import com.ollama.mobile.domain.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.codeshipping.llamakotlin.LlamaConfig
import org.codeshipping.llamakotlin.LlamaModel
import java.io.File

sealed class InferenceState {
    data object Idle : InferenceState()
    data object Loading : InferenceState()
    data class Ready(val model: LlamaModel) : InferenceState()
    data class Error(val message: String) : InferenceState()
}

class LocalInferenceEngine {
    
    private var model: LlamaModel? = null
    private var currentModelPath: String = ""
    private var inferenceJob: Job? = null
    
    private val _state = MutableStateFlow<InferenceState>(InferenceState.Idle)
    val state = _state.asStateFlow()
    
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded = _isModelLoaded.asStateFlow()

    /**
     * Load a GGUF model from the given file path
     */
    fun loadModel(modelPath: String, contextSize: Int = 2048, threads: Int = 4): Flow<Float> = flow {
        emit(0f)
        
        val modelFile = File(modelPath)
        if (!modelFile.exists()) {
            _state.value = InferenceState.Error("Model file not found: $modelPath")
            _isModelLoaded.value = false
            emit(-1f)
            return@flow
        }
        
        if (modelFile.length() < 1000000L) {
            _state.value = InferenceState.Error("Model file too small (${modelFile.length()} bytes). Download may be incomplete.")
            _isModelLoaded.value = false
            emit(-1f)
            return@flow
        }
        
        _state.value = InferenceState.Loading
        emit(0.1f)
        
        try {
            model = LlamaModel.load(modelPath) {
                this.contextSize = contextSize
                this.threads = threads
                this.temperature = 0.7f
                this.topP = 0.9f
                this.topK = 40
                this.repeatPenalty = 1.1f
                this.maxTokens = 512
                this.useMmap = true
                this.useMlock = false
                this.gpuLayers = 0
            }
            
            currentModelPath = modelPath
            _isModelLoaded.value = true
            _state.value = InferenceState.Ready(model!!)
            emit(1f)
        } catch (e: Exception) {
            val errorMsg = "Failed to load model: ${e.message}"
            _state.value = InferenceState.Error(errorMsg)
            _isModelLoaded.value = false
            emit(-1f)
        }
    }
    
    /**
     * Generate a response for the given prompt
     */
    fun generate(prompt: String): Flow<String> = flow {
        val currentModel = model
        if (currentModel == null) {
            emit("Error: No model loaded")
            return@flow
        }
        
        try {
            val formattedPrompt = formatPrompt(prompt)
            var previousChunk = ""
            currentModel.generateStream(formattedPrompt).collect { token ->
                val delta = extractDeltaChunk(previousChunk, token)
                previousChunk = token
                if (delta.isNotEmpty()) emit(delta)
            }
        } catch (e: Exception) {
            emit("Error: ${e.message}")
        }
    }
    
    /**
     * Generate response from chat messages
     */
    fun chat(messages: List<ChatMessage>): Flow<String> = flow {
        val currentModel = model
        if (currentModel == null) {
            emit("Error: No model loaded")
            return@flow
        }
        
        try {
            val prompt = buildPrompt(messages)
            var previousChunk = ""
            currentModel.generateStream(prompt).collect { token ->
                val delta = extractDeltaChunk(previousChunk, token)
                previousChunk = token
                if (delta.isNotEmpty()) emit(delta)
            }
        } catch (e: Exception) {
            emit("Error: ${e.message}")
        }
    }
    
    /**
     * Stop ongoing generation
     */
    fun stop() {
        inferenceJob?.cancel()
        model?.cancelGeneration()
    }
    
    /**
     * Free model resources
     */
    fun free() {
        inferenceJob?.cancel()
        try {
            model?.close()
        } catch (e: Exception) {
            // Ignore
        }
        model = null
        currentModelPath = ""
        _isModelLoaded.value = false
        _state.value = InferenceState.Idle
    }
    
    fun isLoaded(): Boolean = _isModelLoaded.value
    
    fun getModelName(): String {
        return currentModelPath.substringAfterLast("/").substringBefore(".gguf")
    }
    
    private fun buildPrompt(messages: List<ChatMessage>): String {
        // Build prompt based on model type
        val modelName = getModelName().lowercase()
        
        return when {
            modelName.contains("gemma") -> formatGemmaPrompt(messages)
            modelName.contains("llama") -> formatLlama3Prompt(messages)
            modelName.contains("phi") -> formatPhi3Prompt(messages)
            modelName.contains("qwen") -> formatChatMLPrompt(messages)
            else -> formatDefaultPrompt(messages)
        }
    }

    private fun formatGemmaPrompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()

        for (msg in messages) {
            val role = if (msg.role == "user") "user" else "model"
            sb.append("<start_of_turn>")
            sb.append(role)
            sb.append("\n")
            sb.append(msg.content)
            sb.append("<end_of_turn>\n")
        }

        sb.append("<start_of_turn>model\n")
        return sb.toString()
    }
    
    private fun formatLlama3Prompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        sb.append("<|begin_of_text|>")
        
        for (msg in messages) {
            val headerId = if (msg.role == "user") "user" else "assistant"
            sb.append("<|start_header_id|>$headerId<|end_header_id|>\n\n")
            sb.append(msg.content)
            sb.append("<|eot_id|>")
        }
        
        sb.append("<|start_header_id|>assistant<|end_header_id|>\n\n")
        return sb.toString()
    }
    
    private fun formatPhi3Prompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        
        for (msg in messages) {
            val role = if (msg.role == "user") "user" else "assistant"
            sb.append("<|$role|>\n${msg.content}<|end|>\n")
        }
        
        sb.append("<|assistant|>\n")
        return sb.toString()
    }
    
    private fun formatChatMLPrompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        
        for (msg in messages) {
            val role = if (msg.role == "user") "user" else "assistant"
            sb.append("<|im_start|>$role\n${msg.content}<|im_end|>\n")
        }
        
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }
    
    private fun formatDefaultPrompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        for (msg in messages) {
            val role = if (msg.role == "user") "User" else "Assistant"
            sb.append("$role: ${msg.content}\n")
        }
        sb.append("Assistant: ")
        return sb.toString()
    }
    
    private fun formatPrompt(prompt: String): String {
        val modelName = getModelName().lowercase()
        
        return when {
            modelName.contains("gemma") -> {
                "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
            }
            modelName.contains("llama") -> {
                "<|begin_of_text|><|start_header_id|>user<|end_header_id|>\n\n$prompt<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"
            }
            modelName.contains("phi") -> {
                "<|user|>\n$prompt<|end|>\n<|assistant|>\n"
            }
            modelName.contains("qwen") -> {
                "<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
            }
            else -> prompt
        }
    }

    private fun extractDeltaChunk(previousChunk: String, currentChunk: String): String {
        if (currentChunk.isEmpty()) return ""
        if (previousChunk.isEmpty()) return currentChunk
        return if (currentChunk.startsWith(previousChunk)) {
            currentChunk.removePrefix(previousChunk)
        } else {
            currentChunk
        }
    }
}