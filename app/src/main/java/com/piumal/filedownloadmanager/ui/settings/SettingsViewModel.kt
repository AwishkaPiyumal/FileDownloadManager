package com.piumal.filedownloadmanager.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * SettingsViewModel
 *
 * ViewModel for the Settings screen following MVVM architecture.
 * Manages settings state and persists user preferences to SharedPreferences.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_NAME = "settings_prefs"
        // General
        private const val KEY_USE_MOBILE_NETWORK = "use_mobile_network"
        private const val KEY_DARK_MODE = "dark_mode"
        // Download Settings
        private const val KEY_DEFAULT_DOWNLOAD_FOLDER = "default_download_folder"
        private const val KEY_AUTO_FETCH_URL = "auto_fetch_url"
        private const val KEY_ASK_DOWNLOAD_FOLDER = "ask_download_folder"
        private const val KEY_PARALLEL_DOWNLOAD = "parallel_download"
        private const val KEY_AUTO_REMOVE_COMPLETED = "auto_remove_completed"
        private const val KEY_AUTO_RETRY_FAILED = "auto_retry_failed"
        // Notification
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_NOTIFY_COMPLETION = "notify_download_completion"
        private const val KEY_NOTIFY_FAILURE = "notify_download_failure"
        private const val KEY_COMPLETION_RINGTONE = "completion_ringtone"
        private const val KEY_FAILURE_RINGTONE = "failure_ringtone"
        private const val KEY_VIBRATE = "notification_vibrate"
        private const val KEY_LIGHT = "notification_light"
        // Advanced
        private const val KEY_MAX_RETRY_COUNT = "max_retry_count"
    }

    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    // General
                    useMobileNetwork = sharedPreferences.getBoolean(KEY_USE_MOBILE_NETWORK, false),
                    darkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false),
                    // Download Settings
                    defaultDownloadFolder = sharedPreferences.getString(KEY_DEFAULT_DOWNLOAD_FOLDER, "Download/FileDownloadManager") ?: "Download/FileDownloadManager",
                    autoFetchUrl = sharedPreferences.getBoolean(KEY_AUTO_FETCH_URL, true),
                    askDownloadFolder = sharedPreferences.getBoolean(KEY_ASK_DOWNLOAD_FOLDER, false),
                    parallelFileDownload = sharedPreferences.getInt(KEY_PARALLEL_DOWNLOAD, 3),
                    autoRemoveCompleted = sharedPreferences.getBoolean(KEY_AUTO_REMOVE_COMPLETED, false),
                    autoRetryFailed = sharedPreferences.getBoolean(KEY_AUTO_RETRY_FAILED, true),
                    // Notification
                    notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true),
                    notifyDownloadCompletion = sharedPreferences.getBoolean(KEY_NOTIFY_COMPLETION, true),
                    notifyDownloadFailure = sharedPreferences.getBoolean(KEY_NOTIFY_FAILURE, true),
                    completionRingtone = sharedPreferences.getString(KEY_COMPLETION_RINGTONE, "Default") ?: "Default",
                    failureRingtone = sharedPreferences.getString(KEY_FAILURE_RINGTONE, "Default") ?: "Default",
                    vibrateEnabled = sharedPreferences.getBoolean(KEY_VIBRATE, true),
                    lightEnabled = sharedPreferences.getBoolean(KEY_LIGHT, true),
                    // Advanced
                    maxRetryCount = sharedPreferences.getInt(KEY_MAX_RETRY_COUNT, 3)
                )
            }
        }
    }

    // Section toggle functions
    fun toggleDownloadSettingsExpanded() {
        _uiState.update { it.copy(isDownloadSettingsExpanded = !it.isDownloadSettingsExpanded) }
    }

    fun toggleNotificationExpanded() {
        _uiState.update { it.copy(isNotificationExpanded = !it.isNotificationExpanded) }
    }

    fun toggleAdvancedSettingsExpanded() {
        _uiState.update { it.copy(isAdvancedSettingsExpanded = !it.isAdvancedSettingsExpanded) }
    }

    // Dialog visibility functions
    fun showFolderPickerDialog() {
        _uiState.update { it.copy(showFolderPickerDialog = true) }
    }

    fun hideFolderPickerDialog() {
        _uiState.update { it.copy(showFolderPickerDialog = false) }
    }

    fun showParallelDownloadDialog() {
        _uiState.update { it.copy(showParallelDownloadDialog = true) }
    }

    fun hideParallelDownloadDialog() {
        _uiState.update { it.copy(showParallelDownloadDialog = false) }
    }

    // Update default download folder
    fun updateDefaultDownloadFolder(folder: String) {
        _uiState.update { it.copy(defaultDownloadFolder = folder, showFolderPickerDialog = false) }
        saveString(KEY_DEFAULT_DOWNLOAD_FOLDER, folder)
    }

    // General settings
    fun toggleMobileNetwork() {
        val newValue = !_uiState.value.useMobileNetwork
        _uiState.update { it.copy(useMobileNetwork = newValue) }
        saveBoolean(KEY_USE_MOBILE_NETWORK, newValue)
    }

    fun toggleDarkMode() {
        val newValue = !_uiState.value.darkMode
        _uiState.update { it.copy(darkMode = newValue) }
        saveBoolean(KEY_DARK_MODE, newValue)
    }

    // Download settings

    fun toggleAutoFetchUrl() {
        val newValue = !_uiState.value.autoFetchUrl
        _uiState.update { it.copy(autoFetchUrl = newValue) }
        saveBoolean(KEY_AUTO_FETCH_URL, newValue)
    }

    fun toggleAskDownloadFolder() {
        val newValue = !_uiState.value.askDownloadFolder
        _uiState.update { it.copy(askDownloadFolder = newValue) }
        saveBoolean(KEY_ASK_DOWNLOAD_FOLDER, newValue)
    }


    fun toggleAutoRemoveCompleted() {
        val newValue = !_uiState.value.autoRemoveCompleted
        _uiState.update { it.copy(autoRemoveCompleted = newValue) }
        saveBoolean(KEY_AUTO_REMOVE_COMPLETED, newValue)
    }

    fun toggleAutoRetryFailed() {
        val newValue = !_uiState.value.autoRetryFailed
        _uiState.update { it.copy(autoRetryFailed = newValue) }
        saveBoolean(KEY_AUTO_RETRY_FAILED, newValue)
    }

    fun setParallelDownload(count: Int) {
        _uiState.update { it.copy(parallelFileDownload = count, showParallelDownloadDialog = false) }
        saveInt(KEY_PARALLEL_DOWNLOAD, count)
    }

    // Notification settings
    fun toggleNotifications() {
        val newValue = !_uiState.value.notificationsEnabled
        _uiState.update { it.copy(notificationsEnabled = newValue) }
        saveBoolean(KEY_NOTIFICATIONS, newValue)
    }

    fun toggleNotifyDownloadCompletion() {
        val newValue = !_uiState.value.notifyDownloadCompletion
        _uiState.update { it.copy(notifyDownloadCompletion = newValue) }
        saveBoolean(KEY_NOTIFY_COMPLETION, newValue)
    }

    fun toggleNotifyDownloadFailure() {
        val newValue = !_uiState.value.notifyDownloadFailure
        _uiState.update { it.copy(notifyDownloadFailure = newValue) }
        saveBoolean(KEY_NOTIFY_FAILURE, newValue)
    }

    fun setCompletionRingtone(name: String) {
        _uiState.update { it.copy(completionRingtone = name) }
        saveString(KEY_COMPLETION_RINGTONE, name)
    }

    fun setFailureRingtone(name: String) {
        _uiState.update { it.copy(failureRingtone = name) }
        saveString(KEY_FAILURE_RINGTONE, name)
    }

    fun toggleVibrate() {
        val newValue = !_uiState.value.vibrateEnabled
        _uiState.update { it.copy(vibrateEnabled = newValue) }
        saveBoolean(KEY_VIBRATE, newValue)
    }

    fun toggleLight() {
        val newValue = !_uiState.value.lightEnabled
        _uiState.update { it.copy(lightEnabled = newValue) }
        saveBoolean(KEY_LIGHT, newValue)
    }

    // Advanced settings
    fun setMaxRetryCount(count: Int) {
        _uiState.update { it.copy(maxRetryCount = count) }
        saveInt(KEY_MAX_RETRY_COUNT, count)
    }

    private fun saveBoolean(key: String, value: Boolean) {
        viewModelScope.launch {
            sharedPreferences.edit().putBoolean(key, value).apply()
        }
    }

    private fun saveInt(key: String, value: Int) {
        viewModelScope.launch {
            sharedPreferences.edit().putInt(key, value).apply()
        }
    }

    private fun saveString(key: String, value: String) {
        viewModelScope.launch {
            sharedPreferences.edit().putString(key, value).apply()
        }
    }
}
