package com.ollama.mobile.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class OllamaModelInfo(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val size: String,
    val sizeBytes: Long,
    val family: String,
    val minRam: String
) {
    val logo: ImageVector
        get() = familyLogos[family.lowercase()] ?: Icons.Default.Psychology
}

data class OfflineModelInfo(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val size: String,
    val sizeBytes: Long,
    val family: String,
    val minRam: String,
    val sourceUrl: String,
    val fileName: String,
    val sourceLabel: String
) {
    val logo: ImageVector
        get() = familyLogos[family.lowercase()] ?: Icons.Default.Psychology
}

val familyLogos = mapOf(
    "llama" to Icons.Default.Pets,
    "llama3" to Icons.Default.Pets,
    "gemma" to Icons.Default.Diamond,
    "mistral" to Icons.Default.Cloud,
    "mixtral" to Icons.Default.Cloud,
    "qwen" to Icons.Default.AutoAwesome,
    "phi" to Icons.Default.Memory,
    "codellama" to Icons.Default.Code,
    "deepseek" to Icons.Default.Psychology,
    "default" to Icons.Default.Psychology
)

data class DownloadedOfflineModel(
    val id: String,
    val name: String,
    val displayName: String,
    val fileName: String,
    val localPath: String,
    val sizeBytes: Long,
    val downloadedAtMillis: Long,
    val sourceUrl: String,
    val sourceLabel: String
) {
    val formattedSize: String
        get() = when {
            sizeBytes >= 1_000_000_000 -> String.format("%.1fGB", sizeBytes / 1_000_000_000.0)
            sizeBytes >= 1_000_000 -> String.format("%.0fMB", sizeBytes / 1_000_000.0)
            else -> String.format("%dKB", sizeBytes / 1000)
        }
    
    val family: String
        get() = extractFamily(name)
    
    val logo: ImageVector
        get() = familyLogos[family.lowercase()] ?: Icons.Default.CloudDownload
    
    private fun extractFamily(modelName: String): String {
        val lower = modelName.lowercase()
        return when {
            lower.contains("llama") -> "llama"
            lower.contains("gemma") -> "gemma"
            lower.contains("mistral") || lower.contains("mixtral") -> "mistral"
            lower.contains("qwen") -> "qwen"
            lower.contains("phi") -> "phi"
            lower.contains("codellama") || lower.contains("code") -> "codellama"
            lower.contains("deepseek") -> "deepseek"
            else -> "default"
        }
    }
}

data class LocalModel(
    val name: String,
    val size: Long,
    val modifiedAt: String?,
    val digest: String?,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f
) {
    val formattedSize: String
        get() = when {
            size >= 1_000_000_000 -> String.format("%.1fGB", size / 1_000_000_000.0)
            size >= 1_000_000 -> String.format("%.0fMB", size / 1_000_000.0)
            else -> String.format("%dKB", size / 1000)
        }
}

data class ChatMessage(
    val role: String,
    val content: String
)

data class ResponseMessage(
    val role: String,
    val content: String
)

data class ChatResult(
    val message: ResponseMessage,
    val done: Boolean,
    val totalDuration: Long?
)

enum class ModelFamily(val displayName: String) {
    LLAMA("Llama"),
    GEMMA("Gemma"),
    MISTRAL("Mistral"),
    QWEN("Qwen"),
    PHI("Phi"),
    OTHER("Other")
}
