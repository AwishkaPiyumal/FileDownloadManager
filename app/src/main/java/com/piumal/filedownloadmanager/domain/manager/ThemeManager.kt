package com.piumal.filedownloadmanager.domain.manager

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ThemeManager
 *
 * Manages the app's theme settings (dark mode).
 * Provides a reactive way to observe theme changes.
 *
 * Features:
 * - Observes dark mode setting from SharedPreferences
 * - Emits updates via StateFlow for reactive UI updates
 * - Allows overriding system default theme
 *
 * Architecture: Domain layer manager following Clean Architecture
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val DEFAULT_DARK_MODE = false
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // StateFlow to observe dark mode setting changes
    private val _isDarkMode = MutableStateFlow(getDarkModeSetting())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Listener to auto-update when setting changes
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_DARK_MODE) {
            _isDarkMode.value = getDarkModeSetting()
        }
    }

    init {
        // Register listener for preference changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    /**
     * Get current dark mode setting
     * @return true if dark mode is enabled, false otherwise
     */
    fun getDarkModeSetting(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, DEFAULT_DARK_MODE)
    }

    /**
     * Set dark mode setting
     * @param enabled true to enable dark mode, false to disable
     */
    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    /**
     * Toggle dark mode setting
     */
    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        setDarkMode(newValue)
    }
}

