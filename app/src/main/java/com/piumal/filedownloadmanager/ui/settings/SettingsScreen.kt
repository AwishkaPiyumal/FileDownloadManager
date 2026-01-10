package com.piumal.filedownloadmanager.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piumal.filedownloadmanager.ui.settings.components.CollapsibleSection
import com.piumal.filedownloadmanager.ui.settings.components.SettingItem
import com.piumal.filedownloadmanager.ui.settings.components.SettingsDivider
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * SettingsScreen
 *
 * Main settings screen with collapsible sections:
 * - General (always expanded)
 * - Download Settings (collapsible, default collapsed)
 * - Notification (collapsible)
 * - Advanced Settings (collapsible)
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
        onToggleDownloadSettingsExpanded = viewModel::toggleDownloadSettingsExpanded,
        onToggleNotificationExpanded = viewModel::toggleNotificationExpanded,
        onToggleAdvancedSettingsExpanded = viewModel::toggleAdvancedSettingsExpanded,
        onToggleFixedDefaultFolder = viewModel::toggleFixedDefaultFolder,
        onToggleAutoFetchUrl = viewModel::toggleAutoFetchUrl,
        onToggleAskDownloadFolder = viewModel::toggleAskDownloadFolder,
        onToggleDownloadWifiOnly = viewModel::toggleDownloadWifiOnly,
        onToggleAutoRemoveCompleted = viewModel::toggleAutoRemoveCompleted,
        onToggleAutoRetryFailed = viewModel::toggleAutoRetryFailed,
        onToggleNotifications = viewModel::toggleNotifications
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
    onToggleDownloadSettingsExpanded: () -> Unit,
    onToggleNotificationExpanded: () -> Unit,
    onToggleAdvancedSettingsExpanded: () -> Unit,
    onToggleFixedDefaultFolder: () -> Unit,
    onToggleAutoFetchUrl: () -> Unit,
    onToggleAskDownloadFolder: () -> Unit,
    onToggleDownloadWifiOnly: () -> Unit,
    onToggleAutoRemoveCompleted: () -> Unit,
    onToggleAutoRetryFailed: () -> Unit,
    onToggleNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // General Section (always expanded, not collapsible)
        CollapsibleSection(
            title = "General",
            isExpanded = true,
            onToggle = {},
            isCollapsible = false
        ) {
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
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

        // Download Settings Section (collapsible, default collapsed)
        CollapsibleSection(
            title = "Download Settings",
            isExpanded = uiState.isDownloadSettingsExpanded,
            onToggle = onToggleDownloadSettingsExpanded
        ) {
            SettingItem(
                title = "Default download folder",
                subtitle = uiState.defaultDownloadFolder,
                hasChevron = true,
                onClick = { /* Open folder picker */ }
            )
            SettingsDivider()
            SettingItem(
                title = "Fixed default download folder",
                hasSwitch = true,
                isEnabled = uiState.fixedDefaultDownloadFolder,
                onToggle = onToggleFixedDefaultFolder
            )
            SettingsDivider()
            SettingItem(
                title = "Auto fetch URL",
                hasSwitch = true,
                isEnabled = uiState.autoFetchUrl,
                onToggle = onToggleAutoFetchUrl
            )
            SettingsDivider()
            SettingItem(
                title = "Ask download folder",
                hasSwitch = true,
                isEnabled = uiState.askDownloadFolder,
                onToggle = onToggleAskDownloadFolder
            )
            SettingsDivider()
            SettingItem(
                title = "Parallel file download",
                subtitle = "${uiState.parallelFileDownload} files",
                hasChevron = true,
                onClick = { /* Open parallel download picker */ }
            )
            SettingsDivider()
            SettingItem(
                title = "Download WiFi only",
                hasSwitch = true,
                isEnabled = uiState.downloadWifiOnly,
                onToggle = onToggleDownloadWifiOnly
            )
            SettingsDivider()
            SettingItem(
                title = "Automatically remove completed download from list",
                hasSwitch = true,
                isEnabled = uiState.autoRemoveCompleted,
                onToggle = onToggleAutoRemoveCompleted
            )
            SettingsDivider()
            SettingItem(
                title = "Automatically retry failed download",
                hasSwitch = true,
                isEnabled = uiState.autoRetryFailed,
                onToggle = onToggleAutoRetryFailed
            )
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

        // Notification Section (collapsible)
        CollapsibleSection(
            title = "Notification",
            isExpanded = uiState.isNotificationExpanded,
            onToggle = onToggleNotificationExpanded
        ) {
            SettingItem(
                title = "Enable notifications",
                hasSwitch = true,
                isEnabled = uiState.notificationsEnabled,
                onToggle = onToggleNotifications
            )
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

        // Advanced Settings Section (collapsible)
        CollapsibleSection(
            title = "Advanced Settings",
            isExpanded = uiState.isAdvancedSettingsExpanded,
            onToggle = onToggleAdvancedSettingsExpanded
        ) {
            SettingItem(
                title = "Max retry count",
                subtitle = "${uiState.maxRetryCount} times",
                hasChevron = true,
                onClick = { /* Open retry count picker */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FileDownloadManagerTheme {
        SettingsContent(
            uiState = SettingsUiState(
                isDownloadSettingsExpanded = true
            ),
            onToggleMobileData = {},
            onToggleDarkMode = {},
            onToggleDownloadSettingsExpanded = {},
            onToggleNotificationExpanded = {},
            onToggleAdvancedSettingsExpanded = {},
            onToggleFixedDefaultFolder = {},
            onToggleAutoFetchUrl = {},
            onToggleAskDownloadFolder = {},
            onToggleDownloadWifiOnly = {},
            onToggleAutoRemoveCompleted = {},
            onToggleAutoRetryFailed = {},
            onToggleNotifications = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenDarkPreview() {
    FileDownloadManagerTheme {
        SettingsContent(
            uiState = SettingsUiState(
                darkMode = true,
                isDownloadSettingsExpanded = true
            ),
            onToggleMobileData = {},
            onToggleDarkMode = {},
            onToggleDownloadSettingsExpanded = {},
            onToggleNotificationExpanded = {},
            onToggleAdvancedSettingsExpanded = {},
            onToggleFixedDefaultFolder = {},
            onToggleAutoFetchUrl = {},
            onToggleAskDownloadFolder = {},
            onToggleDownloadWifiOnly = {},
            onToggleAutoRemoveCompleted = {},
            onToggleAutoRetryFailed = {},
            onToggleNotifications = {}
        )
    }
}
