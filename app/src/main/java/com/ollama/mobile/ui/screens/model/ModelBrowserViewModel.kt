package com.ollama.mobile.ui.screens.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.repository.ModelRepository
import com.ollama.mobile.data.repository.OfflineModelRepository
import com.ollama.mobile.domain.model.DownloadedOfflineModel
import com.ollama.mobile.domain.model.OfflineModelInfo
import com.ollama.mobile.domain.model.OllamaModelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModelBrowserState(
    val availableModels: List<OllamaModelInfo> = emptyList(),
    val offlineCatalog: List<OfflineModelInfo> = emptyList(),
    val downloadedOfflineModels: List<DownloadedOfflineModel> = emptyList(),
    val downloadingOfflineModels: Map<String, Float> = emptyMap(),
    val offlineDownloadStatus: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFamily: String? = null,
    val isConnected: Boolean = false,
    val needsApiKey: Boolean = false
)

class ModelBrowserViewModel(
    private val repository: ModelRepository = ModelRepository(),
    private val offlineRepository: OfflineModelRepository = OfflineModelRepository()
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
                availableModels = repository.getFallbackModels(),
                offlineCatalog = offlineRepository.getCatalog(),
                downloadedOfflineModels = offlineRepository.getDownloadedModels()
            )

            repository.getAvailableModels().fold(
                onSuccess = { models ->
                    _uiState.value = _uiState.value.copy(
                        availableModels = models,
                        offlineCatalog = offlineRepository.getCatalog(),
                        downloadedOfflineModels = offlineRepository.getDownloadedModels(),
                        isConnected = true,
                        needsApiKey = false,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        availableModels = repository.getFallbackModels(),
                        offlineCatalog = offlineRepository.getCatalog(),
                        downloadedOfflineModels = offlineRepository.getDownloadedModels(),
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
                        offlineCatalog = offlineRepository.getCatalog(),
                        downloadedOfflineModels = offlineRepository.getDownloadedModels(),
                        isRefreshing = false,
                        error = null,
                        isConnected = true,
                        needsApiKey = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        availableModels = repository.getFallbackModels(),
                        offlineCatalog = offlineRepository.getCatalog(),
                        downloadedOfflineModels = offlineRepository.getDownloadedModels(),
                        isRefreshing = false,
                        error = error.message ?: "Couldn't refresh cloud models",
                        isConnected = false,
                        needsApiKey = !repository.hasApiKey()
                    )
                }
            )
        }
    }

    fun downloadOfflineModel(model: OfflineModelInfo) {
        viewModelScope.launch {
            try {
                offlineRepository.downloadModel(model).collect { progress ->
                    _uiState.value = _uiState.value.copy(
                        downloadingOfflineModels = _uiState.value.downloadingOfflineModels + (model.id to progress.progress),
                        offlineDownloadStatus = _uiState.value.offlineDownloadStatus + (model.id to progress.status)
                    )

                    if (progress.progress >= 1f) {
                        _uiState.value = _uiState.value.copy(
                            downloadedOfflineModels = offlineRepository.getDownloadedModels(),
                            downloadingOfflineModels = _uiState.value.downloadingOfflineModels - model.id
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    downloadingOfflineModels = _uiState.value.downloadingOfflineModels - model.id,
                    offlineDownloadStatus = _uiState.value.offlineDownloadStatus + (model.id to "Download failed"),
                    error = e.message ?: "Failed to download ${model.displayName}"
                )
            }
        }
    }

    fun deleteOfflineModel(modelId: String) {
        offlineRepository.deleteDownloadedModel(modelId)
        _uiState.value = _uiState.value.copy(
            downloadedOfflineModels = offlineRepository.getDownloadedModels(),
            downloadingOfflineModels = _uiState.value.downloadingOfflineModels - modelId,
            offlineDownloadStatus = _uiState.value.offlineDownloadStatus - modelId
        )
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

    fun getFilteredOfflineCatalog(): List<OfflineModelInfo> {
        val downloadedIds = _uiState.value.downloadedOfflineModels.map { it.id }.toSet()
        var models = _uiState.value.offlineCatalog.filterNot { it.id in downloadedIds }

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

    fun getFilteredDownloadedModels(): List<DownloadedOfflineModel> {
        var models = _uiState.value.downloadedOfflineModels

        if (_uiState.value.searchQuery.isNotBlank()) {
            val query = _uiState.value.searchQuery.lowercase()
            models = models.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.displayName.contains(query, ignoreCase = true)
            }
        }

        _uiState.value.selectedFamily?.let { family ->
            val matchingIds = _uiState.value.offlineCatalog.filter { it.family == family }.map { it.id }.toSet()
            models = models.filter { it.id in matchingIds }
        }

        return models
    }

    fun getModelFamilies(): List<String> {
        return (_uiState.value.availableModels.map { it.family } + _uiState.value.offlineCatalog.map { it.family })
            .distinct()
            .sorted()
    }
}
