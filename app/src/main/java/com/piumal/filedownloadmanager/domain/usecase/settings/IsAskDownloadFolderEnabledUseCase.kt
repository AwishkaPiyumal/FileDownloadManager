package com.piumal.filedownloadmanager.domain.usecase.settings

import com.piumal.filedownloadmanager.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case to check if Ask Download Folder setting is enabled
 *
 * Purpose:
 * - Checks if the "Ask download folder" option is enabled in Download Settings
 * - When enabled, the app will prompt the user to select a folder for each download
 *
 * Follows Clean Architecture - uses SettingsRepository for settings access
 */
@Singleton
class IsAskDownloadFolderEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Check if ask download folder is enabled
     * @return true if enabled, false otherwise
     */
    operator fun invoke(): Boolean {
        return settingsRepository.isAskDownloadFolderEnabled()
    }
}