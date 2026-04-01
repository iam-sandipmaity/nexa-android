package com.ollama.mobile.data.repository

import com.ollama.mobile.data.api.OllamaApi
import com.ollama.mobile.data.api.RetrofitClient
import com.ollama.mobile.data.model.*
import com.ollama.mobile.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

class ModelRepository(private val api: OllamaApi = RetrofitClient.getApi()) {

    // Popular models with metadata
    val availableModels = listOf(
        OllamaModelInfo(
            id = "llama3.2",
            name = "llama3.2",
            displayName = "Llama 3.2",
            description = "Latest Meta LLM with excellent reasoning",
            size = "2.0GB",
            sizeBytes = 2_000_000_000,
            family = "Llama",
            minRam = "4GB"
        ),
        OllamaModelInfo(
            id = "llama3.1",
            name = "llama3.1",
            displayName = "Llama 3.1",
            description = "Powerful open-source LLM by Meta",
            size = "4.3GB",
            sizeBytes = 4_300_000_000,
            family = "Llama",
            minRam = "8GB"
        ),
        OllamaModelInfo(
            id = "llama3",
            name = "llama3",
            displayName = "Llama 3",
            description = "Fast and efficient Meta model",
            size = "4.7GB",
            sizeBytes = 4_700_000_000,
            family = "Llama",
            minRam = "8GB"
        ),
        OllamaModelInfo(
            id = "gemma2:2b",
            name = "gemma2:2b",
            displayName = "Gemma 2B",
            description = "Google's efficient 2B model",
            size = "1.6GB",
            sizeBytes = 1_600_000_000,
            family = "Gemma",
            minRam = "4GB"
        ),
        OllamaModelInfo(
            id = "gemma2:9b",
            name = "gemma2:9b",
            displayName = "Gemma 9B",
            description = "Google's powerful 9B model",
            size = "5.2GB",
            sizeBytes = 5_200_000_000,
            family = "Gemma",
            minRam = "8GB"
        ),
        OllamaModelInfo(
            id = "mistral",
            name = "mistral",
            displayName = "Mistral",
            description = "Excellent for instruction following",
            size = "4.1GB",
            sizeBytes = 4_100_000_000,
            family = "Mistral",
            minRam = "6GB"
        ),
        OllamaModelInfo(
            id = "mistral-nemo",
            name = "mistral-nemo",
            displayName = "Mistral Nemo",
            description = "Mistral's 12B model",
            size = "7.1GB",
            sizeBytes = 7_100_000_000,
            family = "Mistral",
            minRam = "8GB"
        ),
        OllamaModelInfo(
            id = "qwen2.5",
            name = "qwen2.5",
            displayName = "Qwen 2.5",
            description = "Alibaba's multilingual model",
            size = "3.3GB",
            sizeBytes = 3_300_000_000,
            family = "Qwen",
            minRam = "4GB"
        ),
        OllamaModelInfo(
            id = "qwen2.5:14b",
            name = "qwen2.5:14b",
            displayName = "Qwen 2.5 14B",
            description = "Alibaba's powerful 14B model",
            size = "9.0GB",
            sizeBytes = 9_000_000_000,
            family = "Qwen",
            minRam = "12GB"
        ),
        OllamaModelInfo(
            id = "codellama",
            name = "codellama",
            displayName = "Code Llama",
            description = "Specialized for code generation",
            size = "3.8GB",
            sizeBytes = 3_800_000_000,
            family = "Llama",
            minRam = "6GB"
        ),
        OllamaModelInfo(
            id = "phi3",
            name = "phi3",
            displayName = "Phi-3",
            description = "Microsoft's efficient small model",
            size = "2.3GB",
            sizeBytes = 2_300_000_000,
            family = "Phi",
            minRam = "4GB"
        ),
        OllamaModelInfo(
            id = "phi3.5",
            name = "phi3.5",
            displayName = "Phi-3.5",
            description = "Microsoft's latest small model",
            size = "2.2GB",
            sizeBytes = 2_200_000_000,
            family = "Phi",
            minRam = "4GB"
        ),
        OllamaModelInfo(
            id = "neural-chat",
            name = "neural-chat",
            displayName = "Neural Chat",
            description = "Intel's optimized chat model",
            size = "4.1GB",
            sizeBytes = 4_100_000_000,
            family = "Intel",
            minRam = "6GB"
        ),
        OllamaModelInfo(
            id = "starling-lm",
            name = "starling-lm",
            displayName = "Starling LM",
            description = "Nurturey's helpful assistant",
            size = "7.7GB",
            sizeBytes = 7_700_000_000,
            family = "Starling",
            minRam = "8GB"
        ),
        OllamaModelInfo(
            id = "tinyllama",
            name = "tinyllama",
            displayName = "TinyLlama",
            description = "Ultra-lightweight, 1.1B params",
            size = "637MB",
            sizeBytes = 637_000_000,
            family = "Llama",
            minRam = "1GB"
        ),
        OllamaModelInfo(
            id = "llama2-uncensored",
            name = "llama2-uncensored",
            displayName = "Llama 2 Uncensored",
            description = "Uncensored version of Llama 2",
            size = "3.8GB",
            sizeBytes = 3_800_000_000,
            family = "Llama",
            minRam = "6GB"
        ),
        OllamaModelInfo(
            id = "orca-mini",
            name = "orca-mini",
            displayName = "Orca Mini",
            description = "Microsoft's efficient model",
            size = "3.8GB",
            sizeBytes = 3_800_000_000,
            family = "Orca",
            minRam = "6GB"
        ),
        OllamaModelInfo(
            id = "wizardlm2",
            name = "wizardlm2",
            displayName = "WizardLM 2",
            description = "Microsoft's wizard model",
            size = "4.7GB",
            sizeBytes = 4_700_000_000,
            family = "Wizard",
            minRam = "6GB"
        )
    )

    suspend fun getLocalModels(): Result<List<LocalModel>> {
        return try {
            val response = api.getLocalModels()
            if (response.isSuccessful) {
                val models = response.body()?.models?.map { tag ->
                    LocalModel(
                        name = tag.name,
                        size = tag.size,
                        modifiedAt = tag.modifiedAt,
                        digest = tag.digest,
                        isDownloading = false,
                        downloadProgress = 0f
                    )
                } ?: emptyList()
                Result.success(models)
            } else {
                Result.failure(Exception("Failed to fetch models: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pullModel(modelName: String): Flow<DownloadProgress> = flow {
        emit(DownloadProgress(0f, "Starting download..."))
        
        try {
            val response = api.pullModel(PullRequest(modelName, false))
            if (response.isSuccessful) {
                response.body()?.let { pullResponse ->
                    if (pullResponse.total != null && pullResponse.total > 0) {
                        val progress = pullResponse.completed?.toFloat()?.div(pullResponse.total) ?: 0f
                        emit(DownloadProgress(progress, pullResponse.status ?: "Downloading..."))
                    } else {
                        emit(DownloadProgress(1f, "Complete"))
                    }
                }
                emit(DownloadProgress(1f, "Download complete"))
            } else {
                emit(DownloadProgress(0f, "Download failed"))
            }
        } catch (e: Exception) {
            emit(DownloadProgress(0f, "Error: ${e.message}"))
        }
    }

    suspend fun deleteModel(modelName: String): Result<Boolean> {
        return try {
            val response = api.deleteModel(DeleteModelRequest(modelName))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete model"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chat(model: String, messages: List<ChatMessage>): Result<ChatResult> {
        return try {
            val request = ChatRequest(
                model = model,
                messages = messages.map { com.ollama.mobile.data.model.Message(it.role, it.content) },
                stream = false
            )
            val response = api.chat(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(
                        ChatResult(
                            message = com.ollama.mobile.domain.model.ResponseMessage(
                                role = it.message.role,
                                content = it.message.content
                            ),
                            done = it.done,
                            totalDuration = it.totalDuration
                        )
                    )
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Chat failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkConnection(): Result<Boolean> {
        return try {
            val response = api.health()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateBaseUrl(url: String) {
        RetrofitClient.updateBaseUrl(url)
    }

    fun getBaseUrl(): String = RetrofitClient.getBaseUrl()
}

data class DownloadProgress(
    val progress: Float,
    val status: String
)
