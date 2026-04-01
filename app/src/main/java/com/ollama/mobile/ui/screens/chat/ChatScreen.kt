package com.ollama.mobile.ui.screens.chat

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ollama.mobile.data.config.AppConfig
import com.ollama.mobile.data.repository.ChatHistoryRepository
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.domain.model.DownloadedOfflineModel
import com.ollama.mobile.domain.model.OllamaModelInfo
import com.ollama.mobile.ui.components.ChatBubble
import com.ollama.mobile.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    selectedModel: String = "",
    existingChatId: String? = null,
    onNavigateToSettings: () -> Unit,
    onNavigateToModels: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val drawerState = remember { mutableStateOf(false) }
    val drawerOffset by animateFloatAsState(
        targetValue = if (drawerState.value) 1f else 0f,
        label = "drawer"
    )
    
    var showModelSelector by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var modelSearchQuery by remember { mutableStateOf("") }
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val hasApiKey = AppConfig.hasApiKey()
        if (!hasApiKey) {
            showApiKeyDialog = true
        } else {
            // Auto-select last used model from history
            val history = viewModel.getChatHistory()
            if (history.isNotEmpty()) {
                val lastChat = history.first()
                viewModel.initializeWithChat(lastChat.modelName, lastChat.id)
            }
        }
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

    if (drawerState.value) {
        BackHandler {
            drawerState.value = false
        }
    }

    if (showApiKeyDialog) {
        ApiKeySetupDialog(
            apiKey = apiKeyInput,
            onApiKeyChange = { apiKeyInput = it },
            onSave = {
                if (apiKeyInput.isNotBlank()) {
                    AppConfig.updateApiKey(apiKeyInput.trim())
                    AppConfig.updateBaseUrl("https://ollama.com/")
                }
                showApiKeyDialog = false
                viewModel.refreshModels()
            },
            onSkip = {
                showApiKeyDialog = false
            }
        )
        return
    }

    if (showModelSelector) {
        viewModel.refreshModels()
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = (300f * drawerOffset).dp.toPx()
                },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.clickable { showModelSelector = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Chat", fontWeight = FontWeight.Bold)
                            if (uiState.selectedModel.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { drawerState.value = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "History")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToModels) {
                            Icon(Icons.Default.Storage, contentDescription = "Models")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
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
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = { },
                            onHorizontalDrag = { change, dragAmount ->
                                if (dragAmount > 30 && !drawerState.value) {
                                    drawerState.value = true
                                }
                            }
                        )
                    }
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
                            onDismiss = viewModel::clearError
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

                    MessageInputBox(
                        value = inputText,
                        onValueChange = { inputText = it },
                        onSend = {
                            if (inputText.isNotBlank()) {
                                viewModel.updateInputText(inputText)
                                viewModel.sendMessage()
                                inputText = ""
                            }
                        },
                        enabled = !uiState.isLoading
                    )
                }
            }
        }

        // History Drawer overlay
        if (drawerState.value) {
            // Dim background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { drawerState.value = false }
                    .background(Color.Black.copy(alpha = 0.5f * drawerOffset))
            )
            
            // Drawer content
            HistoryDrawer(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        translationX = (-300f + (300f * drawerOffset)).dp.toPx()
                    },
                history = uiState.chatHistory,
                currentChatId = uiState.chatId,
                onChatSelected = { chatId, modelName ->
                    viewModel.initializeWithChat(modelName, chatId)
                    drawerState.value = false
                },
                onNewChat = {
                    viewModel.initializeWithModel(uiState.selectedModel)
                    drawerState.value = false
                },
                onSaveChat = {
                    viewModel.saveChatToHistory()
                },
                onDeleteChat = { chatId ->
                    viewModel.deleteChat(chatId)
                },
                onNavigateToSettings = {
                    drawerState.value = false
                    onNavigateToSettings()
                },
                onClose = { drawerState.value = false }
            )
        }
    }
}

@Composable
private fun ApiKeySetupDialog(
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onSkip) {
                        Icon(Icons.Default.Close, contentDescription = "Skip")
                    }
                }
                
                Icon(
                    Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Connect to Ollama Cloud",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Enter your API key to start chatting with cloud models.\nYou can also use offline models without an API key.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = onApiKeyChange,
                    label = { Text("API Key") },
                    placeholder = { Text("Paste your Ollama API key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Skip")
                    }
                    
                    Button(
                        onClick = onSave,
                        enabled = apiKey.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Connect")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ollama.com/"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                ) {
                    Text("Get API Key from Ollama")
                }
            }
        }
    }
}

@Composable
private fun MessageInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    val focusManager = LocalFocusManager.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 120.dp),
                placeholder = { Text("Message...") },
                enabled = enabled,
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        focusManager.clearFocus()
                        onSend()
                    }
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (enabled && value.isNotBlank()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    .clickable(
                        enabled = enabled && value.isNotBlank(),
                        onClick = {
                            focusManager.clearFocus()
                            onSend()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (enabled && value.isNotBlank()) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryDrawer(
    modifier: Modifier = Modifier,
    history: List<ChatHistoryRepository.ChatHistoryEntry>,
    currentChatId: String?,
    onChatSelected: (String, String) -> Unit,
    onNewChat: () -> Unit,
    onSaveChat: () -> Unit,
    onDeleteChat: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Divider()
            
            // New Chat Button
            Button(
                onClick = {
                    onSaveChat()
                    onNewChat()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Chat")
            }
            
            Divider()
            
            // History List
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(history) { chat ->
                    HistoryDrawerItem(
                        chat = chat,
                        isSelected = chat.id == currentChatId,
                        onClick = { onChatSelected(chat.id, chat.modelName) },
                        onDelete = { onDeleteChat(chat.id) }
                    )
                }
            }
            
            Divider()
            
            // Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToSettings)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Settings")
            }
        }
    }
}

@Composable
private fun HistoryDrawerItem(
    chat: ChatHistoryRepository.ChatHistoryEntry,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Chat") },
            text = { Text("Are you sure you want to delete this chat?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.messages.firstOrNull()?.content?.take(30) ?: "Empty chat",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                text = chat.modelName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        IconButton(onClick = { showDeleteDialog = true }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
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
            modifier = Modifier.fillMaxWidth(0.7f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.ModelTraining, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Model")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onNavigateToModels,
            modifier = Modifier.fillMaxWidth(0.7f),
            shape = RoundedCornerShape(24.dp)
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
    onDismiss: () -> Unit
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
            TextButton(onClick = onDismiss) {
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
        title = { Text("Select Model", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search models...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    if (filteredCloudModels.isNotEmpty()) {
                        item {
                            Text(
                                text = "Cloud Models",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(filteredCloudModels) { model ->
                            ModelListItem(
                                name = model.displayName,
                                subtitle = "${model.size} • ${model.family}",
                                isSelected = currentModel == model.name,
                                icon = model.logo,
                                onClick = { onModelSelected(model.name) }
                            )
                        }
                    }
                    
                    if (filteredDownloadedModels.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Downloaded Models",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
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
            .clip(RoundedCornerShape(12.dp))
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
                    .clip(RoundedCornerShape(10.dp))
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
