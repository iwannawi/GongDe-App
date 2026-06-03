package com.gongde.app.navigation

/**
 * 导航路由定义
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Achievements : Screen("achievements")
    data object Settings : Screen("settings")
    data object Focus : Screen("focus")
    data object Asmr : Screen("asmr")
}
