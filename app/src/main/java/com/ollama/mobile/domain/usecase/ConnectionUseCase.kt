package com.ollama.mobile.domain.usecase

import com.ollama.mobile.data.repository.OllamaRepository

class ConnectionUseCase(private val repository: OllamaRepository = OllamaRepository()) {

    suspend fun checkConnection(): Result<Boolean> {
        return repository.checkConnection()
    }

    fun updateBaseUrl(url: String) {
        repository.updateBaseUrl(url)
    }

    fun getCurrentBaseUrl(): String = repository.getBaseUrl()
}
