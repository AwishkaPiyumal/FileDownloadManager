package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import javax.inject.Inject

interface ShowInfoUseCase {
    suspend operator fun invoke(id: String): Result<DownloadItem>
}

class ShowInfoUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : ShowInfoUseCase {
    override suspend fun invoke(id: String): Result<DownloadItem> {
        return runCatching {
            repository.getDownloadById(id) ?: throw IllegalArgumentException("Download not found: $id")
        }
    }
}
