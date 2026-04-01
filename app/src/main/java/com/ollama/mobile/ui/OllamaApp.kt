package com.ollama.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ollama.mobile.CrashLogger
import com.ollama.mobile.ui.screens.chat.ChatScreen
import com.ollama.mobile.ui.screens.history.HistoryScreen
import com.ollama.mobile.ui.screens.model.ModelBrowserScreen
import com.ollama.mobile.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Models : Screen("models")
    object Chat : Screen("chat?modelName={modelName}&chatId={chatId}") {
        fun createRoute(modelName: String? = null, chatId: String? = null): String {
            return buildString {
                append("chat")
                val params = mutableListOf<String>()
                if (modelName != null) params.add("modelName=$modelName")
                if (chatId != null) params.add("chatId=$chatId")
                if (params.isNotEmpty()) {
                    append("?${params.joinToString("&")}")
                }
            }
        }
    }
    object History : Screen("history")
    object Settings : Screen("settings")
}

@Composable
fun OllamaApp() {
    CrashLogger.log("OllamaApp composable started")

    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        CrashLogger.log("NavController initialized")
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route
    ) {
        composable(Screen.Models.route) {
            CrashLogger.log("Showing Models screen")
            ModelBrowserScreen(
                onNavigateToChat = { modelName ->
                    CrashLogger.log("Navigating to chat with model: $modelName")
                    navController.navigate(Screen.Chat.createRoute(modelName)) {
                        popUpTo(Screen.Chat.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    CrashLogger.log("Navigating to settings")
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToHistory = {
                    CrashLogger.log("Navigating to history")
                    navController.navigate(Screen.History.route)
                },
                onNavigateBack = {
                    CrashLogger.log("Navigating back from models")
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("modelName") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null 
                },
                navArgument("chatId") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null 
                }
            )
        ) { backStackEntry ->
            val modelName = backStackEntry.arguments?.getString("modelName")
            val chatId = backStackEntry.arguments?.getString("chatId")
            CrashLogger.log("Showing Chat screen for model: $modelName, chatId: $chatId")
            ChatScreen(
                selectedModel = modelName ?: "",
                existingChatId = chatId,
                onNavigateToSettings = {
                    CrashLogger.log("Navigating to settings")
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToModels = {
                    CrashLogger.log("Navigating to models from chat")
                    navController.navigate(Screen.Models.route)
                }
            )
        }

        composable(Screen.History.route) {
            CrashLogger.log("Showing History screen")
            HistoryScreen(
                onNavigateToChat = { modelName, chatId ->
                    CrashLogger.log("Navigating to chat from history: $modelName, $chatId")
                    navController.navigate(Screen.Chat.createRoute(modelName, chatId)) {
                        popUpTo(Screen.Chat.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    CrashLogger.log("Navigating back from history")
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            CrashLogger.log("Showing Settings screen")
            SettingsScreen(
                onNavigateBack = {
                    CrashLogger.log("Navigating back from settings")
                    navController.popBackStack()
                }
            )
        }
    }

    CrashLogger.log("OllamaApp composable completed")
}