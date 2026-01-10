package com.piumal.filedownloadmanager.ui.settings

/**
 * SettingsUiState
 *
 * Data class representing the UI state for the Settings screen.
 * Contains all toggle settings and section expanded states.
 */
data class SettingsUiState(
    // Section expanded states (General is always expanded)
    val isDownloadSettingsExpanded: Boolean = false,
    val isNotificationExpanded: Boolean = false,
    val isAdvancedSettingsExpanded: Boolean = false,

    // General settings
    val useMobileData: Boolean = false,
    val darkMode: Boolean = false,

    // Download settings
    val defaultDownloadFolder: String = "Download/FileDownloadManager",
    val fixedDefaultDownloadFolder: Boolean = false,
    val autoFetchUrl: Boolean = true,
    val askDownloadFolder: Boolean = false,
    val parallelFileDownload: Int = 3,
    val downloadWifiOnly: Boolean = false,
    val autoRemoveCompleted: Boolean = false,
    val autoRetryFailed: Boolean = true,

    // Notification settings
    val notificationsEnabled: Boolean = true,

    // Advanced settings (placeholders for future)
    val maxRetryCount: Int = 3
)

