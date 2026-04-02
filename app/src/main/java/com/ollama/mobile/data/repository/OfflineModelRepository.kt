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

    // ─── Built-in catalog ────────────────────────────────────────────────────

    private val builtInCatalog = listOf(
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

    // ─── Catalog (built-in + user-added) ─────────────────────────────────────

    fun getCatalog(): List<OfflineModelInfo> = builtInCatalog + getCustomModels()

    // ─── Custom model management ──────────────────────────────────────────────

    /**
     * Persist a user-supplied URL as a custom catalog entry so it shows up
     * in [getCatalog] and can be downloaded through the normal [downloadModel] flow.
     *
     * The function performs a lightweight HEAD request to resolve the real
     * filename and Content-Length before saving, so the caller gets accurate
     * metadata back. If the HEAD request fails (e.g. server doesn't support
     * it) we fall back to extracting the filename from the URL string itself.
     *
     * @param rawUrl  The direct-download URL entered by the user.
     * @return        The [OfflineModelInfo] that was stored, ready for download.
     * @throws IllegalArgumentException if the URL is blank or not HTTPS/HTTP.
     * @throws IllegalStateException    if a model with the same derived ID is
     *                                  already present in the catalog.
     */
    suspend fun addCustomModel(rawUrl: String): OfflineModelInfo {
        val url = rawUrl.trim()
        require(url.isNotBlank()) { "URL must not be blank" }
        require(url.startsWith("http://") || url.startsWith("https://")) {
            "URL must start with http:// or https://"
        }

        // ── Step 1: probe the URL for real metadata ───────────────────────
        val (resolvedFileName, resolvedSizeBytes, resolvedLabel) = probeUrl(url)

        // ── Step 2: derive stable ID from file name ───────────────────────
        val safeId = "custom-${resolvedFileName.lowercase().replace(Regex("[^a-z0-9._-]"), "-")}"

        // ── Step 3: reject duplicates ─────────────────────────────────────
        val allIds = getCatalog().map { it.id }
        check(safeId !in allIds) { "A model with this file is already in the catalog." }

        // ── Step 4: infer display metadata from file name ─────────────────
        val displayName = resolvedFileName
            .removeSuffix(".gguf")
            .replace(Regex("[-_]+"), " ")
            .split(" ")
            .joinToString(" ") { word ->
                if (word.length <= 3 && word.all { it.isLetter() }) word.uppercase()
                else word.replaceFirstChar { it.uppercase() }
            }

        val family = inferFamily(resolvedFileName)
        val sizeLabel = if (resolvedSizeBytes > 0L) formatBytes(resolvedSizeBytes) else "Unknown"
        val ramHint = when {
            resolvedSizeBytes <= 800_000_000L  -> "2GB+ RAM recommended"
            resolvedSizeBytes <= 2_000_000_000L -> "4GB+ RAM recommended"
            resolvedSizeBytes <= 4_000_000_000L -> "6GB+ RAM recommended"
            else                               -> "8GB+ RAM recommended"
        }

        val model = OfflineModelInfo(
            id          = safeId,
            name        = resolvedFileName.removeSuffix(".gguf"),
            displayName = displayName,
            description = "Custom model added by user.",
            size        = sizeLabel,
            sizeBytes   = resolvedSizeBytes,
            family      = family,
            minRam      = ramHint,
            sourceUrl   = url,
            fileName    = resolvedFileName,
            sourceLabel = resolvedLabel
        )

        // ── Step 5: persist ───────────────────────────────────────────────
        val updated = getCustomModels() + model
        prefs.edit().putString(KEY_CUSTOM_MODELS, gson.toJson(updated)).commit()

        return model
    }

    /** Remove a user-added custom model entry from the catalog (does not delete any downloaded file). */
    fun removeCustomModel(modelId: String) {
        val updated = getCustomModels().filterNot { it.id == modelId }
        prefs.edit().putString(KEY_CUSTOM_MODELS, gson.toJson(updated)).commit()
    }

    private fun getCustomModels(): List<OfflineModelInfo> {
        val json = prefs.getString(KEY_CUSTOM_MODELS, null).orEmpty()
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<OfflineModelInfo>>() {}.type
        return runCatching {
            gson.fromJson<List<OfflineModelInfo>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    /**
     * Sends a HEAD request to discover the real filename and file size.
     */
    private fun probeUrl(url: String): Triple<String, Long, String> {
        val label = when {
            url.contains("huggingface.co", ignoreCase = true) -> "Hugging Face"
            url.contains("github.com", ignoreCase = true)     -> "GitHub"
            url.contains("ollama.com", ignoreCase = true)     -> "Ollama"
            else -> url.substringAfter("://").substringBefore("/")
        }

        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            downloadClient.newCall(request).execute().use { resp ->
                val contentDisposition = resp.header("Content-Disposition")
                val fileName = extractFileName(contentDisposition, url)
                val sizeBytes = resp.header("Content-Length")?.toLongOrNull() ?: 0L
                Triple(fileName, sizeBytes, label)
            }
        } catch (_: Exception) {
            val fileName = extractFileName(null, url)
            Triple(fileName, 0L, label)
        }
    }

    private fun extractFileName(contentDisposition: String?, url: String): String {
        if (!contentDisposition.isNullOrBlank()) {
            val cdFileName = Regex("""filename\*?=(?:UTF-8'')?["']?([^"';\s]+)["']?""", RegexOption.IGNORE_CASE)
                .find(contentDisposition)
                ?.groupValues
                ?.getOrNull(1)
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            if (!cdFileName.isNullOrBlank()) return sanitizeFileName(cdFileName)
        }

        val path = url.substringBefore("?").trimEnd('/')
        val rawName = path.substringAfterLast("/")
        val decoded = runCatching { java.net.URLDecoder.decode(rawName, "UTF-8") }.getOrDefault(rawName)
        return sanitizeFileName(decoded.ifBlank { "custom-model.gguf" })
    }

    private fun sanitizeFileName(name: String): String =
        name.replace(Regex("""[\\/:*?"<>|]"""), "_").trim()

    private fun inferFamily(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            "llama"     in lower -> "Llama"
            "mistral"   in lower -> "Mistral"
            "phi"       in lower -> "Phi"
            "gemma"     in lower -> "Gemma"
            "qwen"      in lower -> "Qwen"
            "deepseek"  in lower -> "DeepSeek"
            "falcon"    in lower -> "Falcon"
            "mpt"       in lower -> "MPT"
            "stablelm"  in lower -> "StableLM"
            "tinyllama" in lower -> "Llama"
            else                 -> "Custom"
        }
    }

    // ─── Downloaded model management ─────────────────────────────────────────

    fun getDownloadedModels(): List<DownloadedOfflineModel> {
        val json = prefs.getString(KEY_DOWNLOADED_MODELS, null).orEmpty()
        if (json.isBlank()) return emptyList()

        val type = object : TypeToken<List<DownloadedOfflineModel>>() {}.type
        val persisted = runCatching {
            gson.fromJson<List<DownloadedOfflineModel>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())

        val verified = persisted.filter { File(it.localPath).exists() }
        if (verified.size != persisted.size) persistDownloadedModels(verified)
        return verified
    }

    // ─── Download ─────────────────────────────────────────────────────────────

    fun downloadModel(model: OfflineModelInfo): Flow<OfflineDownloadProgress> = flow {
        emit(OfflineDownloadProgress(progress = 0f, status = "Preparing download"))

        val modelDir = File(context.filesDir, "offline-models/${model.id}")
        if (!modelDir.exists()) modelDir.mkdirs()

        val targetFile = File(modelDir, model.fileName)
        val tempFile   = File(modelDir, "${model.fileName}.part")

        if (targetFile.exists() && targetFile.length() > 1_000_000L) {
            saveDownloadedModel(model, targetFile)
            emit(OfflineDownloadProgress(progress = 1f, status = "Already downloaded"))
            return@flow
        }

        try {
            emit(OfflineDownloadProgress(progress = 0f, status = "Connecting to ${model.sourceLabel}…"))

            val request = Request.Builder()
                .url(model.sourceUrl)
                .header("Accept", "*/*")
                .build()

            val response = downloadClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Download failed with HTTP ${response.code}: ${response.message}"
                )
            }

            val body = response.body
                ?: throw IllegalStateException("Empty response body")

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
                            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        val now = System.currentTimeMillis()
                        if (now - lastEmitTime > 500 || progress >= 1f) {
                            emit(
                                OfflineDownloadProgress(
                                    progress = progress,
                                    status = "Downloading ${formatBytes(downloadedBytes)} / ${formatBytes(totalBytes)}"
                                )
                            )
                            lastEmitTime = now
                        }
                    }

                    output.flush()
                }
            }

            if (targetFile.exists()) targetFile.delete()
            val renamed = tempFile.renameTo(targetFile)
            if (!renamed) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            // Update the stored path to the final (renamed) file.
            saveDownloadedModel(model, targetFile)

            emit(OfflineDownloadProgress(progress = 1f, status = "Download complete"))
        } catch (e: Exception) {
            tempFile.delete()
            emit(OfflineDownloadProgress(progress = 0f, status = "Failed: ${e.message}"))
            throw e
        }
    }.flowOn(Dispatchers.IO)

    // ─── Delete ───────────────────────────────────────────────────────────────

    fun deleteDownloadedModel(modelId: String) {
        val model = getDownloadedModels().firstOrNull { it.id == modelId } ?: return
        File(model.localPath).delete()
        File(model.localPath).parentFile?.deleteRecursively()
        persistDownloadedModels(getDownloadedModels().filterNot { it.id == modelId })
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun saveDownloadedModel(model: OfflineModelInfo, targetFile: File) {
        val updated = getDownloadedModels().filterNot { it.id == model.id } +
            DownloadedOfflineModel(
                id               = model.id,
                name             = model.name,
                displayName      = model.displayName,
                fileName         = model.fileName,
                localPath        = targetFile.absolutePath,
                sizeBytes        = targetFile.length().takeIf { it > 0L } ?: model.sizeBytes,
                downloadedAtMillis = System.currentTimeMillis(),
                sourceUrl        = model.sourceUrl,
                sourceLabel      = model.sourceLabel
            )
        persistDownloadedModels(updated.sortedBy { it.displayName })
    }

    private fun persistDownloadedModels(models: List<DownloadedOfflineModel>) {
        prefs.edit().putString(KEY_DOWNLOADED_MODELS, gson.toJson(models)).commit()
    }

    private fun formatBytes(sizeBytes: Long): String = when {
        sizeBytes >= 1_000_000_000L -> String.format("%.1fGB", sizeBytes / 1_000_000_000.0)
        sizeBytes >= 1_000_000L     -> String.format("%.0fMB", sizeBytes / 1_000_000.0)
        else                        -> String.format("%dKB", sizeBytes / 1000)
    }

    companion object {
        private const val KEY_DOWNLOADED_MODELS = "downloaded_offline_models"
        private const val KEY_CUSTOM_MODELS     = "custom_model_catalog"
    }
}

data class OfflineDownloadProgress(
    val progress: Float,
    val status: String
)
