package com.piumal.filedownloadmanager.ui.settings

/**
 * SettingsUiState
 *
 * Data class representing the UI state for the Settings screen.
 * Contains all toggle settings that can be modified by the user.
 */
data class SettingsUiState(
    val useMobileData: Boolean = false,
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)

