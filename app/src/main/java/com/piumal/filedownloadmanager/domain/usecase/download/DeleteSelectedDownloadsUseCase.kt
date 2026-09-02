package com.piumal.filedownloadmanager.domain.usecase.download

import android.content.Context
import android.os.Environment
import android.util.Log
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DeleteSelectedDownloadsUseCase
 *
 * Use case for deleting selected downloads from list AND internal storage
 * Follows Clean Architecture - Single Responsibility Principle
 *
 * This use case:
 * 1. Receives list of selected download IDs
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
@Singleton
class DeleteSelectedDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "DeleteSelectedDownloadsUseCase"
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
     * Execute the delete selected downloads operation
     *
     * @param selectedIds Set of download IDs to delete
     * @return Result containing the number of downloads deleted, or error
     */
    suspend operator fun invoke(selectedIds: Set<String>): Result<Int> {
        return try {
            Log.d(TAG, "Starting bulk delete for ${selectedIds.size} items")

            if (selectedIds.isEmpty()) {
                return Result.success(0)
            }

            // Get the download directory
            val downloadDir = getDownloadDirectory()
            var deletedCount = 0

            selectedIds.forEach { downloadId ->
                try {
                    // Get download item from repository
                    val download = downloadRepository.getDownloadById(downloadId)

                    if (download != null) {
                        Log.d(TAG, "Deleting download ID: ${download.id}")

                        // First, cancel if it's still downloading
                        if (download.status == DownloadStatus.DOWNLOADING ||
                            download.status == DownloadStatus.QUEUED) {
                            try {
                                downloadRepository.cancelDownload(download.id)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to cancel active download: ${e.message}")
                            }
                        }

                        // Try multiple locations to delete the file
                        var fileDeleted = false

                        // 1. Try the stored filePath first
                        val storedFile = File(download.filePath)
                        if (storedFile.exists()) {
                            fileDeleted = storedFile.delete()
                        }

                        // 2. Try the download directory with fileName
                        if (!fileDeleted) {
                            val actualFile = File(downloadDir, download.fileName)
                            if (actualFile.exists()) {
                                fileDeleted = actualFile.delete()
                            }
                        }

                        // 3. Try app-specific storage (for older downloads before fix)
                        if (!fileDeleted) {
                            val appDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            if (appDownloadsDir != null) {
                                val appFile = File(
                                    File(appDownloadsDir, "FileDownloadManager"),
                                    download.fileName
                                )
                                if (appFile.exists()) {
                                    fileDeleted = appFile.delete()
                                }
                            }
                        }

                        if (!fileDeleted) {
                            Log.d(TAG, "No matching file found for selected download")
                        }

                        // Also try to delete any partial/temp files in all locations
                        listOf(
                            File("${download.filePath}.tmp"),
                            File(downloadDir, "${download.fileName}.tmp")
                        ).forEach { tempFile ->
                            if (tempFile.exists()) {
                                tempFile.delete()
                            }
                        }

                        // Remove from database
                        downloadRepository.deleteDownload(download.id)
                        deletedCount++
                        Log.d(TAG, "Deleted download record: ${download.id}")

                    } else {
                            Log.w(TAG, "Download not found for one selected item")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete a selected download: ${e.message}")
                    // Continue with other downloads even if one fails
                }
            }

            Log.d(TAG, "Bulk delete completed: $deletedCount deleted")
            Result.success(deletedCount)

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting selected downloads: ${e.message}", e)
            Result.failure(e)
        }
    }
}