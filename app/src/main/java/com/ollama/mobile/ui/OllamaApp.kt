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
    object Chat : Screen("chat/{modelName}?chatId={chatId}") {
        fun createRoute(modelName: String, chatId: String? = null) = 
            if (chatId != null) "chat/$modelName?chatId=$chatId" else "chat/$modelName"
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
        startDestination = Screen.Models.route
    ) {
        composable(Screen.Models.route) {
            CrashLogger.log("Showing Models screen")
            ModelBrowserScreen(
                onNavigateToChat = { modelName ->
                    CrashLogger.log("Navigating to chat with model: $modelName")
                    navController.navigate(Screen.Chat.createRoute(modelName))
                },
                onNavigateToSettings = {
                    CrashLogger.log("Navigating to settings")
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToHistory = {
                    CrashLogger.log("Navigating to history")
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("modelName") { type = NavType.StringType },
                navArgument("chatId") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null 
                }
            )
        ) { backStackEntry ->
            val modelName = backStackEntry.arguments?.getString("modelName") ?: ""
            val chatId = backStackEntry.arguments?.getString("chatId")
            CrashLogger.log("Showing Chat screen for model: $modelName, chatId: $chatId")
            ChatScreen(
                selectedModel = modelName,
                existingChatId = chatId,
                onNavigateBack = {
                    CrashLogger.log("Navigating back from chat")
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    CrashLogger.log("Navigating to settings")
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToHistory = {
                    CrashLogger.log("Navigating to history from chat")
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(Screen.History.route) {
            CrashLogger.log("Showing History screen")
            HistoryScreen(
                onNavigateToChat = { modelName, chatId ->
                    CrashLogger.log("Navigating to chat from history: $modelName, $chatId")
                    navController.navigate(Screen.Chat.createRoute(modelName, chatId))
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