package com.piumal.filedownloadmanager.domain.usecase

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import com.piumal.filedownloadmanager.domain.util.ContentValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

/**
 * Use Case for starting a new download
 *
 * Follows Clean Architecture principles:
 * - Business logic in domain layer
 * - Independent of UI and data sources
 * - Single responsibility
 *
 * Google Policy Compliance:
 * - Validates content before download
 * - Blocks illegal content
 * - Ensures user responsibility
 *
 * @param repository Download repository interface
 * @param scheduledDownloadManager Scheduled download manager
 */
class StartDownloadUseCase @Inject constructor(
    private val repository: DownloadRepository,
    private val scheduledDownloadManager: ScheduledDownloadManager
) {
    /**
     * Request to start a download
     *
     * @param url Download URL
     * @param fileName Custom file name (optional)
     * @param destinationPath Destination folder path
     * @param scheduleTime Scheduled time to start download (null for immediate)
     * @return Flow of Result with DownloadItem or error
     */
    operator fun invoke(
        url: String,
        fileName: String?,
        destinationPath: String,
        scheduleTime: Long? = null
    ): Flow<Result<DownloadItem>> = flow {
        try {
            // Step 1: Validate URL for Google Policy compliance
            val urlValidation = ContentValidator.validateDownloadUrl(url)
            if (!urlValidation.isValid) {
                emit(Result.failure(IllegalArgumentException(urlValidation.message)))
                return@flow
            }

            // Step 2: Extract or use provided file name
            val actualFileName = fileName?.takeIf { it.isNotBlank() }
                ?: ContentValidator.extractFileName(url)
                ?: "download_${System.currentTimeMillis()}"

            // Step 3: Validate file name
            val fileNameValidation = ContentValidator.validateFileName(actualFileName)
            if (!fileNameValidation.isValid) {
                emit(Result.failure(IllegalArgumentException(fileNameValidation.message)))
                return@flow
            }

            // Step 4: Validate scheduled time (must be in future if provided)
            if (scheduleTime != null && scheduleTime <= System.currentTimeMillis()) {
                emit(Result.failure(IllegalArgumentException("Scheduled time must be in the future")))
                return@flow
            }

            // Step 5: Create download item
            val downloadId = UUID.randomUUID().toString()
            val downloadStatus = if (scheduleTime != null) {
                DownloadStatus.QUEUED // Scheduled downloads start as QUEUED
            } else {
                DownloadStatus.QUEUED // Immediate downloads also start as QUEUED
            }

            val downloadItem = DownloadItem(
                id = downloadId,
                fileName = actualFileName,
                downloadedSize = 0L,
                totalSize = 0L, // Will be updated when download starts
                status = downloadStatus,
                url = url,
                filePath = "$destinationPath/$actualFileName",
                createdAt = System.currentTimeMillis(),
                scheduleTime = scheduleTime  // Store scheduled time
            )

            // Step 6: Save to repository
            repository.insertDownload(downloadItem)

            // Step 7: Start the download (immediate or scheduled)
            if (scheduleTime != null) {
                // Schedule the download using ScheduledDownloadManager
                repository.scheduleDownload(downloadItem, scheduleTime)
                scheduledDownloadManager.scheduleDownload(downloadItem, scheduleTime)
            } else {
                // Start immediately
                repository.startDownload(downloadItem)
            }

            // Step 8: Emit success
            emit(Result.success(downloadItem))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Validate download request before starting
     * Used to show warnings to user
     */
    fun validateBeforeDownload(url: String): ContentValidator.ValidationResult {
        return ContentValidator.validateDownloadUrl(url)
    }
}

