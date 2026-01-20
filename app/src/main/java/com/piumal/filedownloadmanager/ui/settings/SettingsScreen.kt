package com.piumal.filedownloadmanager.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piumal.filedownloadmanager.R
import com.piumal.filedownloadmanager.ui.settings.components.CollapsibleSection
import com.piumal.filedownloadmanager.ui.settings.components.SettingItem
import com.piumal.filedownloadmanager.ui.settings.components.SettingsDivider
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * SettingsScreen
 *
 * Main settings screen with:
 * - General section (always expanded)
 * - Download Settings (navigates to detail screen)
 * - Notification (navigates to detail screen)
 * - Advanced Settings (navigates to detail screen)
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateToDownloadSettings: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToAdvancedSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onToggleMobileNetwork = viewModel::toggleMobileNetwork,
        onToggleDarkMode = viewModel::toggleDarkMode,
        onNavigateToDownloadSettings = onNavigateToDownloadSettings,
        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
        onNavigateToAdvancedSettings = onNavigateToAdvancedSettings
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
    onToggleMobileNetwork: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onNavigateToDownloadSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToAdvancedSettings: () -> Unit,
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
                title = "Use mobile network",
                hasSwitch = true,
                isEnabled = uiState.useMobileNetwork,
                onToggle = onToggleMobileNetwork
            )
            SettingsDivider()
            SettingItem(
                title = "Dark Mode",
                hasSwitch = true,
                isEnabled = uiState.darkMode,
                onToggle = onToggleDarkMode
            )
        }

        SettingsDivider()

        // Download Settings - Navigable Section
        SettingsSectionHeader(
            title = "Download Settings",
            onClick = onNavigateToDownloadSettings
        )

        SettingsDivider()

        // Notification - Navigable Section
        SettingsSectionHeader(
            title = "Notification",
            onClick = onNavigateToNotificationSettings
        )

        SettingsDivider()

        // Advanced Settings - Navigable Section
        SettingsSectionHeader(
            title = "Advanced Settings",
            onClick = onNavigateToAdvancedSettings
        )
    }
}

/**
 * SettingsSectionHeader
 *
 * A clickable section header with chevron_right icon that navigates to a detail screen.
 *
 * @param title Section title text
 * @param onClick Callback when the section is clicked
 */
@Composable
fun SettingsSectionHeader(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.chevron_right_24px),
            contentDescription = "Go to $title",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FileDownloadManagerTheme {
        SettingsContent(
            uiState = SettingsUiState(),
            onToggleMobileNetwork = {},
            onToggleDarkMode = {},
            onNavigateToDownloadSettings = {},
            onNavigateToNotificationSettings = {},
            onNavigateToAdvancedSettings = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenDarkPreview() {
    FileDownloadManagerTheme {
        SettingsContent(
            uiState = SettingsUiState(
                darkMode = true
            ),
            onToggleMobileNetwork = {},
            onToggleDarkMode = {},
            onNavigateToDownloadSettings = {},
            onNavigateToNotificationSettings = {},
            onNavigateToAdvancedSettings = {}
        )
    }
}
