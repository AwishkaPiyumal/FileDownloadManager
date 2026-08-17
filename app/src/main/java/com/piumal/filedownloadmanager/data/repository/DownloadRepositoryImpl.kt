package com.piumal.filedownloadmanager.data.repository

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.content.Context
import android.media.MediaScannerConnection
import android.webkit.MimeTypeMap
import com.piumal.filedownloadmanager.data.download.DownloadManager
import com.piumal.filedownloadmanager.data.download.DownloadService
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.data.local.entity.DownloadEntity
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import com.piumal.filedownloadmanager.data.repository.DownloadFileOperations
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of Download Repository
 *
 * Coordinates between:
 * - Room database (persistence)
 * - Download Manager (actual downloads)
 *
 * Follows Clean Architecture - implements domain interface
 *
 * @param downloadDao Room DAO
 * @param downloadManager Download manager
 */
@Singleton
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val downloadManager: DownloadManager
) : DownloadRepository {

    override fun getAllDownloads(): Flow<List<DownloadItem>> {
        return downloadDao.getAllDownloads().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>> {
        return downloadDao.getDownloadsByStatus(status.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getDownloadById(id: String): DownloadItem? {
        return downloadDao.getDownloadById(id)?.toDomainModel()
    }

    override suspend fun insertDownload(download: DownloadItem) {
        val entity = DownloadEntity.fromDomainModel(download)
        downloadDao.insertDownload(entity)
    }

    override suspend fun updateDownload(download: DownloadItem) {
        val entity = DownloadEntity.fromDomainModel(download)
        downloadDao.updateDownload(entity)
    }

    override suspend fun deleteDownloadFile(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val download = requireExistingDownload(id)
            val file = File(download.filePath)
            if (file.exists() && !DownloadFileOperations.deletePhysicalFile(file.absolutePath)) {
                throw IllegalStateException("Failed to physically delete file: ${file.absolutePath}")
            }
            downloadDao.deleteDownload(id)
        }
    }

    override suspend fun copyDownload(item: DownloadItem, destinationFolderPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val sourceFile = java.io.File(item.filePath)
            if (!sourceFile.exists()) throw Exception("Source file not found")
            try {
                if (destinationFolderPath.startsWith("content://")) {
                    // Handle Scoped Storage / SAF URI
                    val uri = android.net.Uri.parse(destinationFolderPath)
                    val documentFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)
                        ?: throw Exception("Invalid destination folder selected")
                    // Create the new file in the selected directory
                    val newFile = documentFile.createFile("*/*", sourceFile.name)
                        ?: throw Exception("Failed to create file in destination")
                    // Copy streams using ContentResolver
                    context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    } ?: throw Exception("Failed to open output stream")
                } else {
                    // Handle Standard File I/O (Fallback)
                    val destDir = java.io.File(destinationFolderPath)
                    if (!destDir.exists()) destDir.mkdirs()
                    val destFile = java.io.File(destDir, sourceFile.name)
                    sourceFile.inputStream().use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            } catch (e: Exception) {
                // Return the exact localized message so we know what went wrong if it fails again
                throw Exception("Copy error: ${e.localizedMessage}")
            }
            Unit
        }
    }


    override suspend fun openDownload(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val download = requireExistingDownload(id)
            val file = File(download.filePath)
            require(file.exists()) { "File does not exist: ${file.absolutePath}" }

            val uri = fileProviderUri(file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeTypeFor(file) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                clipData = ClipData.newRawUri(file.name, uri)
            }

            val chooser = Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }.recoverCatching { throwable ->
            if (throwable is ActivityNotFoundException) {
                throw IllegalStateException("No app found to open this file", throwable)
            }
            throw throwable
        }
    }

    override suspend fun shareDownload(id: String, chooserTitle: String?): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val download = requireExistingDownload(id)
            val file = File(download.filePath)
            require(file.exists()) { "File does not exist: ${file.absolutePath}" }

            val uri = fileProviderUri(file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeTypeFor(file) ?: "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                clipData = ClipData.newRawUri(file.name, uri)
            }

            val chooser = Intent.createChooser(intent, chooserTitle ?: "Share").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }.recoverCatching { throwable ->
            if (throwable is ActivityNotFoundException) {
                throw IllegalStateException("No app found to share this file", throwable)
            }
            throw throwable
        }
    }

    override suspend fun showInFolder(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val intent = Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                throw Exception("Failed to open downloads folder")
            }
        }
    }

    override suspend fun deleteDownload(id: String) {
        downloadDao.deleteDownload(id)
    }

    override suspend fun startDownload(download: DownloadItem) {
        // Update status to downloading
        downloadDao.updateStatus(
            id = download.id,
            status = DownloadStatus.DOWNLOADING.name,
            updatedAt = System.currentTimeMillis()
        )

        // Start download using DownloadService (foreground service with notifications)
        DownloadService.startDownload(context, download.id)
    }

    override suspend fun pauseDownload(id: String) {
        // Send pause action to DownloadService to stop the download job and update status
        // The service handles database update and notification update for consistency
        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_PAUSE_DOWNLOAD
            putExtra(DownloadService.EXTRA_DOWNLOAD_ID, id)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override suspend fun resumeDownload(id: String) {
        val download = getDownloadById(id)
        if (download != null) {
            startDownload(download)
        }
    }

    override suspend fun scheduleDownload(download: DownloadItem, scheduleTime: Long) {
        // Calculate delay in milliseconds
        val currentTime = System.currentTimeMillis()
        val delay = scheduleTime - currentTime

        if (delay > 0) {
            // Update status to QUEUED (scheduled)
            downloadDao.updateStatus(
                id = download.id,
                status = DownloadStatus.QUEUED.name,
                updatedAt = System.currentTimeMillis()
            )
            // Note: Actual scheduling is handled in StartDownloadUseCase
        } else {
            // Time has passed, start immediately
            startDownload(download)
        }
    }

    override suspend fun cancelDownload(id: String) {
        downloadDao.updateStatus(
            id = id,
            status = DownloadStatus.FAILED.name,
            updatedAt = System.currentTimeMillis()
        )
        // TODO: Implement actual cancel logic
    }

    override suspend fun retryDownload(id: String) {
        val download = getDownloadById(id)
        if (download != null) {
            val updatedDownload = download.copy(
                downloadedSize = 0,
                status = DownloadStatus.QUEUED
            )
            updateDownload(updatedDownload)
            startDownload(updatedDownload)
        }
    }

    private suspend fun requireExistingDownload(id: String): DownloadItem {
        return getDownloadById(id) ?: throw IllegalArgumentException("Download not found: $id")
    }

    private fun mimeTypeFor(file: File): String? {
        val ext = file.extension
        if (ext.isBlank()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
    }

    private fun fileProviderUri(file: File) = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

