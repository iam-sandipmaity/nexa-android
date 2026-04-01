package com.ollama.mobile

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {
    private const val TAG = "OllamaMobile"
    private var logFile: File? = null

    fun init(cacheDir: File) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            logFile = File(cacheDir, "crash_$timestamp.log")
            log("Crash logger initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init crash logger", e)
        }
    }

    fun log(message: String) {
        try {
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val timestamp = dateFormat.format(Date())
                    writer.write("[$timestamp] $message\n")
                }
            }
            Log.d(TAG, message)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write log", e)
        }
    }

    fun logException(e: Throwable) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    PrintWriter(writer).use { printWriter ->
                        printWriter.write("========================================\n")
                        printWriter.write("CRASH AT: $timestamp\n")
                        printWriter.write("========================================\n")
                        e.printStackTrace(printWriter)
                        printWriter.write("\n\n")
                    }
                }
            }
            Log.e(TAG, "Exception logged: ${e.message}", e)
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to log exception", ex)
        }
    }

    fun getCrashLog(): String? {
        return try {
            logFile?.readText()
        } catch (e: Exception) {
            null
        }
    }
}
