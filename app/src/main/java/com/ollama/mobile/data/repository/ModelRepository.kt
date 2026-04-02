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

    val libraryModels = listOf(
        LibraryModelInfo("llama3.1", "llama3.1", "Llama 3.1", "Meta's latest state-of-the-art model in 8B, 70B and 405B", "4.7GB", 4700000000, "Llama", "Cloud", 112500000, true, listOf("8b", "70b", "405b")),
        LibraryModelInfo("deepseek-r1", "deepseek-r1", "DeepSeek R1", "Open reasoning model with performance approaching leading models", "4.7GB", 4700000000, "DeepSeek", "Cloud", 81700000, true, listOf("1.5b", "7b", "8b", "14b", "32b", "70b", "671b")),
        LibraryModelInfo("llama3.2", "llama3.2", "Llama 3.2", "Meta's latest with 1B and 3B parameter models", "2.0GB", 2000000000, "Llama", "Cloud", 63500000, true, listOf("1b", "3b")),
        LibraryModelInfo("nomic-embed-text", "nomic-embed-text", "Nomic Embed Text", "High-performing open embedding model with large token context", "274MB", 274000000, "Nomic", "Cloud", 62100000, false, listOf("embedding")),
        LibraryModelInfo("gemma3", "gemma3", "Gemma 3", "Google's most capable model that runs on single GPU", "8.4GB", 8400000000, "Gemma", "Cloud", 34800000, true, listOf("270m", "1b", "4b", "12b", "27b")),
        LibraryModelInfo("mistral", "mistral", "Mistral", "7B model by Mistral AI, updated to v0.3", "4.1GB", 4100000000, "Mistral", "Cloud", 27700000, true, listOf("7b")),
        LibraryModelInfo("qwen2.5", "qwen2.5", "Qwen 2.5", "Alibaba's pretrained models with up to 18T tokens", "4.7GB", 4700000000, "Qwen", "Cloud", 26500000, true, listOf("0.5b", "1.5b", "3b", "7b", "14b", "32b", "72b")),
        LibraryModelInfo("qwen3", "qwen3", "Qwen 3", "Latest generation with dense and MoE models", "4.7GB", 4700000000, "Qwen", "Cloud", 25600000, true, listOf("0.6b", "1.7b", "4b", "8b", "14b", "30b", "32b", "235b")),
        LibraryModelInfo("llama3", "llama3", "Llama 3", "Meta's most capable openly available LLM", "4.7GB", 4700000000, "Llama", "Cloud", 21300000, true, listOf("8b", "70b")),
        LibraryModelInfo("gemma2", "gemma2", "Gemma 2", "Google's high-performing 2B, 9B, and 27B models", "5.5GB", 5500000000, "Gemma", "Cloud", 18500000, true, listOf("2b", "9b", "27b")),
        LibraryModelInfo("phi4", "phi4", "Phi-4", "Microsoft's latest small model with strong reasoning", "2.3GB", 2300000000, "Phi", "Cloud", 15000000, true, listOf("14b")),
        LibraryModelInfo("llama3.2-vision", "llama3.2-vision", "Llama 3.2 Vision", "Instruction-tuned image reasoning models 11B and 90B", "13GB", 13000000000, "Llama", "Cloud", 4200000, true, listOf("11b", "90b")),
        LibraryModelInfo("llama3.3", "llama3.3", "Llama 3.3", "State of the art 70B similar performance to 405B", "42GB", 42000000000, "Llama", "Cloud", 3600000, true, listOf("70b")),
        LibraryModelInfo("llama4", "llama4", "Llama 4", "Meta's latest collection of multimodal models", "50GB", 50000000000, "Llama", "Cloud", 1500000, true, listOf("16x17b", "128x17b")),
        LibraryModelInfo("deepseek-coder-v2", "deepseek-coder-v2", "DeepSeek Coder V2", "Open source code model with excellent performance", "16GB", 16000000000, "DeepSeek", "Cloud", 890000, true, listOf("16b", "24b")),
        LibraryModelInfo("codellama", "codellama", "Code Llama", "Meta's code-specialized model", "3.8GB", 3800000000, "Llama", "Cloud", 8500000, true, listOf("7b", "13b", "34b", "70b")),
        LibraryModelInfo("mistral-small", "mistral-small", "Mistral Small", "Efficient model for rapid responses", "1.1GB", 1100000000, "Mistral", "Cloud", 7500000, true, listOf("22b")),
        LibraryModelInfo("mixtral", "mixtral", "Mixtral", "Mixture of experts model by Mistral", "26GB", 26000000000, "Mistral", "Cloud", 6500000, true, listOf("8x7b")),
        LibraryModelInfo("aya", "aya", "Aya", "Cohere's multilingual instruction model", "13GB", 13000000000, "Aya", "Cloud", 3200000, false, listOf("8b", "32b", "105b")),
        LibraryModelInfo("command-r", "command-r", "Command R", "Cohere's model for enterprise RAG", "35GB", 35000000000, "Command", "Cloud", 2800000, true, listOf("35b", "104b")),
        LibraryModelInfo("command-r-plus", "command-r-plus", "Command R+", "Cohere's most capable model for RAG", "70GB", 70000000000, "Command", "Cloud", 1800000, true, listOf("104b")),
        LibraryModelInfo("phi3", "phi3", "Phi-3", "Microsoft's small language model", "2.3GB", 2300000000, "Phi", "Cloud", 5000000, true, listOf("3b", "4b", "14b")),
        LibraryModelInfo("starling-lm", "starling-lm", "Starling LM", "Open model with excellent helpfulness", "7.7GB", 7700000000, "Starling", "Cloud", 2100000, false, listOf("7b")),
        LibraryModelInfo("neural-chat", "neural-chat", "Neural Chat", "Intel's chat model optimized for conversation", "4.7GB", 4700000000, "Intel", "Cloud", 1500000, false, listOf("7b", "14b")),
        LibraryModelInfo("samantha-mistral", "samantha-mistral", "Samantha Mistral", "Mistral-based model with roleplay capabilities", "4.1GB", 4100000000, "Mistral", "Cloud", 890000, false, listOf("7b")),
        LibraryModelInfo("wizardlm2", "wizardlm2", "WizardLM 2", "Microsoft's powerful instruction model", "8.9GB", 8900000000, "Wizard", "Cloud", 2800000, false, listOf("7b", "8x22b")),
        LibraryModelInfo("wizardmath", "wizardmath", "WizardMath", "Math-specialized model from WizardLM series", "4.7GB", 4700000000, "Wizard", "Cloud", 750000, false, listOf("7b", "70b", "110b")),
        LibraryModelInfo("mathstral", "mathstral", "Mathstral", "Math-specialized model by Mistral", "4.1GB", 4100000000, "Mistral", "Cloud", 680000, false, listOf("7b")),
        LibraryModelInfo("openchat", "openchat", "OpenChat", "Open source chat model", "7.7GB", 7700000000, "OpenChat", "Cloud", 2100000, false, listOf("7b")),
        LibraryModelInfo("zephyr", "zephyr", "Zephyr", "Mistral-based instruction model", "4.1GB", 4100000000, "Mistral", "Cloud", 4200000, false, listOf("7b")),
        LibraryModelInfo("tinyllama", "tinyllama", "TinyLlama", "Ultra-lightweight 1.1B model", "637MB", 637000000, "Llama", "Cloud", 15000000, false, listOf("1.1b")),
        LibraryModelInfo("phi3.5", "phi3.5", "Phi-3.5", "Microsoft's latest small model", "2.2GB", 2200000000, "Phi", "Cloud", 5000000, true, listOf("3b", "4b")),
        LibraryModelInfo("llama2-uncensored", "llama2-uncensored", "Llama 2 Uncensored", "Uncensored version of Llama 2", "3.8GB", 3800000000, "Llama", "Cloud", 2300000, false, listOf("7b", "13b", "70b")),
        LibraryModelInfo("dolphin-phi", "dolphin-phi", "Dolphin Phi", "Phi-based model with special capabilities", "1.6GB", 1600000000, "Dolphin", "Cloud", 890000, false, listOf("2.8b")),
        LibraryModelInfo("falcon", "falcon", "Falcon", "TII's language model", "39GB", 39000000000, "Falcon", "Cloud", 3200000, false, listOf("7b", "40b", "180b")),
        LibraryModelInfo("falcon2", "falcon2", "Falcon 2", "TII's latest language model", "11GB", 11000000000, "Falcon", "Cloud", 950000, true, listOf("11b")),
        LibraryModelInfo("smollm", "smollm", "SmolLM", "Meta's small models with 135M, 360M, and 1.7B", "126MB", 126000000, "Llama", "Cloud", 1800000, false, listOf("135m", "360m", "1.7b")),
        LibraryModelInfo("gemma3-it", "gemma3-it", "Gemma 3 IT", "Instruction-tuned Gemma 3", "8.4GB", 8400000000, "Gemma", "Cloud", 2500000, true, listOf("4b", "12b", "27b")),
        LibraryModelInfo("llama3-gradient", "llama3-gradient", "Llama 3 Gradient", "Continuously trained Llama 3", "4.7GB", 4700000000, "Llama", "Cloud", 680000, false, listOf("8b", "70b"))
    )

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
            val searchLower = query.lowercase()
            val results = if (searchLower.isBlank() || searchLower == "a") {
                libraryModels.sortedByDescending { it.pullCount }
            } else {
                libraryModels.filter {
                    it.name.contains(searchLower, ignoreCase = true) ||
                    it.displayName.contains(searchLower, ignoreCase = true) ||
                    it.description.contains(searchLower, ignoreCase = true) ||
                    it.family.contains(searchLower, ignoreCase = true)
                }.sortedByDescending { it.pullCount }
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllLibraryModels(): Result<List<LibraryModelInfo>> {
        return try {
            Result.success(libraryModels.sortedByDescending { it.pullCount })
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
                .trim()

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
                val errorMsg = when (response.code()) {
                    404 -> "Model not found on Ollama Cloud. Some models need to be pulled first using 'ollama pull $modelName'"
                    401 -> "Invalid API key. Please check your Ollama API key in Settings."
                    429 -> "Rate limit exceeded. Please wait and try again."
                    500, 502, 503 -> "Ollama Cloud service error. Please try again later."
                    else -> "Chat failed: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMsg))
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
            minRam = curated?.minRam ?: "Cloud hosted",
            source = ModelSource.CLOUD
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