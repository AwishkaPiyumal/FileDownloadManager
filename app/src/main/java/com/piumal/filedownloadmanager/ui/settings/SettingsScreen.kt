package com.piumal.filedownloadmanager.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.piumal.filedownloadmanager.ui.settings.components.CollapsibleSection
import com.piumal.filedownloadmanager.ui.settings.components.SettingItem
import com.piumal.filedownloadmanager.ui.settings.components.SettingsDivider
import com.piumal.filedownloadmanager.ui.settings.screens.DownloadSettingsContent
import com.piumal.filedownloadmanager.ui.settings.screens.NotificationSettingsContent
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * SettingsScreen
 *
 * Main settings screen with:
 * - General section (always expanded)
 * - Download Settings (inline collapsible section)
 * - Notification (inline collapsible section)
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            viewModel.updateDefaultDownloadFolder(getReadablePath(uri))
        }
    }

    SettingsContent(
        uiState = uiState,
        onToggleMobileNetwork = viewModel::toggleMobileNetwork,
        onToggleDarkMode = viewModel::toggleDarkMode,
        onToggleDownloadSettings = viewModel::toggleDownloadSettingsExpanded,
        onToggleNotificationSettings = viewModel::toggleNotificationExpanded,
        onToggleAutoFetchUrl = viewModel::toggleAutoFetchUrl,
        onToggleAskDownloadFolder = viewModel::toggleAskDownloadFolder,
        onToggleAutoRemoveCompleted = viewModel::toggleAutoRemoveCompleted,
        onToggleAutoRetryFailed = viewModel::toggleAutoRetryFailed,
        onShowFolderPickerDialog = viewModel::showFolderPickerDialog,
        onHideFolderPickerDialog = viewModel::hideFolderPickerDialog,
        onConfirmFolderPicker = viewModel::updateDefaultDownloadFolder,
        onBrowseFolder = {
            val initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download")
            folderPickerLauncher.launch(initialUri)
        },
        onShowParallelDownloadDialog = viewModel::showParallelDownloadDialog,
        onHideParallelDownloadDialog = viewModel::hideParallelDownloadDialog,
        onConfirmParallelDownload = viewModel::setParallelDownload,
        onToggleNotifyCompletion = viewModel::toggleNotifyDownloadCompletion,
        onToggleNotifyFailure = viewModel::toggleNotifyDownloadFailure,
        onToggleVibrate = viewModel::toggleVibrate,
        onToggleLight = viewModel::toggleLight
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
    onToggleDownloadSettings: () -> Unit,
    onToggleNotificationSettings: () -> Unit,
    onToggleAutoFetchUrl: () -> Unit,
    onToggleAskDownloadFolder: () -> Unit,
    onToggleAutoRemoveCompleted: () -> Unit,
    onToggleAutoRetryFailed: () -> Unit,
    onShowFolderPickerDialog: () -> Unit,
    onHideFolderPickerDialog: () -> Unit,
    onConfirmFolderPicker: (String) -> Unit,
    onBrowseFolder: () -> Unit,
    onShowParallelDownloadDialog: () -> Unit,
    onHideParallelDownloadDialog: () -> Unit,
    onConfirmParallelDownload: (Int) -> Unit,
    onToggleNotifyCompletion: () -> Unit,
    onToggleNotifyFailure: () -> Unit,
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

        // Download Settings - Inline collapsible section
        CollapsibleSection(
            title = "Download Settings",
            isExpanded = uiState.isDownloadSettingsExpanded,
            onToggle = onToggleDownloadSettings,
            isCollapsible = true
        ) {
            DownloadSettingsContent(
                uiState = uiState,
                onToggleAutoFetchUrl = onToggleAutoFetchUrl,
                onToggleAskDownloadFolder = onToggleAskDownloadFolder,
                onToggleAutoRemoveCompleted = onToggleAutoRemoveCompleted,
                onToggleAutoRetryFailed = onToggleAutoRetryFailed,
                onShowFolderPickerDialog = onShowFolderPickerDialog,
                onHideFolderPickerDialog = onHideFolderPickerDialog,
                onConfirmFolderPicker = onConfirmFolderPicker,
                onBrowseFolder = onBrowseFolder,
                onShowParallelDownloadDialog = onShowParallelDownloadDialog,
                onHideParallelDownloadDialog = onHideParallelDownloadDialog,
                onConfirmParallelDownload = onConfirmParallelDownload
            )
        }

        SettingsDivider()

        // Notification - Inline collapsible section
        CollapsibleSection(
            title = "Notification",
            isExpanded = uiState.isNotificationExpanded,
            onToggle = onToggleNotificationSettings,
            isCollapsible = true
        ) {
            NotificationSettingsContent(
                uiState = uiState,
                onToggleNotifyCompletion = onToggleNotifyCompletion,
                onToggleNotifyFailure = onToggleNotifyFailure,
                onToggleVibrate = onToggleVibrate,
                onToggleLight = onToggleLight
            )
        }
    }
}

private fun getReadablePath(uri: Uri): String {
    val docId = DocumentsContract.getTreeDocumentId(uri)
    val split = docId.split(":")
    return if (split.size >= 2) {
        if (split[0] == "primary") {
            "${Environment.getExternalStorageDirectory().absolutePath}/${split[1]}"
        } else {
            "/storage/${split[0]}/${split[1]}"
        }
    } else {
        uri.path ?: "Download/FileDownloadManager"
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
            onToggleDownloadSettings = {},
            onToggleNotificationSettings = {},
            onToggleAutoFetchUrl = {},
            onToggleAskDownloadFolder = {},
            onToggleAutoRemoveCompleted = {},
            onToggleAutoRetryFailed = {},
            onShowFolderPickerDialog = {},
            onHideFolderPickerDialog = {},
            onConfirmFolderPicker = {},
            onBrowseFolder = {},
            onShowParallelDownloadDialog = {},
            onHideParallelDownloadDialog = {},
            onConfirmParallelDownload = {},
            onToggleNotifyCompletion = {},
            onToggleNotifyFailure = {},
            onToggleVibrate = {},
            onToggleLight = {}
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
            onToggleDownloadSettings = {},
            onToggleNotificationSettings = {},
            onToggleAutoFetchUrl = {},
            onToggleAskDownloadFolder = {},
            onToggleAutoRemoveCompleted = {},
            onToggleAutoRetryFailed = {},
            onShowFolderPickerDialog = {},
            onHideFolderPickerDialog = {},
            onConfirmFolderPicker = {},
            onBrowseFolder = {},
            onShowParallelDownloadDialog = {},
            onHideParallelDownloadDialog = {},
            onConfirmParallelDownload = {},
            onToggleNotifyCompletion = {},
            onToggleNotifyFailure = {},
            onToggleVibrate = {},
            onToggleLight = {}
        )
    }
}
