package com.piumal.filedownloadmanager.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case to check if Auto Fetch URL setting is enabled
 *
 * Purpose:
 * - Checks if the "Auto fetch URL" option is enabled in Download Settings
 * - When enabled, the app will automatically paste clipboard URL into AddDownloadDialog
 * - When disabled, the URL field will remain empty
 *
 * Follows Clean Architecture - encapsulates settings access logic
 */
@Singleton
class IsAutoFetchUrlEnabledUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_AUTO_FETCH_URL = "auto_fetch_url"
        private const val DEFAULT_VALUE = true // Auto fetch is enabled by default
    }

    /**
     * Check if auto fetch URL is enabled
     * @return true if enabled, false otherwise
     */
    operator fun invoke(): Boolean {
        return try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences.getBoolean(KEY_AUTO_FETCH_URL, DEFAULT_VALUE)
        } catch (e: Exception) {
            DEFAULT_VALUE
        }
    }
}

