package com.piumal.filedownloadmanager.domain.usecase

import android.util.Log
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RetryAllDownloadsUseCase
 *
 * Use case for retrying all failed downloads
 * Follows Clean Architecture - Single Responsibility Principle
 *
 * This use case:
 * 1. Fetches all downloads with FAILED status
 * 2. Retries each one using the repository
 * 3. Returns the count of retried downloads
 *
 * @param downloadRepository Repository for download operations
 */
@Singleton
class RetryAllDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    companion object {
        private const val TAG = "RetryAllDownloadsUseCase"
    }

    /**
     * Execute the retry all failed downloads operation
     *
     * @return Result containing the number of downloads retried, or error
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            Log.d(TAG, "=== Starting Retry All Failed Downloads ===")

            // Get all downloads
            val allDownloads = downloadRepository.getAllDownloads().first()

            // Filter for failed items
            val failedDownloads = allDownloads.filter { download ->
                download.status == DownloadStatus.FAILED
            }

            Log.d(TAG, "Found ${failedDownloads.size} failed downloads to retry")

            if (failedDownloads.isEmpty()) {
                Log.d(TAG, "No failed downloads to retry")
                return Result.success(0)
            }

            // Retry each failed download
            var retriedCount = 0
            failedDownloads.forEach { download ->
                try {
                    Log.d(TAG, "Retrying download: ${download.id} - ${download.fileName}")
                    downloadRepository.retryDownload(download.id)
                    retriedCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to retry download ${download.id}: ${e.message}")
                    // Continue with other downloads even if one fails
                }
            }

            Log.d(TAG, "=== Retry All Downloads Complete: $retriedCount retried ===")
            Result.success(retriedCount)

        } catch (e: Exception) {
            Log.e(TAG, "Error retrying all downloads: ${e.message}", e)
            Result.failure(e)
        }
    }
}

