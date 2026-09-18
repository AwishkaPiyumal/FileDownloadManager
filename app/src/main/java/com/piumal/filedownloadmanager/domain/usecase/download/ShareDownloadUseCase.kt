package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import javax.inject.Inject

interface ShareDownloadUseCase {
    suspend operator fun invoke(id: String): Result<Unit>
}

class ShareDownloadUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : ShareDownloadUseCase {
    override suspend fun invoke(id: String): Result<Unit> {
        return repository.shareDownload(id)
    }
}
