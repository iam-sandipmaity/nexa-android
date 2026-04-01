package com.ollama.mobile.ui.screens.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.repository.ModelRepository
import com.ollama.mobile.domain.model.OllamaModelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModelBrowserState(
    val availableModels: List<OllamaModelInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFamily: String? = null,
    val isConnected: Boolean = false,
    val needsApiKey: Boolean = false
)

class ModelBrowserViewModel(
    private val repository: ModelRepository = ModelRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelBrowserState())
    val uiState: StateFlow<ModelBrowserState> = _uiState.asStateFlow()

    init {
        loadModels()
    }

    fun loadModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                availableModels = repository.getFallbackModels()
            )

            repository.getAvailableModels().fold(
                onSuccess = { models ->
                    _uiState.value = _uiState.value.copy(
                        availableModels = models,
                        isConnected = true,
                        needsApiKey = false,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        availableModels = repository.getFallbackModels(),
                        isConnected = false,
                        needsApiKey = !repository.hasApiKey(),
                        isLoading = false,
                        error = error.message ?: "Couldn't load cloud models"
                    )
                }
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            repository.getAvailableModels().fold(
                onSuccess = { models ->
                    _uiState.value = _uiState.value.copy(
                        availableModels = models,
                        isRefreshing = false,
                        error = null,
                        isConnected = true,
                        needsApiKey = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        availableModels = repository.getFallbackModels(),
                        isRefreshing = false,
                        error = error.message ?: "Couldn't refresh cloud models",
                        isConnected = false,
                        needsApiKey = !repository.hasApiKey()
                    )
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectFamily(family: String?) {
        _uiState.value = _uiState.value.copy(selectedFamily = family)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getFilteredModels(): List<OllamaModelInfo> {
        var models = _uiState.value.availableModels

        if (_uiState.value.searchQuery.isNotBlank()) {
            val query = _uiState.value.searchQuery.lowercase()
            models = models.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.displayName.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
            }
        }

        _uiState.value.selectedFamily?.let { family ->
            models = models.filter { it.family == family }
        }

        return models
    }

    fun getModelFamilies(): List<String> {
        return _uiState.value.availableModels.map { it.family }.distinct().sorted()
    }
}
