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
import com.ollama.mobile.ui.screens.model.ModelBrowserScreen
import com.ollama.mobile.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Models : Screen("models")
    object Chat : Screen("chat/{modelName}") {
        fun createRoute(modelName: String) = "chat/$modelName"
    }
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
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("modelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val modelName = backStackEntry.arguments?.getString("modelName") ?: ""
            CrashLogger.log("Showing Chat screen for model: $modelName")
            ChatScreen(
                selectedModel = modelName,
                onNavigateBack = {
                    CrashLogger.log("Navigating back from chat")
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    CrashLogger.log("Navigating to settings")
                    navController.navigate(Screen.Settings.route)
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
