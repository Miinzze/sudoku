package com.sudokuai.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import com.sudokuai.app.ui.editor.EditorScreen
import com.sudokuai.app.ui.game.GameScreen
import com.sudokuai.app.ui.home.HomeScreen
import com.sudokuai.app.ui.library.LibraryScreen
import com.sudokuai.app.ui.settings.SettingsScreen
import com.sudokuai.app.ui.statistics.StatisticsScreen

/** Destinations that show the bottom navigation bar (Game/Editor are pushed on top, full-screen). */
private val bottomNavRoutes = Screen.bottomNavScreens.map { it.route }.toSet()

@Composable
fun SudokuAiNavHost(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination?.route in bottomNavRoutes

    Scaffold(
        bottomBar = { if (showBottomBar) SudokuBottomNavBar(navController) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(padding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToGame = { id -> navController.navigate(Screen.Game.createRoute(id)) },
                    onNavigateToEditor = { navController.navigate(Screen.Editor.route) },
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(onOpenGame = { id -> navController.navigate(Screen.Game.createRoute(id)) })
            }
            composable(Screen.Statistics.route) { StatisticsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }

            composable(
                route = Screen.Game.route,
                arguments = listOf(navArgument(Screen.Game.ARG_GAME_ID) { type = NavType.LongType }),
            ) { backStackEntryArg ->
                val gameId = backStackEntryArg.arguments?.getLong(Screen.Game.ARG_GAME_ID) ?: 0L
                GameScreen(gameId = gameId, onBack = { navController.popBackStack() })
            }

            composable(Screen.Editor.route) {
                EditorScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToGame = { id ->
                        navController.navigate(Screen.Game.createRoute(id)) {
                            popUpTo(Screen.Editor.route) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
