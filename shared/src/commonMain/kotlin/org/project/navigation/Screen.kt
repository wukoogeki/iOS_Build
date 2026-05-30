package org.project.navigation

sealed class Screen(val route: String, val title: String) {
    data object Login : Screen("login", "登录")
    data object Dashboard : Screen("dashboard", "仪表盘")
    data object Device : Screen("device", "设备")
    data object Control : Screen("control", "控制面板")
    data object Settings : Screen("settings", "设置")
}