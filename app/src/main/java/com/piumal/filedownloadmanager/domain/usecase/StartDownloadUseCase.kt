package com.piumal.filedownloadmanager.domain.usecase

import android.content.Context
import android.util.Log
import com.piumal.filedownloadmanager.data.download.DownloadService
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import com.piumal.filedownloadmanager.domain.util.ContentValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.io.File
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
 * Background Download Support:
 * - Uses DownloadService (Foreground Service)
 * - Downloads continue when app is closed
 * - Survives device reboot (via BootReceiver)
 * - Auto-resumes on network restore (via NetworkChangeReceiver)
 *
 * @param context Application context
 * @param repository Download repository interface
 * @param scheduledDownloadManager Scheduled download manager
 */
class StartDownloadUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
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
     * @param forceNewDownload If true, creates new download with renamed file even if exists
     * @return Flow of Result with DownloadItem or error
     */
    operator fun invoke(
        url: String,
        fileName: String?,
        destinationPath: String,
        scheduleTime: Long? = null,
        forceNewDownload: Boolean = false
    ): Flow<Result<DownloadItem>> = flow {
        try {
            Log.d("StartDownloadUseCase", "=== Starting download process ===")
            Log.d("StartDownloadUseCase", "URL: $url")
            Log.d("StartDownloadUseCase", "FileName: $fileName")
            Log.d("StartDownloadUseCase", "DestinationPath: $destinationPath")
            Log.d("StartDownloadUseCase", "ForceNewDownload: $forceNewDownload")

            // Step 1: Validate URL for Google Policy compliance
            val urlValidation = ContentValidator.validateDownloadUrl(url)
            Log.d("StartDownloadUseCase", "URL Validation: ${urlValidation.isValid} - ${urlValidation.message}")
            if (!urlValidation.isValid) {
                emit(Result.failure(IllegalArgumentException(urlValidation.message)))
                return@flow
            }

            // Step 2: Extract or use provided file name
            val actualFileName = fileName?.takeIf { it.isNotBlank() }
                ?: ContentValidator.extractFileName(url)
                ?: "download_${System.currentTimeMillis()}"
            Log.d("StartDownloadUseCase", "Actual file name: $actualFileName")

            // Step 3: Validate file name
            val fileNameValidation = ContentValidator.validateFileName(actualFileName)
            Log.d("StartDownloadUseCase", "File name validation: ${fileNameValidation.isValid}")
            if (!fileNameValidation.isValid) {
                emit(Result.failure(IllegalArgumentException(fileNameValidation.message)))
                return@flow
            }

            // Step 4: Validate scheduled time (must be in future if provided)
            if (scheduleTime != null && scheduleTime <= System.currentTimeMillis()) {
                emit(Result.failure(IllegalArgumentException("Scheduled time must be in the future")))
                return@flow
            }

            // Step 4.5: Check if download already exists with same URL and file name
            // If exists and not completed, resume it instead of creating new one
            Log.d("StartDownloadUseCase", "Checking for existing downloads...")
            val existingDownloads = repository.getAllDownloads().first()
            Log.d("StartDownloadUseCase", "Found ${existingDownloads.size} existing downloads")

            // Determine the final file name (with auto-rename if needed)
            val finalFileName = if (forceNewDownload) {
                // User chose to continue with duplicate - generate unique name
                generateUniqueFileName(actualFileName, destinationPath, existingDownloads)
            } else {
                actualFileName
            }

            Log.d("StartDownloadUseCase", "Final file name: $finalFileName")

            // Check if file already downloaded and exists in database (only if not forcing new download)
            if (!forceNewDownload) {
                val existingCompletedDownload = existingDownloads.firstOrNull {
                    it.fileName == finalFileName &&
                    it.status == DownloadStatus.COMPLETED
                }

                if (existingCompletedDownload != null) {
                    // File already exists, need user confirmation
                    Log.d("StartDownloadUseCase", "File already exists: $finalFileName")
                    emit(Result.failure(FileAlreadyExistsException(finalFileName)))
                    return@flow
                }
            }

            // Check if same download is in progress or paused (only if not forcing new download)
            if (!forceNewDownload) {
                val existingActiveDownload = existingDownloads.firstOrNull {
                    it.url == url &&
                    it.fileName == finalFileName &&
                    it.status != DownloadStatus.COMPLETED
                }

                if (existingActiveDownload != null) {
                    // Resume existing download instead of creating new one
                    Log.d("StartDownloadUseCase", "Found existing download, resuming: ${existingActiveDownload.id}")
                    DownloadService.startDownload(context, existingActiveDownload.id)
                    emit(Result.success(existingActiveDownload))
                    return@flow
                }
            }

            // Step 5: Create download item
            val downloadId = System.currentTimeMillis() // Use timestamp as Long ID
            val downloadStatus = if (scheduleTime != null) {
                DownloadStatus.QUEUED // Scheduled downloads start as QUEUED
            } else {
                DownloadStatus.QUEUED // Immediate downloads also start as QUEUED (will change to DOWNLOADING in service)
            }

            val downloadItem = DownloadItem(
                id = downloadId.toString(),
                fileName = finalFileName,
                downloadedSize = 0L,
                totalSize = 0L, // Will be updated when download starts
                status = downloadStatus,
                url = url,
                filePath = "$destinationPath/$finalFileName",
                createdAt = System.currentTimeMillis(),
                scheduleTime = scheduleTime  // Store scheduled time
            )

            Log.d("StartDownloadUseCase", "Created download item: ID=${downloadItem.id}, Status=${downloadItem.status}")

            // Step 6: Save to repository
            Log.d("StartDownloadUseCase", "Inserting download into database...")
            repository.insertDownload(downloadItem)
            Log.d("StartDownloadUseCase", "Download inserted successfully")

            // Step 7: Start the download (immediate or scheduled)
            if (scheduleTime != null) {
                Log.d("StartDownloadUseCase", "Scheduling download for later")
                // Schedule the download using ScheduledDownloadManager
                repository.scheduleDownload(downloadItem, scheduleTime)
                scheduledDownloadManager.scheduleDownload(downloadItem, scheduleTime)
            } else {
                // Start immediately using DownloadService (background service)
                // This allows download to continue even if app is closed
                Log.d("StartDownloadUseCase", "Starting download service immediately with ID: ${downloadItem.id}")
                DownloadService.startDownload(context, downloadItem.id)
                Log.d("StartDownloadUseCase", "Download service started")
            }

            // Step 8: Emit success
            Log.d("StartDownloadUseCase", "Emitting success result")
            emit(Result.success(downloadItem))
            Log.d("StartDownloadUseCase", "=== Download process completed successfully ===")

        } catch (e: Exception) {
            Log.e("StartDownloadUseCase", "Error starting download", e)
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

    /**
     * Generate unique filename if file already exists
     * Example: download.pdf -> download(1).pdf -> download(2).pdf
     */
    private suspend fun generateUniqueFileName(
        baseName: String,
        destinationPath: String,
        existingDownloads: List<DownloadItem>
    ): String {
        val file = File(baseName)
        val nameWithoutExt = file.nameWithoutExtension
        val extension = file.extension

        var counter = 1
        var newFileName = baseName

        // Check both file system and database
        while (existingDownloads.any { it.fileName == newFileName } ||
               File(destinationPath, newFileName).exists()) {
            newFileName = if (extension.isNotEmpty()) {
                "$nameWithoutExt($counter).$extension"
            } else {
                "$nameWithoutExt($counter)"
            }
            counter++
        }

        return newFileName
    }
}

/**
 * Custom exception for file already exists scenario
 */
class FileAlreadyExistsException(val fileName: String) :
    Exception("File '$fileName' already exists")
