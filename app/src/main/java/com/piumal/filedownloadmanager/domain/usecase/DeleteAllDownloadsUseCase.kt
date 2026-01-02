package com.piumal.filedownloadmanager.domain.usecase

import android.content.Context
import android.os.Environment
import android.util.Log
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

/**
 * DeleteAllDownloadsUseCase
 *
 * Use case for deleting all downloads from list AND internal storage
 * Follows Clean Architecture - Single Responsibility Principle
 *
 * This use case:
 * 1. Fetches all downloads
 * 2. Cancels any active downloads first
 * 3. Deletes the actual files from storage
 * 4. Removes records from database
 * 5. Returns the count of deleted downloads
 *
 * Warning: This permanently deletes files from device storage
 *
 * @param downloadRepository Repository for download operations
 * @param context Application context for file operations
 */
class DeleteAllDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "DeleteAllDownloadsUseCase"
    }

    /**
     * Get the download directory - same logic as DownloadManager
     * Uses public Downloads folder so users can easily find their downloaded files
     */
    private fun getDownloadDirectory(): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        return File(downloadsDir, "FileDownloadManager")
    }

    /**
     * Execute the delete all downloads operation
     *
     * @return Result containing the number of downloads deleted, or error
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            Log.d(TAG, "=== Starting Delete All Downloads ===")

            // Get all downloads
            val allDownloads = downloadRepository.getAllDownloads().first()

            Log.d(TAG, "Found ${allDownloads.size} downloads to delete")

            if (allDownloads.isEmpty()) {
                Log.d(TAG, "No downloads to delete")
                return Result.success(0)
            }

            // Get the download directory
            val downloadDir = getDownloadDirectory()
            Log.d(TAG, "Download directory: ${downloadDir.absolutePath}")

            var deletedCount = 0

            allDownloads.forEach { download ->
                try {
                    Log.d(TAG, "Deleting download: ${download.id} - ${download.fileName}")

                    // First, cancel if it's still downloading
                    if (download.status == DownloadStatus.DOWNLOADING ||
                        download.status == DownloadStatus.QUEUED) {
                        try {
                            downloadRepository.cancelDownload(download.id)
                            Log.d(TAG, "Cancelled active download: ${download.id}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to cancel download ${download.id}: ${e.message}")
                        }
                    }

                    // Try multiple locations to delete the file
                    var fileDeleted = false

                    // 1. Try the stored filePath first
                    val storedFile = File(download.filePath)
                    if (storedFile.exists()) {
                        fileDeleted = storedFile.delete()
                        Log.d(TAG, "Deleted from stored path: ${storedFile.absolutePath} - Result: $fileDeleted")
                    }

                    // 2. Try the download directory with fileName
                    if (!fileDeleted) {
                        val actualFile = File(downloadDir, download.fileName)
                        if (actualFile.exists()) {
                            fileDeleted = actualFile.delete()
                            Log.d(TAG, "Deleted from download dir: ${actualFile.absolutePath} - Result: $fileDeleted")
                        }
                    }

                    // 3. Try app-specific storage (for older downloads before fix)
                    if (!fileDeleted) {
                        val appDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        if (appDownloadsDir != null) {
                            val appFile = File(File(appDownloadsDir, "FileDownloadManager"), download.fileName)
                            if (appFile.exists()) {
                                fileDeleted = appFile.delete()
                                Log.d(TAG, "Deleted from app-specific storage: ${appFile.absolutePath} - Result: $fileDeleted")
                            }
                        }
                    }

                    if (!fileDeleted) {
                        Log.d(TAG, "File not found in any location for: ${download.fileName}")
                    }

                    // Also try to delete any partial/temp files in all locations
                    listOf(
                        File("${download.filePath}.tmp"),
                        File(downloadDir, "${download.fileName}.tmp"),
                        File(File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "FileDownloadManager"), "${download.fileName}.tmp")
                    ).forEach { tempFile ->
                        if (tempFile.exists()) {
                            tempFile.delete()
                            Log.d(TAG, "Deleted temp file: ${tempFile.absolutePath}")
                        }
                    }

                    // Remove from database
                    downloadRepository.deleteDownload(download.id)
                    deletedCount++

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete download ${download.id}: ${e.message}")
                    // Continue with other downloads even if one fails
                }
            }

            Log.d(TAG, "=== Delete All Downloads Complete: $deletedCount deleted ===")
            Result.success(deletedCount)

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all downloads: ${e.message}", e)
            Result.failure(e)
        }
    }
}

