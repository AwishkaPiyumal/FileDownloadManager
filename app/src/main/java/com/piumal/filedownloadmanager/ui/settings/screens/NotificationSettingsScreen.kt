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
import com.piumal.filedownloadmanager.ui.settings.components.SettingsDivider
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
        onToggleNotifications = viewModel::toggleNotifications,
        onToggleNotifyCompletion = viewModel::toggleNotifyDownloadCompletion,
        onToggleNotifyFailure = viewModel::toggleNotifyDownloadFailure,
        onShowCompletionRingtone = { /* TODO: open ringtone picker */ },
        onShowFailureRingtone = { /* TODO: open ringtone picker */ },
        onToggleVibrate = viewModel::toggleVibrate,
        onToggleLight = viewModel::toggleLight
    )
}

@Composable
fun NotificationSettingsContent(
    uiState: SettingsUiState,
    onToggleNotifications: () -> Unit,
    onToggleNotifyCompletion: () -> Unit,
    onToggleNotifyFailure: () -> Unit,
    onShowCompletionRingtone: () -> Unit,
    onShowFailureRingtone: () -> Unit,
    onToggleVibrate: () -> Unit,
    onToggleLight: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Master toggle
        SettingItem(
            title = "Notify download completion",
            hasSwitch = true,
            isEnabled = uiState.notifyDownloadCompletion,
            onToggle = onToggleNotifyCompletion
        )
        SettingsDivider()

        SettingItem(
            title = "Notify download failure",
            hasSwitch = true,
            isEnabled = uiState.notifyDownloadFailure,
            onToggle = onToggleNotifyFailure
        )
        SettingsDivider()

        SettingItem(
            title = "Download completion ringtone",
            subtitle = uiState.completionRingtone,
            hasChevron = true,
            onClick = onShowCompletionRingtone
        )
        SettingsDivider()

        SettingItem(
            title = "Download failure ringtone",
            subtitle = uiState.failureRingtone,
            hasChevron = true,
            onClick = onShowFailureRingtone
        )
        SettingsDivider()

        SettingItem(
            title = "Vibrate",
            hasSwitch = true,
            isEnabled = uiState.vibrateEnabled,
            onToggle = onToggleVibrate
        )
        SettingsDivider()

        SettingItem(
            title = "Light",
            hasSwitch = true,
            isEnabled = uiState.lightEnabled,
            onToggle = onToggleLight
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationSettingsScreenPreview() {
    FileDownloadManagerTheme {
        NotificationSettingsContent(
            uiState = SettingsUiState(),
            onToggleNotifications = {},
            onToggleNotifyCompletion = {},
            onToggleNotifyFailure = {},
            onShowCompletionRingtone = {},
            onShowFailureRingtone = {},
            onToggleVibrate = {},
            onToggleLight = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationSettingsScreenDarkPreview() {
    FileDownloadManagerTheme {
        NotificationSettingsContent(
            uiState = SettingsUiState(darkMode = true),
            onToggleNotifications = {},
            onToggleNotifyCompletion = {},
            onToggleNotifyFailure = {},
            onShowCompletionRingtone = {},
            onShowFailureRingtone = {},
            onToggleVibrate = {},
            onToggleLight = {}
        )
    }
}
