package com.piumal.filedownloadmanager.domain.usecase

import android.util.Log
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * ResumeAllDownloadsUseCase
 *
 * Use case for resuming all paused downloads
 * Follows Clean Architecture - Single Responsibility Principle
 *
 * This use case:
 * 1. Fetches all downloads with PAUSED status
 * 2. Resumes each one using the repository
 * 3. Returns the count of resumed downloads
 *
 * @param downloadRepository Repository for download operations
 */
class ResumeAllDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    companion object {
        private const val TAG = "ResumeAllDownloadsUseCase"
    }

    /**
     * Execute the resume all downloads operation
     *
     * @return Result containing the number of downloads resumed, or error
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            Log.d(TAG, "=== Starting Resume All Downloads ===")

            // Get all downloads
            val allDownloads = downloadRepository.getAllDownloads().first()

            // Filter for paused items
            val pausedDownloads = allDownloads.filter { download ->
                download.status == DownloadStatus.PAUSED
            }

            Log.d(TAG, "Found ${pausedDownloads.size} paused downloads to resume")

            if (pausedDownloads.isEmpty()) {
                Log.d(TAG, "No paused downloads to resume")
                return Result.success(0)
            }

            // Resume each paused download
            var resumedCount = 0
            pausedDownloads.forEach { download ->
                try {
                    Log.d(TAG, "Resuming download: ${download.id} - ${download.fileName}")
                    downloadRepository.resumeDownload(download.id)
                    resumedCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to resume download ${download.id}: ${e.message}")
                    // Continue with other downloads even if one fails
                }
            }

            Log.d(TAG, "=== Resume All Downloads Complete: $resumedCount resumed ===")
            Result.success(resumedCount)

        } catch (e: Exception) {
            Log.e(TAG, "Error resuming all downloads: ${e.message}", e)
            Result.failure(e)
        }
    }
}

