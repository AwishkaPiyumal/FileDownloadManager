package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.data.repository.DownloadFileOperations
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Test-only repository that stores downloads in memory but performs real file operations
 * for delete/move so use case tests can assert actual filesystem behavior.
 */
class FileOperationTestRepository : DownloadRepository {
    private val downloads = mutableListOf<DownloadItem>()

    override fun getAllDownloads(): Flow<List<DownloadItem>> = flowOf(downloads.toList())
    override fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>> = flowOf(downloads.filter { it.status == status })
    override suspend fun getDownloadById(id: String): DownloadItem? = downloads.find { it.id == id }
    override suspend fun insertDownload(download: DownloadItem) { downloads.add(download) }
    override suspend fun updateDownload(download: DownloadItem) {
        val index = downloads.indexOfFirst { it.id == download.id }
        if (index != -1) downloads[index] = download
    }

    override suspend fun deleteDownloadFile(id: String): Result<Unit> = runCatching {
        val download = getDownloadById(id) ?: throw IllegalArgumentException("Download not found: $id")
        DownloadFileOperations.deletePhysicalFile(download.filePath)
        deleteDownload(id)
    }

    override suspend fun moveDownloadFile(id: String, destinationPath: String): Result<Unit> = runCatching {
        val download = getDownloadById(id) ?: throw IllegalArgumentException("Download not found: $id")
        val target = DownloadFileOperations.movePhysicalFile(download.filePath, destinationPath)
        updateDownload(download.copy(filePath = target.absolutePath))
    }

    override suspend fun openDownload(id: String): Result<Unit> = Result.success(Unit)
    override suspend fun shareDownload(id: String, chooserTitle: String?): Result<Unit> = Result.success(Unit)
    override suspend fun showInFolder(id: String): Result<Unit> = Result.success(Unit)
    override suspend fun deleteDownload(id: String) { downloads.removeAll { it.id == id } }
    override suspend fun startDownload(download: DownloadItem) { }
    override suspend fun scheduleDownload(download: DownloadItem, scheduleTime: Long) { }
    override suspend fun pauseDownload(id: String) { }
    override suspend fun resumeDownload(id: String) { }
    override suspend fun cancelDownload(id: String) { }
    override suspend fun retryDownload(id: String) { }
}

