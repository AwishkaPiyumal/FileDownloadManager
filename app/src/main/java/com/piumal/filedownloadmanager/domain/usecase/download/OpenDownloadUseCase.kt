package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import javax.inject.Inject

interface OpenDownloadUseCase {
    suspend operator fun invoke(id: String): Result<Unit>
}

class OpenDownloadUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : OpenDownloadUseCase {
    override suspend fun invoke(id: String): Result<Unit> {
        return repository.openDownload(id)
    }
}
