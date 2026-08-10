package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeDownloadRepository : DownloadRepository {
    private val downloads = mutableListOf<DownloadItem>()

    override fun getAllDownloads(): Flow<List<DownloadItem>> = flowOf(downloads.toList())

    override fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>> =
        flowOf(downloads.filter { it.status == status })

    override suspend fun getDownloadById(id: String): DownloadItem? = downloads.find { it.id == id }

    override suspend fun insertDownload(download: DownloadItem) {
        downloads.add(download)
    }

    override suspend fun updateDownload(download: DownloadItem) {
        val index = downloads.indexOfFirst { it.id == download.id }
        if (index != -1) downloads[index] = download
    }

    override suspend fun deleteDownloadFile(id: String): Result<Unit> = runCatching { deleteDownload(id) }

    override suspend fun moveDownloadFile(id: String, destinationPath: String): Result<Unit> = runCatching {
        val index = downloads.indexOfFirst { it.id == id }
        if (index != -1) downloads[index] = downloads[index].copy(filePath = destinationPath)
    }

    override suspend fun openDownload(id: String): Result<Unit> = Result.success(Unit)

    override suspend fun shareDownload(id: String, chooserTitle: String?): Result<Unit> = Result.success(Unit)

    override suspend fun showInFolder(id: String): Result<Unit> = Result.success(Unit)

    override suspend fun deleteDownload(id: String) {
        downloads.removeAll { it.id == id }
    }

    override suspend fun startDownload(download: DownloadItem) { /* no-op */ }

    override suspend fun scheduleDownload(download: DownloadItem, scheduleTime: Long) { /* no-op */ }

    override suspend fun pauseDownload(id: String) { /* no-op */ }

    override suspend fun resumeDownload(id: String) { /* no-op */ }

    override suspend fun cancelDownload(id: String) { /* no-op */ }

    override suspend fun retryDownload(id: String) { /* no-op */ }
}
