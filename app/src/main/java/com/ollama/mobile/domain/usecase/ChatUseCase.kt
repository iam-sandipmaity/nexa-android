package com.ollama.mobile.domain.usecase

import com.ollama.mobile.data.repository.OllamaRepository
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.domain.model.ChatResult

class ChatUseCase(private val repository: OllamaRepository = OllamaRepository()) {

    suspend operator fun invoke(
        model: String,
        messages: List<ChatMessage>
    ): Result<ChatResult> {
        if (model.isBlank()) {
            return Result.failure(Exception("No model selected"))
        }
        if (messages.isEmpty()) {
            return Result.failure(Exception("No messages provided"))
        }
        return repository.chat(model, messages)
    }

    fun updateBaseUrl(url: String) {
        repository.updateBaseUrl(url)
    }
}
