@file:Suppress("unused")

package com.piumal.filedownloadmanager.ui.settings.screens

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piumal.filedownloadmanager.domain.util.DownloadStoragePaths
import com.piumal.filedownloadmanager.ui.settings.SettingsUiState
import com.piumal.filedownloadmanager.ui.settings.SettingsViewModel
import com.piumal.filedownloadmanager.ui.settings.components.SettingItem
import com.piumal.filedownloadmanager.ui.settings.components.SettingsDivider
import com.piumal.filedownloadmanager.ui.settings.dialogs.FolderPickerDialog
import com.piumal.filedownloadmanager.ui.settings.dialogs.ParallelDownloadDialog
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * DownloadSettingsScreen
 *
 * Detail screen for Download Settings options.
 * Contains all download-related settings that were previously inside the collapsible section.
 *
 * Settings order:
 * 1. Default download folder
 * 2. Ask download folder
 * 3. Auto fetch URL
 * 4. Parallel file download
 * 5. Automatically remove completed
 * 6. Automatically retry failed
 */
@Composable
fun DownloadSettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    DownloadSettingsStandaloneContent(
        uiState = uiState,
        viewModel = viewModel
    )
}

/**
 * Converts a content URI to a readable folder path
 */
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
        uri.path ?: DownloadStoragePaths.DEFAULT_UI_FOLDER_LABEL
    }
}

@Composable
fun DownloadSettingsContent(
    uiState: SettingsUiState,
    onToggleAutoFetchUrl: () -> Unit,
    onToggleAskDownloadFolder: () -> Unit,
    onToggleAutoRemoveCompleted: () -> Unit,
    onToggleAutoRetryFailed: () -> Unit,
    onShowFolderPickerDialog: () -> Unit,
    onHideFolderPickerDialog: () -> Unit,
    onConfirmFolderPicker: (String) -> Unit,
    onShowParallelDownloadDialog: () -> Unit,
    onHideParallelDownloadDialog: () -> Unit,
    onConfirmParallelDownload: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onBrowseFolder: () -> Unit = {}
) {
    // Show Folder Picker Dialog
    if (uiState.showFolderPickerDialog) {
        FolderPickerDialog(
            currentFolder = uiState.defaultDownloadFolder,
            onDismiss = onHideFolderPickerDialog,
            onConfirm = onConfirmFolderPicker,
            onBrowse = onBrowseFolder
        )
    }

    // Show Parallel Download Dialog
    if (uiState.showParallelDownloadDialog) {
        ParallelDownloadDialog(
            currentCount = uiState.parallelFileDownload,
            onDismiss = onHideParallelDownloadDialog,
            onConfirm = onConfirmParallelDownload
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Default download folder
        SettingItem(
            title = "Default download folder",
            subtitle = uiState.defaultDownloadFolder,
            hasChevron = true,
            onClick = onShowFolderPickerDialog
        )
        SettingsDivider()

        // 2. Ask download folder
        SettingItem(
            title = "Ask download folder",
            hasSwitch = true,
            isEnabled = uiState.askDownloadFolder,
            onToggle = onToggleAskDownloadFolder
        )
        SettingsDivider()

        // 3. Auto fetch URL
        SettingItem(
            title = "Auto fetch URL",
            hasSwitch = true,
            isEnabled = uiState.autoFetchUrl,
            onToggle = onToggleAutoFetchUrl
        )
        SettingsDivider()

        // 4. Parallel file download
        SettingItem(
            title = "Parallel file download",
            subtitle = "${uiState.parallelFileDownload} files",
            hasChevron = true,
            onClick = onShowParallelDownloadDialog
        )
        SettingsDivider()

        // 5. Automatically remove completed download from list
        SettingItem(
            title = "Automatically remove completed download from list",
            hasSwitch = true,
            isEnabled = uiState.autoRemoveCompleted,
            onToggle = onToggleAutoRemoveCompleted
        )
        SettingsDivider()

        // 6. Automatically retry failed download
        SettingItem(
            title = "Automatically retry failed download",
            hasSwitch = true,
            isEnabled = uiState.autoRetryFailed,
            onToggle = onToggleAutoRetryFailed
        )
    }
}

@Composable
private fun DownloadSettingsStandaloneContent(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
) {
    val context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            val folderPath = getReadablePath(uri)
            viewModel.updateDefaultDownloadFolder(folderPath)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        DownloadSettingsContent(
            uiState = uiState,
            onToggleAutoFetchUrl = viewModel::toggleAutoFetchUrl,
            onToggleAskDownloadFolder = viewModel::toggleAskDownloadFolder,
            onToggleAutoRemoveCompleted = viewModel::toggleAutoRemoveCompleted,
            onToggleAutoRetryFailed = viewModel::toggleAutoRetryFailed,
            onShowFolderPickerDialog = viewModel::showFolderPickerDialog,
            onHideFolderPickerDialog = viewModel::hideFolderPickerDialog,
            onConfirmFolderPicker = viewModel::updateDefaultDownloadFolder,
            onBrowseFolder = {
                val initialUri = "content://com.android.externalstorage.documents/document/primary:Download".toUri()
                folderPickerLauncher.launch(initialUri)
            },
            onShowParallelDownloadDialog = viewModel::showParallelDownloadDialog,
            onHideParallelDownloadDialog = viewModel::hideParallelDownloadDialog,
            onConfirmParallelDownload = viewModel::setParallelDownload
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadSettingsScreenPreview() {
    FileDownloadManagerTheme {
        DownloadSettingsContent(
            uiState = SettingsUiState(),
            onToggleAutoFetchUrl = {},
            onToggleAskDownloadFolder = {},
            onToggleAutoRemoveCompleted = {},
            onToggleAutoRetryFailed = {},
            onShowFolderPickerDialog = {},
            onHideFolderPickerDialog = {},
            onConfirmFolderPicker = {},
            onShowParallelDownloadDialog = {},
            onHideParallelDownloadDialog = {},
            onConfirmParallelDownload = {},
            onBrowseFolder = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DownloadSettingsScreenDarkPreview() {
    FileDownloadManagerTheme {
        DownloadSettingsContent(
            uiState = SettingsUiState(darkMode = true),
            onToggleAutoFetchUrl = {},
            onToggleAskDownloadFolder = {},
            onToggleAutoRemoveCompleted = {},
            onToggleAutoRetryFailed = {},
            onShowFolderPickerDialog = {},
            onHideFolderPickerDialog = {},
            onConfirmFolderPicker = {},
            onShowParallelDownloadDialog = {},
            onHideParallelDownloadDialog = {},
            onConfirmParallelDownload = {},
            onBrowseFolder = {}
        )
    }
}
