package com.piumal.filedownloadmanager.domain.usecase.settings

import com.piumal.filedownloadmanager.domain.repository.SettingsRepository
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
 * Follows Clean Architecture - uses SettingsRepository for settings access
 */
@Singleton
class IsAutoFetchUrlEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Check if auto fetch URL is enabled
     * @return true if enabled, false otherwise
     */
    operator fun invoke(): Boolean {
        return settingsRepository.isAutoFetchUrlEnabled()
    }
}