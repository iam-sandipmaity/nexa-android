package com.ollama.mobile.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ollama.mobile.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.chatHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_history")

class ChatHistoryRepository(private val context: Context) {
    
    private val gson = Gson()
    
    data class ChatHistoryEntry(
        val id: String,
        val modelName: String,
        val messages: List<ChatMessage>,
        val lastUpdated: Long = System.currentTimeMillis()
    )
    
    fun getAllHistory(): Flow<List<ChatHistoryEntry>> {
        return context.chatHistoryDataStore.data.map { preferences ->
            val json = preferences[KEY_CHAT_HISTORY] ?: "[]"
            val type = object : TypeToken<List<ChatHistoryEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }
    
    suspend fun saveChat(chat: ChatHistoryEntry) {
        context.chatHistoryDataStore.edit { preferences ->
            val json = preferences[KEY_CHAT_HISTORY] ?: "[]"
            val type = object : TypeToken<MutableList<ChatHistoryEntry>>() {}.type
            val history: MutableList<ChatHistoryEntry> = gson.fromJson(json, type) ?: mutableListOf()
            
            // Remove existing entry with same ID
            history.removeAll { it.id == chat.id }
            
            // Add to beginning (most recent first)
            history.add(0, chat)
            
            // Keep only last 50 chats
            val trimmed = history.take(50)
            preferences[KEY_CHAT_HISTORY] = gson.toJson(trimmed)
        }
    }
    
    suspend fun deleteChat(chatId: String) {
        context.chatHistoryDataStore.edit { preferences ->
            val json = preferences[KEY_CHAT_HISTORY] ?: "[]"
            val type = object : TypeToken<MutableList<ChatHistoryEntry>>() {}.type
            val history: MutableList<ChatHistoryEntry> = gson.fromJson(json, type) ?: mutableListOf()
            history.removeAll { it.id == chatId }
            preferences[KEY_CHAT_HISTORY] = gson.toJson(history)
        }
    }
    
    suspend fun clearAllHistory() {
        context.chatHistoryDataStore.edit { preferences ->
            preferences[KEY_CHAT_HISTORY] = "[]"
        }
    }
    
    fun getChatById(chatId: String): Flow<ChatHistoryEntry?> {
        return getAllHistory().map { list ->
            list.find { it.id == chatId }
        }
    }
    
    companion object {
        private val KEY_CHAT_HISTORY = stringPreferencesKey("chat_history")
    }
}