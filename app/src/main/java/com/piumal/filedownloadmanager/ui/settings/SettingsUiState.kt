package com.piumal.filedownloadmanager.ui.settings

import com.piumal.filedownloadmanager.domain.util.DownloadStoragePaths

/**
 * SettingsUiState
 *
 * Data class representing the UI state for the Settings screen.
 * Contains all toggle settings and section expanded states.
 */
data class SettingsUiState(
    // Section expanded states (General is always expanded)
    val isDownloadSettingsExpanded: Boolean = true,
    val isNotificationExpanded: Boolean = true,
    val isAdvancedSettingsExpanded: Boolean = false,

    // Dialog visibility states
    val showFolderPickerDialog: Boolean = false,
    val showParallelDownloadDialog: Boolean = false,

    // General settings
    val useMobileNetwork: Boolean = false,
    val darkMode: Boolean = false,

    // Download settings
    val defaultDownloadFolder: String = DownloadStoragePaths.DEFAULT_UI_FOLDER_LABEL,
    val autoFetchUrl: Boolean = true,
    val askDownloadFolder: Boolean = false,
    val parallelFileDownload: Int = 3,
    val autoRemoveCompleted: Boolean = false,
    val autoRetryFailed: Boolean = true,

    // Notification settings
    val notificationsEnabled: Boolean = true,
    val notifyDownloadCompletion: Boolean = true,
    val notifyDownloadFailure: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val lightEnabled: Boolean = true,

    // Advanced settings (placeholders for future)
    val maxRetryCount: Int = 3
)
