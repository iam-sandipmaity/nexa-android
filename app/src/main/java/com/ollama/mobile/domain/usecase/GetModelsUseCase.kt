package com.ollama.mobile.domain.usecase

import com.ollama.mobile.data.repository.OllamaRepository
import com.ollama.mobile.domain.model.OllamaModel

class GetModelsUseCase(private val repository: OllamaRepository = OllamaRepository()) {

    suspend operator fun invoke(): Result<List<OllamaModel>> {
        return repository.getModels()
    }

    fun updateBaseUrl(url: String) {
        repository.updateBaseUrl(url)
    }
}
