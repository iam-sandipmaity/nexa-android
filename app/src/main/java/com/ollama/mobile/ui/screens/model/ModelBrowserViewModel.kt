package com.ollama.mobile.ui.screens.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.repository.DownloadProgress
import com.ollama.mobile.data.repository.ModelRepository
import com.ollama.mobile.domain.model.LocalModel
import com.ollama.mobile.domain.model.OllamaModelInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModelBrowserState(
    val availableModels: List<OllamaModelInfo> = emptyList(),
    val localModels: List<LocalModel> = emptyList(),
    val downloadingModels: Map<String, Float> = emptyMap(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFamily: String? = null,
    val isConnected: Boolean = true
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load available models (always available)
            _uiState.value = _uiState.value.copy(
                availableModels = repository.availableModels
            )
            
            // Load local models
            repository.getLocalModels().fold(
                onSuccess = { models ->
                    _uiState.value = _uiState.value.copy(
                        localModels = models,
                        isConnected = true,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isConnected = false,
                        isLoading = false,
                        error = "Cannot connect to Ollama"
                    )
                }
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            repository.getLocalModels().fold(
                onSuccess = { models ->
                    _uiState.value = _uiState.value.copy(
                        localModels = models,
                        isRefreshing = false,
                        error = null,
                        isConnected = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = error.message,
                        isConnected = false
                    )
                }
            )
        }
    }

    fun downloadModel(modelInfo: OllamaModelInfo) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    downloadingModels = _uiState.value.downloadingModels + (modelInfo.name to 0f)
                )
                
                repository.pullModel(modelInfo.name).collect { progress ->
                    _uiState.value = _uiState.value.copy(
                        downloadingModels = _uiState.value.downloadingModels + (modelInfo.name to progress.progress)
                    )
                    
                    if (progress.progress >= 1f) {
                        delay(500)
                        repository.getLocalModels().onSuccess { models ->
                            _uiState.value = _uiState.value.copy(
                                localModels = models,
                                downloadingModels = _uiState.value.downloadingModels - modelInfo.name
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    downloadingModels = _uiState.value.downloadingModels - modelInfo.name,
                    error = "Download failed: ${e.message}"
                )
            }
        }
    }

    fun deleteModel(modelName: String) {
        viewModelScope.launch {
            repository.deleteModel(modelName).onSuccess {
                repository.getLocalModels().onSuccess { models ->
                    _uiState.value = _uiState.value.copy(localModels = models)
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
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
        
        // Filter by search query
        if (_uiState.value.searchQuery.isNotBlank()) {
            val query = _uiState.value.searchQuery.lowercase()
            models = models.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.displayName.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
        
        // Filter by family
        _uiState.value.selectedFamily?.let { family ->
            models = models.filter { it.family == family }
        }
        
        return models
    }

    fun isModelDownloaded(modelName: String): Boolean {
        return _uiState.value.localModels.any { it.name == modelName }
    }

    fun isModelDownloading(modelName: String): Boolean {
        return _uiState.value.downloadingModels.containsKey(modelName)
    }

    fun getModelDownloadProgress(modelName: String): Float {
        return _uiState.value.downloadingModels[modelName] ?: 0f
    }

    fun getModelFamilies(): List<String> {
        return _uiState.value.availableModels.map { it.family }.distinct().sorted()
    }
}
