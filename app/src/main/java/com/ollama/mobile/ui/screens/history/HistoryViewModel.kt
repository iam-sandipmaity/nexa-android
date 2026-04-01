package com.ollama.mobile.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollama.mobile.data.config.AppConfig
import com.ollama.mobile.data.repository.ChatHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val history: List<ChatHistoryRepository.ChatHistoryEntry> = emptyList(),
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val repository: ChatHistoryRepository = AppConfig.getChatHistoryRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            repository.getAllHistory().collect { list ->
                _uiState.value = HistoryUiState(
                    history = list,
                    isLoading = false
                )
            }
        }
    }
    
    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}