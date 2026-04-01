package com.ollama.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ollama.mobile.ui.OllamaApp
import com.ollama.mobile.ui.theme.OllamaMobileTheme

class MainActivity : ComponentActivity() {
    
    private var hasError by mutableStateOf(false)
    private var errorMessage by mutableStateOf("")
    private var crashLog by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        CrashLogger.init(cacheDir)
        CrashLogger.log("MainActivity.onCreate START")
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            CrashLogger.log("CRASH: ${thread.name}")
            CrashLogger.logException(throwable)
            errorMessage = throwable.message ?: "Unknown error"
            crashLog = CrashLogger.getCrashLog() ?: ""
            hasError = true
        }
        
        try {
            CrashLogger.log("Setting up content...")
            setContent {
                OllamaMobileTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (hasError) {
                            ErrorScreen(errorMessage, crashLog)
                        } else {
                            OllamaApp()
                        }
                    }
                }
            }
            CrashLogger.log("Content set successfully")
        } catch (e: Exception) {
            CrashLogger.logException(e)
            errorMessage = e.message ?: "Init error"
            crashLog = CrashLogger.getCrashLog() ?: ""
            hasError = true
            setContent {
                OllamaMobileTheme {
                    ErrorScreen(errorMessage, crashLog)
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(message: String, log: String) {
    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text("CRASH", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Error: $message")
        Spacer(modifier = Modifier.height(16.dp))
        if (log.isNotEmpty()) {
            Text("Log:", style = MaterialTheme.typography.titleMedium)
            Text(log, style = MaterialTheme.typography.bodySmall)
        }
    }
}
