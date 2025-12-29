package com.piumal.filedownloadmanager.data.repository

import android.content.Context
import com.piumal.filedownloadmanager.data.download.DownloadManager
import com.piumal.filedownloadmanager.data.download.DownloadService
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.data.local.entity.DownloadEntity
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    // Coroutine scope for background operations
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        val intent = android.content.Intent(context, DownloadService::class.java).apply {
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
}

