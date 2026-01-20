package com.piumal.filedownloadmanager.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings access.
 */
interface SettingsRepository {
    /** Observe the "Automatically remove completed" setting as a Flow<Boolean> */
    fun observeAutoRemoveCompleted(): Flow<Boolean>
}
