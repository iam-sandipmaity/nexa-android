package com.ollama.mobile.data.api

import com.ollama.mobile.data.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val DEFAULT_BASE_URL = "https://ollama.com/"

    private fun normalizeBaseUrlOrNull(rawUrl: String): String? {
        val candidate = rawUrl.trim().ifBlank { DEFAULT_BASE_URL }
        val withScheme = if (candidate.startsWith("http://") || candidate.startsWith("https://")) {
            candidate
        } else {
            "https://$candidate"
        }
        val normalized = if (withScheme.endsWith('/')) withScheme else "$withScheme/"
        val uri = runCatching { URI(normalized) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") return null
        if (uri.host.isNullOrBlank()) return null
        return normalized
    }

    private var baseUrl = normalizeBaseUrlOrNull(AppConfig.getBaseUrl()) ?: DEFAULT_BASE_URL
    private var apiKey = AppConfig.getApiKey()

    private fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)

            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                if (apiKey.isNotBlank()) {
                    requestBuilder.header("Authorization", "Bearer $apiKey")
                }
                chain.proceed(requestBuilder.build())
            }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        builder.addInterceptor(loggingInterceptor)

        return builder.build()
    }

    private var okHttpClient: OkHttpClient = createOkHttpClient()

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private var retrofit: Retrofit = createRetrofit()

    fun updateBaseUrl(newBaseUrl: String) {
        val normalized = normalizeBaseUrlOrNull(newBaseUrl) ?: return
        baseUrl = normalized
        okHttpClient = createOkHttpClient()
        retrofit = createRetrofit()
    }

    fun updateApiKey(newApiKey: String) {
        apiKey = newApiKey.trim()
        okHttpClient = createOkHttpClient()
        retrofit = createRetrofit()
    }

    fun getApi(): OllamaApi {
        return retrofit.create(OllamaApi::class.java)
    }

    fun getBaseUrl(): String = baseUrl
    fun getApiKey(): String = apiKey
}
