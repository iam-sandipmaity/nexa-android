package com.ollama.mobile.data.model

data class ModelsResponse(
    val models: List<Model>
)

data class Model(
    val name: String,
    val model: String,
    val modifiedAt: String?,
    val size: Long?,
    val digest: String?
)
