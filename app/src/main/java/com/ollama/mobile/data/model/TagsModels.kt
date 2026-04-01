package com.ollama.mobile.data.model

data class TagsResponse(
    val models: List<ModelInfo>
)

data class ModelInfo(
    val name: String,
    @com.google.gson.annotations.SerializedName("modified_at")
    val modifiedAt: String?,
    val size: Long?,
    val digest: String?
)
