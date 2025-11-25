package com.piumal.filedownloadmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.piumal.filedownloadmanager.R

sealed class Screen(
    val route: String,
    val title: String,
    val iconRes : Int? = null,
    val icon: ImageVector? = null
) {
    //object Home : Screen("home", "Home", Icons.Default.Home)
    object Downloads : Screen("downloads", "Downloads", iconRes= R.drawable.download_24px)
    object Browser : Screen("browser", "Browser", iconRes= R.drawable.web_asset_24px)
    object Settings : Screen("settings", "Settings", iconRes= R.drawable.settings_24px)

    object Help : Screen("settings", "Settings", iconRes= R.drawable.help_center_24px)

    object info : Screen("settings", "Settings", iconRes= R.drawable.info_24px)



}

val bottomNavItems = listOf(
    //Screen.Home,
    Screen.Downloads,
    Screen.Browser,
    Screen.Settings,
    Screen.Help,
    Screen.info
)
