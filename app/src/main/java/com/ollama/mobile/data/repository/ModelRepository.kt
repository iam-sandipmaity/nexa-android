package com.ollama.mobile.data.repository

import com.ollama.mobile.data.api.OllamaLibraryClient
import com.ollama.mobile.data.api.RetrofitClient
import com.ollama.mobile.data.config.AppConfig
import com.ollama.mobile.data.model.ChatRequest
import com.ollama.mobile.data.model.Message
import com.ollama.mobile.data.model.ModelTag
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.domain.model.ChatResult
import com.ollama.mobile.domain.model.LibraryModelInfo
import com.ollama.mobile.domain.model.ModelSource
import com.ollama.mobile.domain.model.OllamaModelInfo

class ModelRepository {

    private val api get() = RetrofitClient.getApi()
    private val libraryApi get() = OllamaLibraryClient.getLibraryApi()

    val curatedModels = listOf(
        OllamaModelInfo("llama3.2", "llama3.2", "Llama 3.2", "Latest Meta LLM with excellent reasoning", "2.0GB", 2_000_000_000, "Llama", "Cloud hosted", ModelSource.CURATED),
        OllamaModelInfo("llama3.1", "llama3.1", "Llama 3.1", "Powerful open-source LLM by Meta", "4.3GB", 4_300_000_000, "Llama", "Cloud hosted", ModelSource.CURATED),
        OllamaModelInfo("gemma2:2b", "gemma2:2b", "Gemma 2B", "Google's efficient 2B model", "1.6GB", 1_600_000_000, "Gemma", "Cloud hosted", ModelSource.CURATED),
        OllamaModelInfo("gemma2:9b", "gemma2:9b", "Gemma 9B", "Google's powerful 9B model", "5.2GB", 5_200_000_000, "Gemma", "Cloud hosted", ModelSource.CURATED),
        OllamaModelInfo("mistral", "mistral", "Mistral", "Excellent for instruction following", "4.1GB", 4_100_000_000, "Mistral", "Cloud hosted", ModelSource.CURATED),
        OllamaModelInfo("qwen2.5", "qwen2.5", "Qwen 2.5", "Alibaba's multilingual model", "3.3GB", 3_300_000_000, "Qwen", "Cloud hosted", ModelSource.CURATED),
        OllamaModelInfo("codellama", "codellama", "Code Llama", "Specialized for code generation", "3.8GB", 3_800_000_000, "Llama", "Cloud hosted", ModelSource.CURATED),
        OllamaModelInfo("phi3.5", "phi3.5", "Phi-3.5", "Microsoft's latest small model", "2.2GB", 2_200_000_000, "Phi", "Cloud hosted", ModelSource.CURATED),
        OllamaModelInfo("tinyllama", "tinyllama", "TinyLlama", "Ultra-lightweight, 1.1B params", "637MB", 637_000_000, "Llama", "Cloud hosted", ModelSource.CURATED)
    )

    suspend fun searchLibraryModels(query: String): Result<List<LibraryModelInfo>> {
        return try {
            val response = libraryApi.searchModels(query)
            if (response.isSuccessful) {
                val models = response.body()?.models?.map { libraryModel ->
                    LibraryModelInfo(
                        id = libraryModel.name,
                        name = libraryModel.name,
                        displayName = formatLibraryDisplayName(libraryModel.name),
                        description = libraryModel.description ?: "An Ollama library model",
                        size = formatSize(libraryModel.size ?: 0),
                        sizeBytes = libraryModel.size ?: 0,
                        family = extractFamily(libraryModel.name),
                        minRam = "Cloud",
                        pullCount = libraryModel.pullCount ?: 0,
                        verified = libraryModel.verified ?: false,
                        tags = libraryModel.tags ?: emptyList()
                    )
                }?.sortedByDescending { it.pullCount } ?: emptyList()
                Result.success(models)
            } else {
                Result.failure(Exception("Failed to search library: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllLibraryModels(): Result<List<LibraryModelInfo>> {
        return try {
            val response = libraryApi.searchModels("")
            if (response.isSuccessful) {
                val models = response.body()?.models?.map { libraryModel ->
                    LibraryModelInfo(
                        id = libraryModel.name,
                        name = libraryModel.name,
                        displayName = formatLibraryDisplayName(libraryModel.name),
                        description = libraryModel.description ?: "An Ollama library model",
                        size = formatSize(libraryModel.size ?: 0),
                        sizeBytes = libraryModel.size ?: 0,
                        family = extractFamily(libraryModel.name),
                        minRam = "Cloud",
                        pullCount = libraryModel.pullCount ?: 0,
                        verified = libraryModel.verified ?: false,
                        tags = libraryModel.tags ?: emptyList()
                    )
                }?.sortedByDescending { it.pullCount } ?: emptyList()
                Result.success(models)
            } else {
                Result.failure(Exception("Failed to load library: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableModels(): Result<List<OllamaModelInfo>> {
        return try {
            if (!hasApiKey()) {
                return Result.failure(IllegalStateException("Add your Ollama API key to load cloud models."))
            }

            val response = api.listModels()
            if (!response.isSuccessful) {
                return Result.failure(Exception("Failed to load cloud models: ${response.code()}"))
            }

            val models = response.body()?.models?.map(::mapRemoteModel)?.sortedBy { it.displayName }.orEmpty()
            if (models.isEmpty()) Result.failure(Exception("No models available for this account"))
            else Result.success(models)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chat(model: String, messages: List<ChatMessage>): Result<ChatResult> {
        return try {
            if (!hasApiKey()) {
                return Result.failure(IllegalStateException("Add your Ollama API key in Settings before chatting."))
            }

            val modelName = model
                .removePrefix("library:")
                .removeSuffix("-cloud")
                .let { if (it.contains(":")) it.substringBefore(":") else it }

            val response = api.chat(
                ChatRequest(
                    model = modelName,
                    messages = messages.map { Message(it.role, it.content) },
                    stream = false
                )
            )

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
            if (!hasApiKey()) {
                return Result.failure(IllegalStateException("Missing API key"))
            }
            Result.success(api.version().isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFallbackModels(): List<OllamaModelInfo> = curatedModels

    fun hasApiKey(): Boolean = AppConfig.hasApiKey()

    fun updateBaseUrl(url: String) {
        AppConfig.updateBaseUrl(url)
        RetrofitClient.updateBaseUrl(url)
    }

    fun getBaseUrl(): String = AppConfig.getBaseUrl()

    fun updateApiKey(apiKey: String) {
        AppConfig.updateApiKey(apiKey)
        RetrofitClient.updateApiKey(apiKey)
    }

    fun getApiKey(): String = AppConfig.getApiKey()

    private fun mapRemoteModel(tag: ModelTag): OllamaModelInfo {
        val curated = curatedModels.firstOrNull { it.name == tag.name }
            ?: curatedModels.firstOrNull { tag.name.startsWith(it.name.substringBefore(":")) }

        val family = tag.details?.family?.replaceFirstChar { it.uppercase() }
            ?: curated?.family
            ?: "Cloud"

        return OllamaModelInfo(
            id = tag.model ?: tag.name,
            name = tag.name,
            displayName = curated?.displayName ?: formatDisplayName(tag.name),
            description = curated?.description ?: "Use this model directly through Ollama Cloud.",
            size = formatSize(tag.size),
            sizeBytes = tag.size,
            family = family,
            minRam = curated?.minRam ?: "Cloud hosted"
        )
    }

    private fun formatSize(sizeBytes: Long): String = when {
        sizeBytes >= 1_000_000_000 -> String.format("%.1fGB", sizeBytes / 1_000_000_000.0)
        sizeBytes >= 1_000_000 -> String.format("%.0fMB", sizeBytes / 1_000_000.0)
        else -> String.format("%dKB", sizeBytes / 1000)
    }

    private fun formatDisplayName(name: String): String =
        name.split(":", "-", "_", ".")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }

    private fun formatLibraryDisplayName(name: String): String {
        val parts = name.split(":")
        val baseName = parts[0].replace("-", " ").replace("_", " ")
            .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        return if (parts.size > 1) "$baseName (${parts[1]})" else baseName
    }

    private fun extractFamily(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("llama") -> "Llama"
            lower.contains("gemma") -> "Gemma"
            lower.contains("mistral") || lower.contains("mixtral") -> "Mistral"
            lower.contains("qwen") -> "Qwen"
            lower.contains("phi") -> "Phi"
            lower.contains("codellama") || lower.contains("code") -> "CodeLlama"
            lower.contains("deepseek") -> "DeepSeek"
            lower.contains("nemotron") -> "Nemotron"
            lower.contains("aya") -> "Aya"
            lower.contains("command") -> "Command"
            else -> "Model"
        }
    }
}
