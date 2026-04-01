package com.ollama.mobile.data.api

import com.ollama.mobile.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface OllamaApi {

    @GET("/api/tags")
    suspend fun getLocalModels(): Response<TagsResponse>

    @POST("/api/pull")
    suspend fun pullModel(@Body request: PullRequest): Response<PullResponse>

    @DELETE("/api/delete")
    suspend fun deleteModel(@Body request: DeleteModelRequest): Response<DeleteResponse>

    @GET("/api/show")
    suspend fun getModelInfo(@Query("name") name: String): Response<ModelDetail>

    @GET("/")
    suspend fun health(): Response<Unit>
}
