package com.piumal.filedownloadmanager.ui.settings.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piumal.filedownloadmanager.ui.settings.SettingsUiState
import com.piumal.filedownloadmanager.ui.settings.SettingsViewModel
import com.piumal.filedownloadmanager.ui.settings.components.SettingItem
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * NotificationSettingsScreen
 *
 * Detail screen for Notification Settings options.
 * Contains notification-related settings.
 */
@Composable
fun NotificationSettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NotificationSettingsContent(
        uiState = uiState,
        onToggleNotifications = viewModel::toggleNotifications
    )
}

@Composable
fun NotificationSettingsContent(
    uiState: SettingsUiState,
    onToggleNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        SettingItem(
            title = "Enable notifications",
            hasSwitch = true,
            isEnabled = uiState.notificationsEnabled,
            onToggle = onToggleNotifications
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationSettingsScreenPreview() {
    FileDownloadManagerTheme {
        NotificationSettingsContent(
            uiState = SettingsUiState(),
            onToggleNotifications = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationSettingsScreenDarkPreview() {
    FileDownloadManagerTheme {
        NotificationSettingsContent(
            uiState = SettingsUiState(darkMode = true),
            onToggleNotifications = {}
        )
    }
}

