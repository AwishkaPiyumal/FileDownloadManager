package com.piumal.filedownloadmanager.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.piumal.filedownloadmanager.ui.browser.BrowserScreen
import com.piumal.filedownloadmanager.ui.downloads.DownloadScreen
import com.piumal.filedownloadmanager.ui.home.HomeScreen
import com.piumal.filedownloadmanager.ui.settings.SettingsScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Downloads.route
    ) {
       /* composable(Screen.Home.route) {
            HomeScreen()
        }*/
        composable(Screen.Downloads.route) {
            DownloadScreen()
        }
        composable(Screen.Browser.route) {
            BrowserScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
