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
        private const val KEY_USE_MOBILE_DATA = "use_mobile_data"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
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
                    useMobileData = sharedPreferences.getBoolean(KEY_USE_MOBILE_DATA, false),
                    darkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false),
                    notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true)
                )
            }
        }
    }

    fun toggleMobileData() {
        val newValue = !_uiState.value.useMobileData
        _uiState.update { it.copy(useMobileData = newValue) }
        saveBoolean(KEY_USE_MOBILE_DATA, newValue)
    }

    fun toggleDarkMode() {
        val newValue = !_uiState.value.darkMode
        _uiState.update { it.copy(darkMode = newValue) }
        saveBoolean(KEY_DARK_MODE, newValue)
    }

    fun toggleNotifications() {
        val newValue = !_uiState.value.notificationsEnabled
        _uiState.update { it.copy(notificationsEnabled = newValue) }
        saveBoolean(KEY_NOTIFICATIONS, newValue)
    }

    private fun saveBoolean(key: String, value: Boolean) {
        viewModelScope.launch {
            sharedPreferences.edit().putBoolean(key, value).apply()
        }
    }
}

