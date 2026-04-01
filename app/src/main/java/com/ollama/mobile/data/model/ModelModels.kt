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

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
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

data class ChatResponse(
    val model: String,
    val message: ResponseMessage,
    @SerializedName("done_reason")
    val doneReason: String?,
    val done: Boolean,
    @SerializedName("total_duration")
    val totalDuration: Long?
)

data class ResponseMessage(
    val role: String,
    val content: String
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
