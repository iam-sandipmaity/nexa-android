package com.ollama.mobile.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.ui.components.ChatBubble
import com.ollama.mobile.ui.components.LoadingIndicator
import com.ollama.mobile.ui.components.MessageInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    selectedModel: String = "",
    existingChatId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isOfflineModel = remember(uiState.selectedModel) { uiState.selectedModel.startsWith("offline:") }
    val displayModelName = remember(uiState.selectedModel) {
        uiState.selectedModel.removePrefix("offline:")
    }

    LaunchedEffect(selectedModel, existingChatId) {
        if (selectedModel.isNotEmpty()) {
            viewModel.initializeWithChat(selectedModel, existingChatId)
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat")
                        if (uiState.selectedModel.isNotEmpty()) {
                            Text(
                                text = displayModelName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Filled.History, contentDescription = "Chat History")
                    }
                    if (uiState.messages.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearChat) {
                            Icon(Icons.Filled.Delete, contentDescription = "Clear chat")
                        }
                    }
                    if (uiState.isLoading && isOfflineModel) {
                        IconButton(onClick = viewModel::stopGeneration) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop generation")
                        }
                    } else {
                        IconButton(onClick = { viewModel.initializeWithChat(uiState.selectedModel, null) }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                MessageInput(
                    value = uiState.inputText,
                    onValueChange = viewModel::updateInputText,
                    onSend = viewModel::sendMessage,
                    enabled = !uiState.isLoading && uiState.selectedModel.isNotEmpty(),
                    placeholder = when {
                        uiState.selectedModel.isEmpty() -> "Select a model first"
                        uiState.selectedModel.startsWith("offline:") -> {
                            if (uiState.isModelLoaded) "Ask offline model..." 
                            else "Loading model..."
                        }
                        else -> "Ask $displayModelName..."
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                EmptyChatState(
                    modelName = displayModelName,
                    isConnected = uiState.isConnected,
                    isOfflineModel = isOfflineModel,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.messages) { message ->
                        ChatBubble(message = message)
                    }
                    
                    // Show streaming response for offline models
                    if (uiState.streamingResponse.isNotEmpty()) {
                        item {
                            ChatBubble(message = ChatMessage(role = "assistant", content = uiState.streamingResponse))
                        }
                    }
                    
                    if (uiState.isLoading) {
                        item {
                            LoadingIndicator()
                        }
                    }
                }
            }

            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(
    modelName: String,
    isConnected: Boolean,
    isOfflineModel: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when {
                        isOfflineModel -> "Offline Model Ready"
                        isConnected -> "Ready to Chat"
                        else -> "Connection Issue"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = when {
                isOfflineModel -> "Model ready on device.\nLocal inference coming soon!"
                !isConnected -> "Add your API key in Settings or check your Ollama Cloud connection"
                modelName.isEmpty() -> "Select a model to start"
                else -> "Send a message to begin with\n$modelName"
            },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
