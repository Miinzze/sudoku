package com.sudokuai.app.navigation

/** Route definitions. Game/Editor are pushed on top of the bottom-nav graph, not part of it. */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")

    object Game : Screen("game/{gameId}") {
        fun createRoute(gameId: Long) = "game/$gameId"
        const val ARG_GAME_ID = "gameId"
    }

    object Editor : Screen("editor")

    companion object {
        val bottomNavScreens = listOf(Home, Library, Statistics, Settings)
    }
}
