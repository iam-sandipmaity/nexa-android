package com.ollama.mobile.data.api

import com.ollama.mobile.data.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var baseUrl = AppConfig.getBaseUrl()
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
        baseUrl = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
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
