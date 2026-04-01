package com.ollama.mobile.domain.model

data class AppSettings(
    val baseUrl: String = "http://localhost:11434/",
    val selectedModel: String = "",
    val systemPrompt: String = "You are a helpful AI assistant.",
    val temperature: Float = 0.7f
)
