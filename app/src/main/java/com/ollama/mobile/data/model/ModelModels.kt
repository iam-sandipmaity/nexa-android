package com.ollama.mobile.data.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
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

data class ModelDetails(
    @SerializedName("parent_model")
    val parentModel: String?,
    val format: String?,
    val family: String?,
    val families: List<String>?,
    @SerializedName("parameter_size")
    val parameterSize: String?,
    @SerializedName("quantization_level")
    val quantizationLevel: String?
)

data class TagsResponse(
    val models: List<ModelTag>
)

data class ModelTag(
    val name: String,
    val model: String? = null,
    @SerializedName("modified_at")
    val modifiedAt: String?,
    val size: Long,
    val digest: String?,
    val details: ModelDetails? = null
)

data class VersionResponse(
    val version: String
)

data class LibraryModelsResponse(
    val models: List<LibraryModel>
)

data class LibraryModel(
    val name: String,
    val description: String?,
    val model: String?,
    val size: Long?,
    val digest: String?,
    val lastModified: String?,
    val tags: List<String>?,
    val pullCount: Int?,
    val verified: Boolean?,
    val featured: Boolean?
)
