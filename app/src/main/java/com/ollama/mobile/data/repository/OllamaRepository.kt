package com.ollama.mobile.data.repository

import com.ollama.mobile.data.api.OllamaApi
import com.ollama.mobile.data.api.RetrofitClient
import com.ollama.mobile.data.model.*
import com.ollama.mobile.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OllamaRepository(private val api: OllamaApi = RetrofitClient.getApi()) {

    suspend fun getModels(): Result<List<OllamaModel>> {
        return try {
            val response = api.getModels()
            if (response.isSuccessful) {
                val models = response.body()?.models?.map { it.toDomain() } ?: emptyList()
                Result.success(models)
            } else {
                Result.failure(Exception("Failed to fetch models: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chat(model: String, messages: List<ChatMessage>): Result<ChatResult> {
        return try {
            val request = ChatRequest(
                model = model,
                messages = messages.map { it.toData() },
                stream = false
            )
            val response = api.chat(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.toDomain())
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Chat failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generate(
        model: String,
        prompt: String,
        system: String? = null
    ): Result<GenerateResult> {
        return try {
            val request = GenerateRequest(
                model = model,
                prompt = prompt,
                system = system,
                stream = false
            )
            val response = api.generate(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.toDomain())
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Generate failed: ${response.code()}"))
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

    private fun ModelInfo.toDomain() = OllamaModel(
        name = name,
        modifiedAt = modifiedAt,
        size = size,
        digest = digest
    )

    private fun Message.toData() = com.ollama.mobile.data.model.Message(
        role = role,
        content = content
    )

    private fun ChatResponse.toDomain() = ChatResult(
        message = ResponseMessage(
            role = message.role,
            content = message.content
        ),
        done = done,
        totalDuration = totalDuration
    )

    private fun GenerateResponse.toDomain() = GenerateResult(
        response = response,
        done = done,
        totalDuration = totalDuration,
        evalCount = evalCount
    )
}
