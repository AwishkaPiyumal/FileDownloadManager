package com.piumal.filedownloadmanager.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings access.
 */
interface SettingsRepository {
    /** Observe the "Automatically remove completed" setting as a Flow<Boolean> */
    fun observeAutoRemoveCompleted(): Flow<Boolean>

    /** Observe the "Automatically retry failed" setting as a Flow<Boolean> */
    fun observeAutoRetryFailed(): Flow<Boolean>

    /** Observe the set of hidden completed download IDs (those removed from list) */
    fun observeHiddenCompletedIds(): Flow<Set<String>>

    /** Add given IDs to the hidden completed set */
    fun addHiddenCompletedIds(ids: Set<String>)
}
