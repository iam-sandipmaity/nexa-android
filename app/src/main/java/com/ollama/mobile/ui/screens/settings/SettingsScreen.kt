package com.ollama.mobile.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ollama.mobile.data.config.AppConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = SettingsViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = try { AppConfig.getThemeMode() } catch (e: Exception) { AppConfig.THEME_SYSTEM },
            onThemeSelected = { theme ->
                AppConfig.setThemeMode(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache") },
            text = { Text("This will delete all cached model data. Downloaded models will not be affected.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCache()
                    showClearCacheDialog = false
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Chat History") },
            text = { Text("This will delete all your chat history. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearChatHistory()
                    showClearHistoryDialog = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = when (try { AppConfig.getThemeMode() } catch (e: Exception) { AppConfig.THEME_SYSTEM }) {
                        AppConfig.THEME_DARK -> "Dark"
                        AppConfig.THEME_LIGHT -> "Light"
                        else -> "System default"
                    },
                    onClick = { showThemeDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.TextFields,
                    title = "Font Size",
                    subtitle = "Medium",
                    onClick = { /* TODO: Font size settings */ }
                )
            }

            // Cloud Connection Section
            SettingsSection(title = "Cloud Connection") {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = uiState.baseUrl,
                            onValueChange = viewModel::updateBaseUrl,
                            label = { Text("API Base URL") },
                            placeholder = { Text("https://ollama.com/") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = uiState.apiKey,
                            onValueChange = viewModel::updateApiKey,
                            label = { Text("API Key") },
                            placeholder = { Text("Paste your Ollama API key") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null)
                            },
                            trailingIcon = {
                                when (uiState.connectionStatus) {
                                    ConnectionStatus.Connected -> Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Connected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    ConnectionStatus.Failed -> Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Failed",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    ConnectionStatus.Unknown -> Unit
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = viewModel::testConnection,
                                enabled = !uiState.isTestingConnection,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isTestingConnection) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (uiState.isTestingConnection) "Testing..." else "Test Connection")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateBaseUrl("https://ollama.com/")
                                    viewModel.updateApiKey("")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            // Model Management Section
            SettingsSection(title = "Model Management") {
                SettingsItem(
                    icon = Icons.Default.CloudDownload,
                    title = "Auto-Download Models",
                    subtitle = "Automatically download selected models",
                    onClick = { /* TODO: Toggle auto-download */ },
                    trailing = {
                        Switch(checked = true, onClick = null)
                    }
                )
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Storage Location",
                    subtitle = "Internal storage",
                    onClick = { /* TODO: Storage settings */ }
                )
                SettingsItem(
                    icon = Icons.Default.Speed,
                    title = "Model Cache",
                    subtitle = "${uiState.cacheSize}",
                    onClick = { showClearCacheDialog = true },
                    onClickAction = {
                        TextButton(onClick = { showClearCacheDialog = true }) {
                            Text("Clear")
                        }
                    }
                )
            }

            // Data & Privacy Section
            SettingsSection(title = "Data & Privacy") {
                SettingsItem(
                    icon = Icons.Default.History,
                    title = "Chat History",
                    subtitle = "${uiState.chatCount} conversations saved",
                    onClick = { /* Navigate to history */ },
                    onClickAction = {
                        TextButton(onClick = { showClearHistoryDialog = true }) {
                            Text("Clear All")
                        }
                    }
                )
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Delete all app data and settings",
                    onClick = { /* TODO: Clear all data */ },
                    isDangerous = true
                )
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Version ${uiState.appVersion}",
                    onClick = { showAboutDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Open Source Licenses",
                    subtitle = "View third-party licenses",
                    onClick = { /* TODO: Show licenses */ }
                )
                SettingsItem(
                    icon = Icons.Default.BugReport,
                    title = "Report a Bug",
                    subtitle = "Help improve the app",
                    onClick = { /* TODO: Bug reporting */ }
                )
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Rate App",
                    subtitle = "Share your feedback",
                    onClick = { /* TODO: Rate app */ }
                )
            }

            // Help Section
            SettingsSection(title = "Help") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Quick Guide",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HelpItem(number = "1", text = "Get an API key from ollama.com")
                        HelpItem(number = "2", text = "Add it in Cloud Connection above")
                        HelpItem(number = "3", text = "Download models for offline use")
                        HelpItem(number = "4", text = "Chat without internet!")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HelpItem(number: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
    onClickAction: @Composable (() -> Unit)? = null,
    isDangerous: Boolean = false
) {
    val contentColor = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (trailing != null) {
                trailing()
            } else if (onClickAction != null) {
                onClickAction()
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: Int,
    onThemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                ThemeOption(
                    title = "System default",
                    icon = Icons.Default.SettingsSystemDaydream,
                    selected = currentTheme == AppConfig.THEME_SYSTEM,
                    onClick = { onThemeSelected(AppConfig.THEME_SYSTEM) }
                )
                ThemeOption(
                    title = "Light",
                    icon = Icons.Default.LightMode,
                    selected = currentTheme == AppConfig.THEME_LIGHT,
                    onClick = { onThemeSelected(AppConfig.THEME_LIGHT) }
                )
                ThemeOption(
                    title = "Dark",
                    icon = Icons.Default.DarkMode,
                    selected = currentTheme == AppConfig.THEME_DARK,
                    onClick = { onThemeSelected(AppConfig.THEME_DARK) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ThemeOption(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surface
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer 
                   else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ollama Mobile") },
        text = {
            Column {
                Text("Version 1.0.0")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A mobile client for Ollama Cloud and local GGUF model inference.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Built with Jetpack Compose",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
