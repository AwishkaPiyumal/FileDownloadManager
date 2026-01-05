package com.piumal.filedownloadmanager.domain.usecase

import android.util.Log
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PauseAllDownloadsUseCase
 *
 * Use case for pausing all currently downloading/active items
 * Follows Clean Architecture - Single Responsibility Principle
 *
 * This use case:
 * 1. Fetches all downloads with DOWNLOADING status
 * 2. Pauses each one using the repository
 * 3. Returns the count of paused downloads
 *
 * @param downloadRepository Repository for download operations
 */
@Singleton
class PauseAllDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    companion object {
        private const val TAG = "PauseAllDownloadsUseCase"
    }

    /**
     * Execute the pause all downloads operation
     *
     * @return Result containing the number of downloads paused, or error
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            Log.d(TAG, "=== Starting Pause All Downloads ===")

            // Get all downloads currently downloading
            val allDownloads = downloadRepository.getAllDownloads().first()

            // Filter for active/downloading items
            val activeDownloads = allDownloads.filter { download ->
                download.status == DownloadStatus.DOWNLOADING ||
                download.status == DownloadStatus.QUEUED
            }

            Log.d(TAG, "Found ${activeDownloads.size} active downloads to pause")

            if (activeDownloads.isEmpty()) {
                Log.d(TAG, "No active downloads to pause")
                return Result.success(0)
            }

            // Pause each active download
            var pausedCount = 0
            activeDownloads.forEach { download ->
                try {
                    Log.d(TAG, "Pausing download: ${download.id} - ${download.fileName}")
                    downloadRepository.pauseDownload(download.id)
                    pausedCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to pause download ${download.id}: ${e.message}")
                    // Continue with other downloads even if one fails
                }
            }

            Log.d(TAG, "=== Pause All Downloads Complete: $pausedCount paused ===")
            Result.success(pausedCount)

        } catch (e: Exception) {
            Log.e(TAG, "Error pausing all downloads: ${e.message}", e)
            Result.failure(e)
        }
    }
}

