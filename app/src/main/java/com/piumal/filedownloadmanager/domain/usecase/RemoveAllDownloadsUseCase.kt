package com.piumal.filedownloadmanager.domain.usecase

import android.util.Log
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * RemoveAllDownloadsUseCase
 *
 * Use case for removing all downloads from the list
 * Follows Clean Architecture - Single Responsibility Principle
 *
 * This use case:
 * 1. Fetches all downloads
 * 2. Removes each one from the database (keeps files on storage)
 * 3. Returns the count of removed downloads
 *
 * Note: This only removes records from database, downloaded files remain on device
 *
 * @param downloadRepository Repository for download operations
 */
class RemoveAllDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    companion object {
        private const val TAG = "RemoveAllDownloadsUseCase"
    }

    /**
     * Execute the remove all downloads operation
     *
     * @return Result containing the number of downloads removed, or error
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            Log.d(TAG, "=== Starting Remove All Downloads ===")

            // Get all downloads
            val allDownloads = downloadRepository.getAllDownloads().first()

            Log.d(TAG, "Found ${allDownloads.size} downloads to remove")

            if (allDownloads.isEmpty()) {
                Log.d(TAG, "No downloads to remove")
                return Result.success(0)
            }

            // Remove each download from database (keeps files)
            var removedCount = 0
            allDownloads.forEach { download ->
                try {
                    Log.d(TAG, "Removing download: ${download.id} - ${download.fileName}")
                    downloadRepository.deleteDownload(download.id)
                    removedCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to remove download ${download.id}: ${e.message}")
                    // Continue with other downloads even if one fails
                }
            }

            Log.d(TAG, "=== Remove All Downloads Complete: $removedCount removed ===")
            Result.success(removedCount)

        } catch (e: Exception) {
            Log.e(TAG, "Error removing all downloads: ${e.message}", e)
            Result.failure(e)
        }
    }
}

