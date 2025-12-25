package com.piumal.filedownloadmanager.data.repository

import com.piumal.filedownloadmanager.data.download.DownloadManager
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.data.local.entity.DownloadEntity
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
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

        // Start download in background
        repositoryScope.launch {
            downloadManager.downloadFile(download).collect { progress ->
                // Update progress in database
                downloadDao.updateProgress(
                    id = download.id,
                    downloadedSize = progress.downloadedBytes,
                    totalSize = progress.totalBytes,
                    updatedAt = System.currentTimeMillis()
                )

                // Update status
                when (progress.status) {
                    DownloadStatus.COMPLETED -> {
                        downloadDao.updateStatus(
                            id = download.id,
                            status = DownloadStatus.COMPLETED.name,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    DownloadStatus.FAILED -> {
                        downloadDao.updateStatus(
                            id = download.id,
                            status = DownloadStatus.FAILED.name,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    else -> {
                        // Continue downloading
                    }
                }
            }
        }
    }

    override suspend fun pauseDownload(id: String) {
        downloadDao.updateStatus(
            id = id,
            status = DownloadStatus.PAUSED.name,
            updatedAt = System.currentTimeMillis()
        )
        // TODO: Implement actual pause logic
    }

    override suspend fun resumeDownload(id: String) {
        val download = getDownloadById(id)
        if (download != null) {
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

