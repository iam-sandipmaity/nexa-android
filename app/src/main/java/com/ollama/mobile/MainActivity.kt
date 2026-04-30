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
import com.ollama.mobile.data.config.AppConfig
import com.ollama.mobile.ui.OllamaApp
import com.ollama.mobile.ui.theme.OllamaMobileTheme

class MainActivity : ComponentActivity() {
    
    private var hasError = false
    private var errorMessage = ""
    private var crashLog = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppConfig.init(applicationContext)
        CrashLogger.init(cacheDir)
        CrashLogger.log("MainActivity.onCreate START")
        
        val errorHandler = ErrorHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            CrashLogger.log("CRASH: ${thread.name}")
            CrashLogger.logException(throwable)
            errorHandler.showError(throwable.message ?: "Unknown error", CrashLogger.getCrashLog() ?: "")
        }
        
        try {
            CrashLogger.log("Setting up content...")
            setContent {
                OllamaMobileTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (errorHandler.hasError.value) {
                            ErrorScreen(errorHandler.errorMessage.value, errorHandler.crashLog.value)
                        } else {
                            OllamaApp()
                        }
                    }
                }
            }
            CrashLogger.log("Content set successfully")
        } catch (e: Exception) {
            CrashLogger.logException(e)
            errorHandler.showError(e.message ?: "Init error", CrashLogger.getCrashLog() ?: "")
            setContent {
                OllamaMobileTheme {
                    ErrorScreen(errorHandler.errorMessage.value, errorHandler.crashLog.value)
                }
            }
        }
    }
}

class ErrorHandler {
    val hasError = mutableStateOf(false)
    val errorMessage = mutableStateOf("")
    val crashLog = mutableStateOf("")

    fun showError(message: String, log: String) {
        hasError.value = true
        errorMessage.value = message
        crashLog.value = log
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
