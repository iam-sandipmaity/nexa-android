package com.ollama.mobile.data.api

import com.ollama.mobile.data.model.LibraryModelsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OllamaLibraryApi {

    @GET("/api/search")
    suspend fun searchModels(@Query("q") query: String): Response<LibraryModelsResponse>
}