package com.ollama.mobile.ui.screens.settings

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
                    Text("Clear", color = MaterialTheme.colorScheme.error)
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
                title = { 
                    Text(
                        "Settings", 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Cloud Connection Section - Most Important
            SettingsSectionHeader(
                icon = Icons.Default.Cloud,
                title = "Cloud Connection",
                subtitle = "Configure Ollama Cloud API"
            )
            
            SettingsCard {
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
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
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
                                    tint = Color(0xFF4CAF50)
                                )
                                ConnectionStatus.Failed -> Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Failed",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                ConnectionStatus.Unknown -> Unit
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = viewModel::testConnection,
                            enabled = !uiState.isTestingConnection,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
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
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }
                    }
                }
            }

            // Appearance Section
            SettingsSectionHeader(
                icon = Icons.Default.Palette,
                title = "Appearance",
                subtitle = "Customize the app look"
            )
            
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = when (try { AppConfig.getThemeMode() } catch (e: Exception) { AppConfig.THEME_SYSTEM }) {
                        AppConfig.THEME_DARK -> "Dark Mode"
                        AppConfig.THEME_LIGHT -> "Light Mode"
                        else -> "System Default"
                    },
                    onClick = { showThemeDialog = true }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsClickableItem(
                    icon = Icons.Default.TextFields,
                    title = "Font Size",
                    subtitle = "Medium",
                    onClick = { /* TODO: Font size settings */ }
                )
            }

            // Model Management Section
            SettingsSectionHeader(
                icon = Icons.Default.Storage,
                title = "Model Management",
                subtitle = "Manage downloaded models"
            )
            
            SettingsCard {
                SettingsToggleItem(
                    icon = Icons.Default.CloudDownload,
                    title = "Auto-Download",
                    subtitle = "Automatically download selected models",
                    checked = true,
                    onCheckedChange = { /* TODO: Toggle auto-download */ }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsClickableItem(
                    icon = Icons.Default.Folder,
                    title = "Storage Location",
                    subtitle = "Internal storage",
                    onClick = { /* TODO: Storage settings */ }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsClickableItem(
                    icon = Icons.Default.Speed,
                    title = "Model Cache",
                    subtitle = "${uiState.cacheSize}",
                    onClick = { showClearCacheDialog = true },
                    action = {
                        TextButton(onClick = { showClearCacheDialog = true }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            // Data & Privacy Section
            SettingsSectionHeader(
                icon = Icons.Default.Security,
                title = "Data & Privacy",
                subtitle = "Manage your data"
            )
            
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Default.History,
                    title = "Chat History",
                    subtitle = "${uiState.chatCount} conversations",
                    onClick = { /* TODO: View history */ },
                    action = {
                        TextButton(onClick = { showClearHistoryDialog = true }) {
                            Text("Clear All", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsClickableItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Delete all app data and settings",
                    onClick = { /* TODO: Clear all data */ },
                    isDangerous = true
                )
            }

            // About Section
            SettingsSectionHeader(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App information"
            )
            
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Default.Psychology,
                    title = "Ollama Mobile",
                    subtitle = "Version ${uiState.appVersion}",
                    onClick = { showAboutDialog = true }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsClickableItem(
                    icon = Icons.Default.Description,
                    title = "Open Source Licenses",
                    subtitle = "View third-party licenses",
                    onClick = { /* TODO: Show licenses */ }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsClickableItem(
                    icon = Icons.Default.BugReport,
                    title = "Report a Bug",
                    subtitle = "Help improve the app",
                    onClick = { /* TODO: Bug reporting */ }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsClickableItem(
                    icon = Icons.Default.Star,
                    title = "Rate App",
                    subtitle = "Share your feedback",
                    onClick = { /* TODO: Rate app */ }
                )
            }

            // Contributor Section
            SettingsSectionHeader(
                icon = Icons.Default.Person,
                title = "Contributor",
                subtitle = "Developer information"
            )
            
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Sandip Maity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "profile.sandipmaity.me",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO: Open GitHub */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GitHub")
                        }
                        
                        OutlinedButton(
                            onClick = { /* TODO: Open LinkedIn */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Work,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LinkedIn")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO: Open X/Twitter */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("X / Twitter", style = MaterialTheme.typography.labelMedium)
                        }
                        
                        OutlinedButton(
                            onClick = { /* TODO: Open Instagram */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Instagram")
                        }
                    }
                }
            }

            // Help Section
            SettingsSectionHeader(
                icon = Icons.Default.Help,
                title = "Help",
                subtitle = "Quick guide to get started"
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    action: @Composable (() -> Unit)? = null,
    isDangerous: Boolean = false
) {
    val contentColor = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        if (action != null) {
            action()
        } else {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
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
                .size(22.dp)
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
            style = MaterialTheme.typography.bodyMedium
        )
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
        title = { Text("Choose Theme", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer 
                     else MaterialTheme.colorScheme.surfaceVariant,
        label = "bg"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.weight(1f))
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Ollama Mobile", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("Version 1.0.0", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "A mobile client for Ollama Cloud and local GGUF model inference. Chat with AI models online or offline.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Built with Jetpack Compose",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
