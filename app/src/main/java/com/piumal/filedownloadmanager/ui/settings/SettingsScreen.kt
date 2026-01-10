package com.piumal.filedownloadmanager.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piumal.filedownloadmanager.ui.settings.components.SectionHeader
import com.piumal.filedownloadmanager.ui.settings.components.SettingItem
import com.piumal.filedownloadmanager.ui.settings.components.SettingsDivider
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * SettingsScreen
 *
 * Main settings screen that displays all app settings organized by sections.
 * The top app bar is handled by MainScreen, so this only contains the content.
 *
 * Sections:
 * - General: Mobile data usage, Dark mode
 * - Notifications: Enable/disable notifications
 * - Download Settings: Navigate to download preferences
 * - Advanced Settings: Navigate to advanced options
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onToggleMobileData = viewModel::toggleMobileData,
        onToggleDarkMode = viewModel::toggleDarkMode,
        onToggleNotifications = viewModel::toggleNotifications,
        onDownloadSettingsClick = { /* Navigate to download settings */ },
        onAdvancedSettingsClick = { /* Navigate to advanced settings */ }
    )
}

/**
 * SettingsContent
 *
 * Stateless composable containing all settings UI.
 * Separated from SettingsScreen for better testability and preview support.
 */
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onToggleMobileData: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleNotifications: () -> Unit,
    onDownloadSettingsClick: () -> Unit,
    onAdvancedSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // General Section
        SectionHeader(title = "General")

        SettingItem(
            title = "Use mobile data",
            hasSwitch = true,
            isEnabled = uiState.useMobileData,
            onToggle = onToggleMobileData
        )

        SettingsDivider()

        SettingItem(
            title = "Dark Mode",
            hasSwitch = true,
            isEnabled = uiState.darkMode,
            onToggle = onToggleDarkMode
        )

        SettingsDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Section
        SectionHeader(title = "Notifications")

        SettingItem(
            title = "Notifications",
            hasSwitch = true,
            isEnabled = uiState.notificationsEnabled,
            onToggle = onToggleNotifications
        )

        SettingsDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // Download Settings Section
        SectionHeader(title = "Download Settings")

        SettingItem(
            title = "Download Settings",
            hasChevron = true,
            onClick = onDownloadSettingsClick
        )

        SettingsDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced Settings Section
        SectionHeader(title = "Advanced Settings")

        SettingItem(
            title = "Advanced Settings",
            hasChevron = true,
            onClick = onAdvancedSettingsClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FileDownloadManagerTheme {
        SettingsContent(
            uiState = SettingsUiState(
                useMobileData = false,
                darkMode = false,
                notificationsEnabled = true
            ),
            onToggleMobileData = {},
            onToggleDarkMode = {},
            onToggleNotifications = {},
            onDownloadSettingsClick = {},
            onAdvancedSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenDarkPreview() {
    FileDownloadManagerTheme {
        SettingsContent(
            uiState = SettingsUiState(
                useMobileData = true,
                darkMode = true,
                notificationsEnabled = true
            ),
            onToggleMobileData = {},
            onToggleDarkMode = {},
            onToggleNotifications = {},
            onDownloadSettingsClick = {},
            onAdvancedSettingsClick = {}
        )
    }
}
