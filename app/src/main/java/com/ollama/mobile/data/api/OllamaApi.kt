package com.ollama.mobile.data.api

import com.ollama.mobile.data.model.ChatRequest
import com.ollama.mobile.data.model.ChatResponse
import com.ollama.mobile.data.model.TagsResponse
import com.ollama.mobile.data.model.VersionResponse
import retrofit2.Response
import retrofit2.http.*

interface OllamaApi {

    @GET("/api/tags")
    suspend fun listModels(): Response<TagsResponse>

    @POST("/api/chat")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>

    @GET("/api/version")
    suspend fun version(): Response<VersionResponse>
}
