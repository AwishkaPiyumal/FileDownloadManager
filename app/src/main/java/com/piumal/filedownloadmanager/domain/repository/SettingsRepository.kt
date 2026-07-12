package com.piumal.filedownloadmanager.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings access.
 * Follows Clean Architecture - domain layer interface
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

    /** Observe whether download completion notifications are enabled */
    fun observeNotifyDownloadCompletion(): Flow<Boolean>

    /** Observe whether download failure notifications are enabled */
    fun observeNotifyDownloadFailure(): Flow<Boolean>

    /** Observe whether vibrate is enabled for notifications */
    fun observeVibrate(): Flow<Boolean>

    /** Observe whether light (flash) is enabled for notifications */
    fun observeLight(): Flow<Boolean>

    /** Observe the completion notification ringtone URI */
    fun observeCompletionRingtone(): Flow<String>

    /** Observe the failure notification ringtone URI */
    fun observeFailureRingtone(): Flow<String>

    /** Get current parallel download limit */
    fun getParallelDownloadLimit(): Int

    /** Observe parallel download limit changes */
    fun observeParallelDownloadLimit(): Flow<Int>

    /** Check if auto fetch URL from clipboard is enabled */
    fun isAutoFetchUrlEnabled(): Boolean

    /** Check if ask download folder is enabled */
    fun isAskDownloadFolderEnabled(): Boolean
}
