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
            .readTimeout(0, TimeUnit.SECONDS) // No timeout for large file downloads
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
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
            id = "offline-phi3-mini-q4",
            name = "Phi-3-mini-4k-instruct-q4",
            displayName = "Phi-3 Mini 4K",
            description = "Microsoft Phi-3 Mini GGUF model for lightweight offline use.",
            size = "3.8GB",
            sizeBytes = 3_821_079_552,
            family = "Phi",
            minRam = "6GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf?download=true",
            fileName = "Phi-3-mini-4k-instruct-q4.gguf",
            sourceLabel = "Hugging Face"
        ),
        OfflineModelInfo(
            id = "offline-gemma2-2b-q4km",
            name = "gemma-2-2b-it-Q4_K_M",
            displayName = "Gemma 2 2B",
            description = "Gemma 2B instruct model in GGUF format for offline downloads.",
            size = "2.6GB",
            sizeBytes = 2_614_341_888,
            family = "Gemma",
            minRam = "4GB+ RAM recommended",
            sourceUrl = "https://huggingface.co/lmstudio-community/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf?download=true",
            fileName = "gemma-2-2b-it-Q4_K_M.gguf",
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

        if (targetFile.exists() && targetFile.length() > 0L) {
            saveDownloadedModel(model, targetFile)
            emit(OfflineDownloadProgress(progress = 1f, status = "Already downloaded"))
            return@flow
        }

        try {
            emit(OfflineDownloadProgress(progress = 0f, status = "Connecting to ${model.sourceLabel}..."))
            
            val request = Request.Builder()
                .url(model.sourceUrl)
                .header("Accept", "*/*")
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile) OllamaMobile/1.0")
                .build()

            val response = downloadClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw IllegalStateException("Download failed with HTTP ${response.code}: ${response.message}")
            }

            val body = response.body ?: throw IllegalStateException("Empty response body")
            val totalBytes = body.contentLength().takeIf { it > 0L } ?: model.sizeBytes

            emit(OfflineDownloadProgress(progress = 0f, status = "Downloading 0 / ${formatBytes(totalBytes)}"))

            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
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

    companion object {
        private const val KEY_DOWNLOADED_MODELS = "downloaded_offline_models"
    }
}

data class OfflineDownloadProgress(
    val progress: Float,
    val status: String
)
