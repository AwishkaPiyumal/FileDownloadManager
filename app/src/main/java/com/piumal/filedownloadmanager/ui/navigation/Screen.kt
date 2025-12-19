package com.piumal.filedownloadmanager.ui.navigation
import androidx.compose.ui.graphics.vector.ImageVector

import com.piumal.filedownloadmanager.R

sealed class Screen(
    val route: String,
    val title: String,
    val iconRes : Int? = null,
    val icon: ImageVector? = null
) {
    object Downloads : Screen("downloads", "Downloads", iconRes= R.drawable.download_24px)
    object Browser : Screen("browser", "Browser", iconRes= R.drawable.web_asset_24px)
    object Settings : Screen("settings", "Settings", iconRes= R.drawable.settings_24px)
    object Support : Screen("help", "Help & Support", iconRes= R.drawable.help_center_24px)

    object About : Screen("info", "About", iconRes= R.drawable.info_24px)



}

val bottomNavItems = listOf(
    //Screen.Home,
    Screen.Downloads,
    Screen.Browser,
    Screen.Settings,
    Screen.Support,
    Screen.About
)
