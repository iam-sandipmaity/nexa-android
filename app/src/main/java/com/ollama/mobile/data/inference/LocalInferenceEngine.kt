package com.ollama.mobile.data.inference

import com.ollama.mobile.domain.model.ChatMessage

/**
 * Local inference engine using llama.cpp for GGUF models
 * This class provides native methods that connect to the C++ JNI layer
 */
class LocalInferenceEngine {
    
    companion object {
        init {
            System.loadLibrary("llama")
        }
    }
    
    private var isModelLoaded = false
    
    /**
     * Load a GGUF model from the given file path
     */
    fun loadModel(modelPath: String, contextSize: Int = 2048, threads: Int = 4): Boolean {
        return try {
            isModelLoaded = nativeLoadModel(modelPath, contextSize, threads)
            isModelLoaded
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate a response for the given prompt
     */
    fun generate(prompt: String): String {
        if (!isModelLoaded) {
            return "Error: No model loaded"
        }
        return try {
            nativeGenerate(prompt)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    /**
     * Generate response from chat messages
     */
    fun chat(messages: List<ChatMessage>): String {
        val prompt = buildPrompt(messages)
        return generate(prompt)
    }
    
    /**
     * Stop ongoing generation
     */
    fun stop() {
        try {
            nativeStop()
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    /**
     * Free model resources
     */
    fun free() {
        try {
            nativeFree()
            isModelLoaded = false
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    fun isLoaded(): Boolean = isModelLoaded
    
    private fun buildPrompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        for (msg in messages) {
            val role = if (msg.role == "user") "User" else "Assistant"
            sb.append("$role: ${msg.content}\n")
        }
        sb.append("Assistant: ")
        return sb.toString()
    }
    
    // Native method declarations - linked to JNI C++ code
    private external fun nativeLoadModel(modelPath: String, contextSize: Int, threads: Int): Boolean
    private external fun nativeGenerate(prompt: String): String
    private external fun nativeStop()
    private external fun nativeFree()
}
