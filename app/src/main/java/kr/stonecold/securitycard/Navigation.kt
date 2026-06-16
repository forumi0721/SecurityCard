package kr.stonecold.securitycard

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kr.stonecold.securitycard.ui.main.MainScreen
import kr.stonecold.securitycard.ui.login.LoginScreen
import kr.stonecold.securitycard.ui.edit.EditScreen
import kr.stonecold.securitycard.ui.view.ViewScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
        composable("main") {
            MainScreen(
                onItemClick = { id -> navController.navigate("view/$id") },
                onAddClick = { navController.navigate("edit") },
                onSettingsClick = { navController.navigate("settings") },
                modifier = Modifier.safeDrawingPadding().padding(16.dp)
            )
        }
        composable(
            route = "edit?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val idParam = backStackEntry.arguments?.getString("id")
            val id = idParam?.toLongOrNull()
            EditScreen(id = id, onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = "view/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            ViewScreen(
                id = id,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate("edit?id=$id") }
            )
        }
        composable("settings") {
            kr.stonecold.securitycard.ui.settings.SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
