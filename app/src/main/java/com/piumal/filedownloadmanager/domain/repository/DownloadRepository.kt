package com.piumal.filedownloadmanager.domain.repository

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

/**
 * Download Repository Interface
 *
 * Defines contract for download operations
 * Follows Clean Architecture - Domain layer doesn't depend on implementation
 *
 * @author File Download Manager Team
 */
interface DownloadRepository {

    /**
     * Get all downloads as Flow (reactive)
     */
    fun getAllDownloads(): Flow<List<DownloadItem>>

    /**
     * Get downloads by status
     */
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>>

    /**
     * Get single download by ID
     */
    suspend fun getDownloadById(id: String): DownloadItem?

    /**
     * Insert new download
     */
    suspend fun insertDownload(download: DownloadItem)

    /**
     * Update download progress
     */
    suspend fun updateDownload(download: DownloadItem)

    /**
     * Delete download
     */
    suspend fun deleteDownload(id: String)

    /**
     * Start download process
     */
    suspend fun startDownload(download: DownloadItem)

    /**
     * Pause download
     */
    suspend fun pauseDownload(id: String)

    /**
     * Resume download
     */
    suspend fun resumeDownload(id: String)

    /**
     * Cancel download
     */
    suspend fun cancelDownload(id: String)

    /**
     * Retry failed download
     */
    suspend fun retryDownload(id: String)
}

