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
    val totalDuration: Long?,
    @SerializedName("load_duration")
    val loadDuration: Long?,
    @SerializedName("prompt_eval_count")
    val promptEvalCount: Int?,
    @SerializedName("prompt_eval_duration")
    val promptEvalDuration: Long?,
    @SerializedName("eval_count")
    val evalCount: Int?,
    @SerializedName("eval_duration")
    val evalDuration: Long?
)

data class ResponseMessage(
    val role: String,
    val content: String
)
