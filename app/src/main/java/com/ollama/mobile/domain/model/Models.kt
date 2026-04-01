package com.ollama.mobile.domain.model

data class OllamaModelInfo(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val size: String,
    val sizeBytes: Long,
    val family: String,
    val minRam: String
)

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
