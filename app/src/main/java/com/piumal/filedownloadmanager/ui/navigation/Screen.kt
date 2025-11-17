package com.piumal.filedownloadmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Downloads : Screen("downloads", "Downloads", Icons.Default.Home)
    object Browser : Screen("browser", "Browser", Icons.Default.Home)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Downloads,
    Screen.Browser,
    Screen.Settings
)
