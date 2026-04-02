@file:OptIn(ExperimentalMaterial3Api::class)

package com.ollama.mobile.ui.screens.model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ollama.mobile.domain.model.DownloadedOfflineModel
import com.ollama.mobile.domain.model.LibraryModelInfo
import com.ollama.mobile.domain.model.OfflineModelInfo
import com.ollama.mobile.domain.model.familyLogos

@Composable
fun ModelBrowserScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: ModelBrowserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }
    var showImportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showImportDialog) {
        ImportModelDialog(
            importState = uiState.importState,
            onDismiss = {
                showImportDialog = false
                viewModel.clearImportState()
            },
            onImport = { url ->
                viewModel.addCustomModelFromUrl(url)
            }
        )
        LaunchedEffect(uiState.importState) {
            if (uiState.importState is ImportState.Success) {
                showImportDialog = false
                viewModel.clearImportState()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Models", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Downloaded (${uiState.downloadedOfflineModels.size})") },
                    icon = { Icon(Icons.Default.Storage, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        if (uiState.downloadModels.isEmpty()) {
                            viewModel.loadOfflineCatalog()
                        }
                    },
                    text = { Text("Available") },
                    icon = { Icon(Icons.Default.CloudDownload, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        if (uiState.libraryModels.isEmpty()) {
                            viewModel.loadLibraryModels()
                        }
                    },
                    text = { Text("Library") },
                    icon = { Icon(Icons.Default.Cloud, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> DownloadedModelsTab(
                    downloadedModels = uiState.downloadedOfflineModels,
                    onDelete = viewModel::deleteOfflineModel,
                    onChat = { model -> onNavigateToChat("offline:${model.id}") }
                )
                1 -> AvailableModelsTab(
                    availableModels = uiState.downloadModels,
                    downloadedIds = uiState.downloadedOfflineModels.map { it.id }.toSet(),
                    downloadingModels = uiState.downloadingOfflineModels,
                    downloadStatus = uiState.offlineDownloadStatus,
                    onDownload = viewModel::downloadOfflineModel,
                    onImportClick = { showImportDialog = true }
                )
                2 -> LibraryModelsTab(
                    libraryModels = viewModel.getFilteredLibraryModels(),
                    isLoading = uiState.isLoadingLibrary,
                    error = uiState.libraryError,
                    searchQuery = uiState.librarySearchQuery,
                    onSearchQueryChange = viewModel::searchLibraryModels,
                    onRefresh = { viewModel.loadLibraryModels() },
                    onChat = { model -> onNavigateToChat("library:${model.name}") }
                )
            }
        }
    }
}

sealed interface ImportState {
    data object Idle : ImportState
    data object Probing : ImportState
    data class Error(val message: String) : ImportState
    data class Success(val message: String) : ImportState
}

@Composable
private fun ImportModelDialog(
    importState: ImportState,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }

    val urlError: String? = remember(urlInput) {
        val u = urlInput.trim()
        when {
            u.isBlank() -> null
            !u.startsWith("http://") && !u.startsWith("https://") ->
                "URL must start with http:// or https://"
            !u.contains(".gguf", ignoreCase = true) ->
                "URL must point to a .gguf file"
            else -> null
        }
    }

    val canImport = urlInput.isNotBlank() &&
        urlError == null &&
        importState !is ImportState.Probing

    AlertDialog(
        onDismissRequest = { if (importState !is ImportState.Probing) onDismiss() },
        title = { Text("Import Custom Model", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Paste the direct download URL of any GGUF model file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Model URL") },
                    placeholder = { Text("https://.../model.gguf") },
                    singleLine = false,
                    maxLines = 4,
                    isError = urlError != null,
                    supportingText = {
                        urlError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    enabled = importState !is ImportState.Probing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                when (importState) {
                    is ImportState.Probing -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking URL...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is ImportState.Error -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(importState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    is ImportState.Success -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(importState.message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                    else -> Unit
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onImport(urlInput.trim()) }, enabled = canImport) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = importState !is ImportState.Probing) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AvailableModelsTab(
    availableModels: List<OfflineModelInfo>,
    downloadedIds: Set<String>,
    downloadingModels: Map<String, Float>,
    downloadStatus: Map<String, String>,
    onDownload: (OfflineModelInfo) -> Unit,
    onImportClick: () -> Unit
) {
    val availableToDownload = availableModels.filterNot { it.id in downloadedIds }
    
    if (availableToDownload.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No models available to download",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap the button below to import a custom model",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onImportClick) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Custom Model")
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = onImportClick,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Import Custom Model",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Add any GGUF model via direct URL",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableToDownload, key = { it.id }) { model ->
                val isDownloading = downloadingModels.containsKey(model.id)
                val progress = downloadingModels[model.id]
                val status = downloadStatus[model.id]
                
                AvailableModelItem(
                    model = model,
                    isDownloading = isDownloading,
                    progress = progress,
                    status = status,
                    onDownload = { onDownload(model) }
                )
            }
        }
    }
}

@Composable
private fun AvailableModelItem(
    model: OfflineModelInfo,
    isDownloading: Boolean,
    progress: Float?,
    status: String?,
    onDownload: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FamilyIcon(family = model.family, logo = model.logo)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    model.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${model.size} - ${model.family}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (isDownloading && status != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (progress != null && progress < 1f) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
            } else {
                IconButton(onClick = onDownload) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadedModelsTab(
    downloadedModels: List<DownloadedOfflineModel>,
    onDelete: (String) -> Unit,
    onChat: (DownloadedOfflineModel) -> Unit
) {
    if (downloadedModels.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No downloaded models",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Switch to Library tab to browse models",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        return
    }

    val totalSize = downloadedModels.sumOf { it.sizeBytes }
    val formattedTotal = when {
        totalSize >= 1_000_000_000L -> String.format("%.1f GB", totalSize / 1_000_000_000.0)
        totalSize >= 1_000_000L     -> String.format("%.0f MB", totalSize / 1_000_000.0)
        else                        -> String.format("%d KB", totalSize / 1000)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total Storage Used",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "${downloadedModels.size} models - $formattedTotal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(downloadedModels, key = { it.id }) { model ->
                DownloadedModelItem(
                    model = model,
                    onDelete = { onDelete(model.id) },
                    onChat = { onChat(model) }
                )
            }
        }
    }
}

@Composable
private fun DownloadedModelItem(
    model: DownloadedOfflineModel,
    onDelete: () -> Unit,
    onChat: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Model") },
            text = {
                Text(
                    "Are you sure you want to delete ${model.displayName}? " +
                        "This will free up ${model.formattedSize} of storage."
                )
            },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FamilyIcon(family = model.family, logo = model.logo)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    model.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    model.formattedSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onChat) {
                Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun LibraryModelsTab(
    libraryModels: List<LibraryModelInfo>,
    isLoading: Boolean,
    error: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onChat: (LibraryModelInfo) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search Ollama Library...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.SearchOff, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Loading Ollama Library...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text("Retry")
                    }
                }
            }
        } else if (libraryModels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (searchQuery.isNotEmpty()) "No models found for \"$searchQuery\""
                        else "Start typing to search Ollama Library",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Browse thousands of models from Ollama.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(libraryModels, key = { it.id }) { model ->
                    LibraryModelItem(
                        model = model,
                        onChat = { onChat(model) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryModelItem(
    model: LibraryModelInfo,
    onChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onChat
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(familyColor(model.family)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = model.logo,
                    contentDescription = model.family,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        model.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (model.verified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Verified",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
                Text(
                    model.description.take(80) + if (model.description.length > 80) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        model.size,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "${formatPullCount(model.pullCount)} pulls",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        model.family,
                        style = MaterialTheme.typography.labelSmall,
                        color = familyColor(model.family)
                    )
                }
            }
            
            Icon(
                Icons.Default.Chat,
                contentDescription = "Chat",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatPullCount(count: Int): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

@Composable
private fun FamilyIcon(
    family: String,
    logo: androidx.compose.ui.graphics.vector.ImageVector =
        familyLogos[family.lowercase()] ?: Icons.Default.Psychology
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(familyColor(family)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = logo,
            contentDescription = family,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun familyColor(family: String): Color = when (family.lowercase()) {
    "llama"     -> Color(0xFF6750A4)
    "gemma"     -> Color(0xFF4285F4)
    "mistral"   -> Color(0xFFE94235)
    "qwen"      -> Color(0xFFFF9800)
    "phi"       -> Color(0xFF0078D4)
    "deepseek"  -> Color(0xFF00BCD4)
    "codellama" -> Color(0xFF4CAF50)
    "nemotron"  -> Color(0xFF8B5CF6)
    "aya"       -> Color(0xFF10B981)
    "command"   -> Color(0xFF6366F1)
    "custom"    -> Color(0xFF9C27B0)
    else        -> Color(0xFF607D8B)
}