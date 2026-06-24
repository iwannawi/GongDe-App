package com.gongde.app.navigation

/**
 * 导航路由定义
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Collection : Screen("collection")
    data object Records : Screen("records")
    data object Achievements : Screen("achievements")
    data object Settings : Screen("settings")
}
