package com.piumal.filedownloadmanager.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.piumal.filedownloadmanager.ui.about.AboutScreen
import com.piumal.filedownloadmanager.ui.browser.BrowserScreen
import com.piumal.filedownloadmanager.ui.downloads.DownloadScreen
import com.piumal.filedownloadmanager.ui.downloads.viewmodel.MoreOptionsViewModel
import com.piumal.filedownloadmanager.ui.settings.SettingsScreen
import com.piumal.filedownloadmanager.ui.settings.screens.AdvancedSettingsScreen
import com.piumal.filedownloadmanager.ui.settings.screens.DownloadSettingsScreen
import com.piumal.filedownloadmanager.ui.settings.screens.NotificationSettingsScreen
import com.piumal.filedownloadmanager.ui.support.SupportScreen

/**
 * Navigation Graph for the app
 * Handles navigation between different screens
 *
 * @param navController Navigation controller for handling navigation
 * @param moreOptionsViewModel Shared ViewModel for MoreOptions menu (passed from MainScreen)
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,
    moreOptionsViewModel: MoreOptionsViewModel? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Downloads.route
    ) {
       /* composable(Screen.Home.route) {
            HomeScreen()
        }*/
        composable(Screen.Downloads.route) {
            // Pass the shared ViewModel to DownloadScreen if provided
            if (moreOptionsViewModel != null) {
                DownloadScreen(moreOptionsViewModel = moreOptionsViewModel)
            } else {
                DownloadScreen()
            }
        }
        composable(Screen.Browser.route) {
            BrowserScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToDownloadSettings = {
                    navController.navigate(Screen.DownloadSettings.route)
                },
                onNavigateToNotificationSettings = {
                    navController.navigate(Screen.NotificationSettings.route)
                },
                onNavigateToAdvancedSettings = {
                    navController.navigate(Screen.AdvancedSettings.route)
                }
            )
        }
        composable(Screen.Support.route) {
            SupportScreen()
        }

        composable(Screen.About.route){
            AboutScreen()
        }

        // Settings detail screens
        composable(Screen.DownloadSettings.route) {
            DownloadSettingsScreen()
        }
        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen()
        }
        composable(Screen.AdvancedSettings.route) {
            AdvancedSettingsScreen()
        }
    }
}
