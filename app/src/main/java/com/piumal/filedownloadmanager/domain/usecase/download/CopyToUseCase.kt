package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import javax.inject.Inject

import com.piumal.filedownloadmanager.domain.model.DownloadItem

interface CopyToUseCase {
    suspend operator fun invoke(item: DownloadItem, destinationFolderPath: String): Result<Unit>
}

class CopyToUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : CopyToUseCase {
    override suspend fun invoke(item: DownloadItem, destinationFolderPath: String): Result<Unit> {
        return repository.copyDownload(item, destinationFolderPath)
    }
}
