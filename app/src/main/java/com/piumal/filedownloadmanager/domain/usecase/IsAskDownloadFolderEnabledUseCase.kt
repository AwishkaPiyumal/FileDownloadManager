package com.piumal.filedownloadmanager.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case to check if Ask Download Folder setting is enabled
 *
 * Purpose:
 * - Checks if the "Ask download folder" option is enabled in Download Settings
 * - When enabled, the app will prompt the user to select a folder for each download
 *
 * Follows Clean Architecture - encapsulates settings access logic
 */
@Singleton
class IsAskDownloadFolderEnabledUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_ASK_DOWNLOAD_FOLDER = "ask_download_folder"
        private const val DEFAULT_VALUE = false
    }

    /**
     * Check if ask download folder is enabled
     * @return true if enabled, false otherwise
     */
    operator fun invoke(): Boolean {
        return try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences.getBoolean(KEY_ASK_DOWNLOAD_FOLDER, DEFAULT_VALUE)
        } catch (_: Exception) {
            DEFAULT_VALUE
        }
    }
}
