@file:OptIn(ExperimentalMaterial3Api::class)

package com.ollama.mobile.ui.screens.model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
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
import com.ollama.mobile.domain.model.OfflineModelInfo
import com.ollama.mobile.domain.model.OllamaModelInfo
import com.ollama.mobile.domain.model.familyLogos

// ─── Screen ──────────────────────────────────────────────────────────────────

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

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
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
                    onClick = { selectedTab = 1 },
                    text = { Text("Browse") },
                    icon = { Icon(Icons.Default.Download, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> DownloadedModelsTab(
                    downloadedModels = uiState.downloadedOfflineModels,
                    onDelete = viewModel::deleteOfflineModel,
                    onChat = { model -> onNavigateToChat("offline:${model.id}") }
                )
                1 -> BrowseModelsTab(
                    offlineModels = uiState.offlineCatalog,
                    downloadedModels = uiState.downloadedOfflineModels,
                    cloudModels = uiState.availableModels,
                    downloadingModels = uiState.downloadingOfflineModels,
                    importState = uiState.importState,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onDownload = viewModel::downloadOfflineModel,
                    onImportUrl = viewModel::addCustomModelFromUrl,
                    onDismissImportState = viewModel::clearImportState,
                    onChat = onNavigateToChat
                )
            }
        }
    }
}

// ─── Downloaded tab ───────────────────────────────────────────────────────────

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
                    "Go to Browse tab to download models",
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
                        "${downloadedModels.size} models • $formattedTotal",
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

// ─── Browse tab ───────────────────────────────────────────────────────────────

enum class ModelType { OFFLINE, CLOUD }

@Composable
private fun BrowseModelsTab(
    offlineModels: List<OfflineModelInfo>,
    downloadedModels: List<DownloadedOfflineModel>,
    cloudModels: List<OllamaModelInfo>,
    downloadingModels: Map<String, Float>,
    importState: ImportState,
    onSearchQueryChange: (String) -> Unit,
    onDownload: (OfflineModelInfo) -> Unit,
    onImportUrl: (String) -> Unit,
    onDismissImportState: () -> Unit,
    onChat: (String) -> Unit
) {
    val downloadedIds = downloadedModels.map { it.id }.toSet()
    val availableToDownload = offlineModels.filterNot { it.id in downloadedIds }

    var selectedModelType by remember { mutableStateOf(ModelType.OFFLINE) }
    var searchQuery by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    // Auto-close dialog when import finishes successfully
    LaunchedEffect(importState) {
        if (importState is ImportState.Success) showImportDialog = false
    }

    val filteredOffline = availableToDownload.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.family.contains(searchQuery, ignoreCase = true)
    }
    val filteredCloud = cloudModels.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.displayName.contains(searchQuery, ignoreCase = true)
    }
    val filteredModels: List<Any> = when (selectedModelType) {
        ModelType.OFFLINE -> filteredOffline
        ModelType.CLOUD   -> filteredCloud
    }

    // ── Import dialog ─────────────────────────────────────────────────────
    if (showImportDialog) {
        ImportModelDialog(
            importState = importState,
            onDismiss = {
                showImportDialog = false
                onDismissImportState()
            },
            onImport = onImportUrl
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; onSearchQueryChange(it) },
            placeholder = { Text("Search models…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Offline / Cloud toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedModelType == ModelType.OFFLINE,
                onClick = { selectedModelType = ModelType.OFFLINE },
                label = { Text("Offline") },
                leadingIcon = {
                    if (selectedModelType == ModelType.OFFLINE)
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedModelType == ModelType.CLOUD,
                onClick = { selectedModelType = ModelType.CLOUD },
                label = { Text("Cloud") },
                leadingIcon = {
                    if (selectedModelType == ModelType.CLOUD)
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Import card — shown only on Offline tab
        if (selectedModelType == ModelType.OFFLINE) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showImportDialog = true; onDismissImportState() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AddCircle,
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
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Model list
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredModels, key = { m ->
                when (m) {
                    is OfflineModelInfo -> m.id
                    is OllamaModelInfo  -> m.id
                    else -> m.hashCode()
                }
            }) { model ->
                when {
                    selectedModelType == ModelType.OFFLINE && model is OfflineModelInfo ->
                        BrowseModelItem(
                            model = model,
                            isDownloading = downloadingModels.containsKey(model.id),
                            downloadProgress = downloadingModels[model.id],
                            onDownload = { onDownload(model) },
                            onChat = { onChat(model.name) }
                        )
                    selectedModelType == ModelType.CLOUD && model is OllamaModelInfo ->
                        CloudModelItem(
                            model = model,
                            onChat = { onChat(model.name) }
                        )
                }
            }

            if (filteredModels.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No models found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ─── Import dialog ────────────────────────────────────────────────────────────

/**
 * A sealed class that the ViewModel exposes so the UI can render the exact
 * state of an in-progress or completed custom-model import.
 */
sealed interface ImportState {
    data object Idle : ImportState
    data object Probing : ImportState                     // HEAD request in flight
    data class Error(val message: String) : ImportState
    data class Success(val model: OfflineModelInfo) : ImportState
}

@Composable
private fun ImportModelDialog(
    importState: ImportState,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }

    // Basic client-side URL sanity check (no network needed)
    val urlError: String? = remember(urlInput) {
        val u = urlInput.trim()
        when {
            u.isBlank()                                         -> null   // not typed yet
            !u.startsWith("http://") && !u.startsWith("https://") ->
                "URL must start with http:// or https://"
            !u.contains(".gguf", ignoreCase = true)             ->
                "URL must point to a .gguf file"
            else                                               -> null
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Supported: Hugging Face, direct HTTP/S links, any host that returns a .gguf file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Model URL") },
                    placeholder = { Text("https://…/model.gguf") },
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

                // State feedback
                when (importState) {
                    is ImportState.Probing -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Checking URL…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is ImportState.Error -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            importState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is ImportState.Success -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "✓ \"${importState.model.displayName}\" added — starting download",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> Unit
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tip: /blob/ Hugging Face URLs are converted to direct downloads automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onImport(urlInput.trim()) },
                enabled = canImport
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = importState !is ImportState.Probing
            ) {
                Text("Cancel")
            }
        }
    )
}

// ─── Shared item composables ──────────────────────────────────────────────────

@Composable
private fun BrowseModelItem(
    model: OfflineModelInfo,
    isDownloading: Boolean,
    downloadProgress: Float?,
    onDownload: () -> Unit,
    onChat: () -> Unit
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
                    "${model.size} • ${model.family}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (isDownloading && downloadProgress != null) {
                CircularProgressIndicator(
                    progress = downloadProgress.coerceIn(0f, 1f),
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
private fun CloudModelItem(
    model: OllamaModelInfo,
    onChat: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onChat) {
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
                    "${model.size} • ${model.family}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.Chat,
                contentDescription = "Chat",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

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
    "custom"    -> Color(0xFF9C27B0)
    else        -> Color(0xFF607D8B)
}
