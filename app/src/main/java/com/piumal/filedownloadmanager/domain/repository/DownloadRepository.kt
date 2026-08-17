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
     * Physically delete the file from storage first, then remove the database record.
     * This is used by the "Delete file" action.
     */
    suspend fun deleteDownloadFile(id: String): Result<Unit>

    /**
     * Copy the file to a new destination.
     * This is used by the "Copy to..." action.
     */
    suspend fun copyDownload(item: DownloadItem, destinationFolderPath: String): Result<Unit>

    /**
     * Open a completed download using a FileProvider-backed Intent.
     */
    suspend fun openDownload(id: String): Result<Unit>

    /**
     * Share a completed download using a FileProvider-backed Intent.
     */
    suspend fun shareDownload(id: String, chooserTitle: String? = null): Result<Unit>

    /**
     * Show the download in a file manager / folder view.
     */
    suspend fun showInFolder(id: String): Result<Unit>

    /**
     * Delete download
     */
    suspend fun deleteDownload(id: String)

    /**
     * Start download process
     */
    suspend fun startDownload(download: DownloadItem)

    /**
     * Schedule download to start at specific time
     * @param download The download item to schedule
     * @param scheduleTime Timestamp when download should start
     */
    suspend fun scheduleDownload(download: DownloadItem, scheduleTime: Long)

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

