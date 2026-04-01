package com.ollama.mobile.data.model

import com.google.gson.annotations.SerializedName

// Request models
data class PullRequest(
    val name: String,
    val stream: Boolean = false
)

data class DeleteModelRequest(
    val name: String
)

// Response models
data class PullResponse(
    val status: String?,
    val digest: String?,
    val message: String?,
    val total: Long?,
    val completed: Long?
)

data class DeleteResponse(
    val status: String?
)

// Model info from API
data class ModelDetail(
    val name: String,
    val modifiedAt: String?,
    val size: Long,
    val digest: String?,
    val details: ModelDetails?
)

data class ModelDetails(
    val parentModel: String?,
    val format: String?,
    val family: String?,
    val families: List<String>?,
    val parameterSize: String?,
    val quantizationLevel: String?
)

// Tag list response
data class TagsResponse(
    val models: List<ModelTag>
)

data class ModelTag(
    val name: String,
    @SerializedName("modified_at")
    val modifiedAt: String?,
    val size: Long,
    val digest: String?
)
