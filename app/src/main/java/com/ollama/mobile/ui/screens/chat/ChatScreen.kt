package com.ollama.mobile.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.domain.model.DownloadedOfflineModel
import com.ollama.mobile.domain.model.OllamaModelInfo
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
    onNavigateToModels: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isOfflineModel = remember(uiState.selectedModel) { uiState.selectedModel.startsWith("offline:") }
    val displayModelName = remember(uiState.selectedModel) {
        uiState.selectedModel.removePrefix("offline:")
    }
    
    var showModelSelector by remember { mutableStateOf(false) }
    var modelSearchQuery by remember { mutableStateOf("") }

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

    if (showModelSelector) {
        ModelSelectorDialog(
            availableModels = uiState.availableModels,
            downloadedModels = uiState.downloadedModels,
            searchQuery = modelSearchQuery,
            onSearchQueryChange = { modelSearchQuery = it },
            currentModel = uiState.selectedModel,
            onModelSelected = { modelName ->
                viewModel.initializeWithModel(modelName)
                showModelSelector = false
                modelSearchQuery = ""
            },
            onDismiss = { showModelSelector = false },
            onNavigateToModels = {
                showModelSelector = false
                onNavigateToModels()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable { showModelSelector = true },
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Chat", fontWeight = FontWeight.Bold)
                            if (uiState.selectedModel.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Model",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (uiState.selectedModel.isNotEmpty()) {
                            Text(
                                text = displayModelName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        } else {
                            Text(
                                text = "Select a model",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
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
                    IconButton(onClick = { showModelSelector = true }) {
                        Icon(Icons.Filled.ModelTraining, contentDescription = "Select Model")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Filled.History, contentDescription = "Chat History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.selectedModel.isEmpty()) {
                EmptyStateSection(
                    onSelectModel = { showModelSelector = true },
                    onNavigateToModels = onNavigateToModels
                )
            } else {
                if (uiState.error != null && !uiState.isLoading) {
                    ErrorBanner(
                        error = uiState.error!!,
                        onRetry = viewModel::clearError
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.messages) { message ->
                        ChatBubble(message = message)
                    }

                    if (uiState.isLoading && uiState.streamingResponse.isNotEmpty()) {
                        item {
                            ChatBubble(
                                message = ChatMessage(
                                    role = "assistant",
                                    content = uiState.streamingResponse
                                )
                            )
                        }
                    }

                    if (uiState.isLoading && uiState.streamingResponse.isEmpty()) {
                        item {
                            LoadingIndicator()
                        }
                    }
                }

                MessageInput(
                    value = uiState.inputText,
                    onValueChange = viewModel::updateInputText,
                    onSend = viewModel::sendMessage,
                    enabled = !uiState.isLoading && uiState.selectedModel.isNotEmpty(),
                    isOfflineModel = isOfflineModel
                )
            }
        }
    }
}

@Composable
private fun EmptyStateSection(
    onSelectModel: () -> Unit,
    onNavigateToModels: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Chat,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Welcome to Ollama Mobile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Select a model to start chatting",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onSelectModel,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.ModelTraining, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Model")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onNavigateToModels,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse & Download Models")
        }
    }
}

@Composable
private fun ErrorBanner(
    error: String,
    onRetry: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text("Dismiss")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelectorDialog(
    availableModels: List<OllamaModelInfo>,
    downloadedModels: List<DownloadedOfflineModel>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentModel: String,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToModels: () -> Unit
) {
    val filteredCloudModels = availableModels.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.displayName.contains(searchQuery, ignoreCase = true)
    }
    
    val filteredDownloadedModels = downloadedModels.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.displayName.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Select Model", fontWeight = FontWeight.Bold) 
        },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search models...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (searchQuery.isEmpty()) {
                    if (downloadedModels.isNotEmpty()) {
                        Text(
                            text = "Downloaded Models",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    if (filteredDownloadedModels.isNotEmpty()) {
                        items(filteredDownloadedModels) { model ->
                            ModelListItem(
                                name = model.displayName,
                                subtitle = "Offline • ${model.formattedSize}",
                                isSelected = currentModel == "offline:${model.id}",
                                icon = model.logo,
                                onClick = { onModelSelected("offline:${model.id}") }
                            )
                        }
                    }
                    
                    if (filteredCloudModels.isNotEmpty()) {
                        if (filteredDownloadedModels.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Cloud Models",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        items(filteredCloudModels.take(10)) { model ->
                            ModelListItem(
                                name = model.displayName,
                                subtitle = "${model.size} • ${model.family}",
                                isSelected = currentModel == model.name,
                                icon = model.logo,
                                onClick = { onModelSelected(model.name) }
                            )
                        }
                    }
                    
                    if (filteredDownloadedModels.isEmpty() && filteredCloudModels.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No models found")
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onNavigateToModels) {
                                    Text("Download Models")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onNavigateToModels) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Manage Models")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ModelListItem(
    name: String,
    subtitle: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}