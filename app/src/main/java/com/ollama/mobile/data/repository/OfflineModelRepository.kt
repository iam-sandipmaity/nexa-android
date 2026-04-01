package com.ollama.mobile.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ollama.mobile.data.config.AppConfig
import com.ollama.mobile.domain.model.DownloadedOfflineModel
import com.ollama.mobile.domain.model.OfflineModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class OfflineModelRepository {

    private val context get() = AppConfig.getAppContext()
    private val gson = Gson()
    private val prefs by lazy {
        context.getSharedPreferences("offline_model_catalog", android.content.Context.MODE_PRIVATE)
    }
    private val downloadClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private val catalog = listOf(
        OfflineModelInfo(
            id = "offline-qwen25-05b-q4km",
            name = "Qwen2.5-0.5B-Instruct-Q4_K_M",
            displayName = "Qwen 2.5 0.5B",
            description = "Small mobile-friendly instruct model in GGUF format.",
            size = "494MB",
            sizeBytes = 494_032_768,
            family = "Qwen",
            minRam = "2GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/bartowski/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/Qwen2.5-0.5B-Instruct-Q4_K_M.gguf?download=true",
            fileName = "Qwen2.5-0.5B-Instruct-Q4_K_M.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-qwen25-15b-q4km",
            name = "Qwen2.5-1.5B-Instruct-Q4_K_M",
            displayName = "Qwen 2.5 1.5B",
            description = "Medium size Qwen model with better reasoning.",
            size = "1.1GB",
            sizeBytes = 1_100_000_000,
            family = "Qwen",
            minRam = "4GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/bartowski/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/Qwen2.5-1.5B-Instruct-Q4_K_M.gguf?download=true",
            fileName = "Qwen2.5-1.5B-Instruct-Q4_K_M.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-phi3-mini-q4",
            name = "Phi-3-mini-4k-instruct-q4",
            displayName = "Phi-3 Mini 4K",
            description = "Microsoft Phi-3 Mini GGUF model for lightweight offline use.",
            size = "2.3GB",
            sizeBytes = 2_300_000_000,
            family = "Phi",
            minRam = "4GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf?download=true",
            fileName = "Phi-3-mini-4k-instruct-q4.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-phi35-mini-q4",
            name = "Phi-3.5-mini-instruct-q4",
            displayName = "Phi-3.5 Mini",
            description = "Microsoft Phi-3.5 Mini with improved capabilities.",
            size = "2.4GB",
            sizeBytes = 2_400_000_000,
            family = "Phi",
            minRam = "4GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/bartowski/Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf?download=true",
            fileName = "Phi-3.5-mini-instruct-Q4_K_M.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-gemma2-2b-q4km",
            name = "gemma-2-2b-it-Q4_K_M",
            displayName = "Gemma 2 2B",
            description = "Gemma 2B instruct model in GGUF format for offline downloads.",
            size = "1.7GB",
            sizeBytes = 1_700_000_000,
            family = "Gemma",
            minRam = "4GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/lmstudio-community/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf?download=true",
            fileName = "gemma-2-2b-it-Q4_K_M.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-llama32-1b-q4km",
            name = "Llama-3.2-1B-Instruct-Q4_K_M",
            displayName = "Llama 3.2 1B",
            description = "Meta's latest small LLM optimized for mobile.",
            size = "770MB",
            sizeBytes = 770_000_000,
            family = "Llama",
            minRam = "2GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf?download=true",
            fileName = "Llama-3.2-1B-Instruct-Q4_K_M.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-llama32-3b-q4km",
            name = "Llama-3.2-3B-Instruct-Q4_K_M",
            displayName = "Llama 3.2 3B",
            description = "Meta's medium LLM with excellent performance.",
            size = "2.0GB",
            sizeBytes = 2_000_000_000,
            family = "Llama",
            minRam = "4GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf?download=true",
            fileName = "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-mistral-7b-q4km",
            name = "Mistral-7B-Instruct-v0.3-Q4_K_M",
            displayName = "Mistral 7B",
            description = "Mistral 7B instruct model for advanced tasks.",
            size = "4.4GB",
            sizeBytes = 4_400_000_000,
            family = "Mistral",
            minRam = "6GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/bartowski/Mistral-7B-Instruct-v0.3-GGUF/resolve/main/Mistral-7B-Instruct-v0.3-Q4_K_M.gguf?download=true",
            fileName = "Mistral-7B-Instruct-v0.3-Q4_K_M.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-deepseek-coder-1.3b-q4km",
            name = "DeepSeek-Coder-1.3B-Instruct-Q4_K_M",
            displayName = "DeepSeek Coder 1.3B",
            description = "Code-specialized model for programming tasks.",
            size = "980MB",
            sizeBytes = 980_000_000,
            family = "DeepSeek",
            minRam = "3GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/bartowski/DeepSeek-Coder-1.3B-Instruct-GGUF/resolve/main/DeepSeek-Coder-1.3B-Instruct-Q4_K_M.gguf?download=true",
            fileName = "DeepSeek-Coder-1.3B-Instruct-Q4_K_M.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-tinyllama-1.1b-q4km",
            name = "TinyLlama-1.1B-Chat-v1.0-Q4_K_M",
            displayName = "TinyLlama 1.1B",
            description = "Ultra-lightweight chat model for basic tasks.",
            size = "670MB",
            sizeBytes = 670_000_000,
            family = "Llama",
            minRam = "2GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf?download=true",
            fileName = "tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            sourceLabel = "Hugging Face"
        )
    )

    fun getCatalog(): List<OfflineModelInfo> = catalog

    fun getDownloadedModels(): List<DownloadedOfflineModel> {
        val json = prefs.getString(KEY_DOWNLOADED_MODELS, null).orEmpty()
        if (json.isBlank()) return emptyList()

        val type = object : TypeToken<List<DownloadedOfflineModel>>() {}.type
        val persisted = runCatching {
            gson.fromJson<List<DownloadedOfflineModel>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())

        val verified = persisted.filter { File(it.localPath).exists() }
        if (verified.size != persisted.size) {
            persistDownloadedModels(verified)
        }
        return verified
    }

    fun downloadModel(model: OfflineModelInfo): Flow<OfflineDownloadProgress> = flow {
        emit(OfflineDownloadProgress(progress = 0f, status = "Preparing download"))

        val modelDir = File(context.filesDir, "offline-models/${model.id}")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }

        val targetFile = File(modelDir, model.fileName)
        val tempFile = File(modelDir, "${model.fileName}.part")

        if (targetFile.exists() && targetFile.length() > 1000000L) {
            saveDownloadedModel(model, targetFile)
            emit(OfflineDownloadProgress(progress = 1f, status = "Already downloaded"))
            return@flow
        }

        try {
            emit(OfflineDownloadProgress(progress = 0f, status = "Connecting to ${model.sourceLabel}..."))
            
            val request = Request.Builder()
                .url(model.sourceUrl)
                .header("Accept", "*/*")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept-Encoding", "identity")
                .build()

            val response = downloadClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw IllegalStateException("Download failed with HTTP ${response.code}: ${response.message}")
            }

            val body = response.body ?: throw IllegalStateException("Empty response body")
            
            // Check if we got an HTML page instead of the actual file
            val contentType = response.header("Content-Type") ?: ""
            if (contentType.contains("text/html", ignoreCase = true)) {
                throw IllegalStateException("Download returned HTML instead of GGUF file. Make sure the URL points directly to a .gguf file (use /resolve/ not /blob/).")
            }
            
            val totalBytes = body.contentLength().takeIf { it > 0L } ?: model.sizeBytes

            emit(OfflineDownloadProgress(progress = 0f, status = "Downloading 0 / ${formatBytes(totalBytes)}"))

            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var downloadedBytes = 0L
                    var lastEmitTime = System.currentTimeMillis()

                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break

                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val progress = if (totalBytes > 0L) {
                            downloadedBytes.toFloat() / totalBytes.toFloat()
                        } else {
                            0f
                        }

                        // Only emit progress updates every 500ms to avoid flooding the UI
                        val now = System.currentTimeMillis()
                        if (now - lastEmitTime > 500 || progress >= 1f) {
                            emit(
                                OfflineDownloadProgress(
                                    progress = progress.coerceIn(0f, 1f),
                                    status = "Downloading ${formatBytes(downloadedBytes)} / ${formatBytes(totalBytes)}"
                                )
                            )
                            lastEmitTime = now
                        }
                    }

                    output.flush()
                }
            }

            if (targetFile.exists()) {
                targetFile.delete()
            }
            tempFile.renameTo(targetFile)
            
            // Verify the downloaded file is a valid GGUF file
            if (!isValidGGUF(targetFile)) {
                targetFile.delete()
                throw IllegalStateException("Downloaded file is not a valid GGUF model. Please check the URL points to a .gguf file.")
            }
            
            saveDownloadedModel(model, targetFile)
            emit(OfflineDownloadProgress(progress = 1f, status = "Download complete"))
        } catch (e: Exception) {
            // Clean up partial download on failure
            tempFile.delete()
            emit(OfflineDownloadProgress(progress = 0f, status = "Failed: ${e.message}"))
            throw e
        }
    }.flowOn(Dispatchers.IO)

    fun deleteDownloadedModel(modelId: String) {
        val model = getDownloadedModels().firstOrNull { it.id == modelId } ?: return
        File(model.localPath).delete()
        File(model.localPath).parentFile?.deleteRecursively()
        persistDownloadedModels(getDownloadedModels().filterNot { it.id == modelId })
    }

    private fun saveDownloadedModel(model: OfflineModelInfo, targetFile: File) {
        val updated = getDownloadedModels().filterNot { it.id == model.id } + DownloadedOfflineModel(
            id = model.id,
            name = model.name,
            displayName = model.displayName,
            fileName = model.fileName,
            localPath = targetFile.absolutePath,
            sizeBytes = targetFile.length().takeIf { it > 0L } ?: model.sizeBytes,
            downloadedAtMillis = System.currentTimeMillis(),
            sourceUrl = model.sourceUrl,
            sourceLabel = model.sourceLabel
        )
        persistDownloadedModels(updated.sortedBy { it.displayName })
    }

    private fun persistDownloadedModels(models: List<DownloadedOfflineModel>) {
        prefs.edit().putString(KEY_DOWNLOADED_MODELS, gson.toJson(models)).apply()
    }

    private fun formatBytes(sizeBytes: Long): String = when {
        sizeBytes >= 1_000_000_000 -> String.format("%.1fGB", sizeBytes / 1_000_000_000.0)
        sizeBytes >= 1_000_000 -> String.format("%.0fMB", sizeBytes / 1_000_000.0)
        else -> String.format("%dKB", sizeBytes / 1000)
    }
    
    private fun isValidGGUF(file: File): Boolean {
        return try {
            file.length() > 1000 && file.readBytes().sliceArray(0..3).contentEquals(byteArrayOf(0x47.toByte(), 0x47.toByte(), 0x55.toByte(), 0x46.toByte()))
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val KEY_DOWNLOADED_MODELS = "downloaded_offline_models"
    }
}

data class OfflineDownloadProgress(
    val progress: Float,
    val status: String
)
