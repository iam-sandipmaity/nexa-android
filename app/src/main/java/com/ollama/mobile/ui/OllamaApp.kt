package com.ollama.mobile.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
fun OllamaApp(
    onError: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Models.route
    ) {
        composable(Screen.Models.route) {
            ModelBrowserScreen(
                onNavigateToChat = { modelName ->
                    navController.navigate(Screen.Chat.createRoute(modelName))
                }
            )
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("modelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val modelName = backStackEntry.arguments?.getString("modelName") ?: ""
            ChatScreen(
                selectedModel = modelName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
