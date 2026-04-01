package com.ollama.mobile.data.model

data class GenerateRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
    val system: String? = null,
    val context: IntArray? = null,
    val options: ModelOptions? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenerateRequest

        if (model != other.model) return false
        if (prompt != other.prompt) return false
        if (stream != other.stream) return false
        if (system != other.system) return false
        if (context != null) {
            if (other.context == null) return false
            if (!context.contentEquals(other.context)) return false
        } else if (other.context != null) return false
        if (options != other.options) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.hashCode()
        result = 31 * result + prompt.hashCode()
        result = 31 * result + stream.hashCode()
        result = 31 * result + (system?.hashCode() ?: 0)
        result = 31 * result + (context?.contentHashCode() ?: 0)
        result = 31 * result + (options?.hashCode() ?: 0)
        return result
    }
}

data class GenerateResponse(
    val model: String,
    val response: String,
    val done: Boolean,
    @com.google.gson.annotations.SerializedName("done_reason")
    val doneReason: String?,
    @com.google.gson.annotations.SerializedName("total_duration")
    val totalDuration: Long?,
    @com.google.gson.annotations.SerializedName("load_duration")
    val loadDuration: Long?,
    @com.google.gson.annotations.SerializedName("prompt_eval_count")
    val promptEvalCount: Int?,
    @com.google.gson.annotations.SerializedName("eval_count")
    val evalCount: Int?,
    val context: IntArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenerateResponse

        if (model != other.model) return false
        if (response != other.response) return false
        if (done != other.done) return false
        if (context != null) {
            if (other.context == null) return false
            if (!context.contentEquals(other.context)) return false
        } else if (other.context != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.hashCode()
        result = 31 * result + response.hashCode()
        result = 31 * result + done.hashCode()
        result = 31 * result + (context?.contentHashCode() ?: 0)
        return result
    }
}

data class ModelOptions(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val numPredict: Int? = null,
    val repeatPenalty: Float? = null
)
