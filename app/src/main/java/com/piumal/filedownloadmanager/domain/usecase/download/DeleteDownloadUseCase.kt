package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import javax.inject.Inject

interface DeleteDownloadUseCase {
    suspend operator fun invoke(id: String): Result<Unit>
}

class DeleteDownloadUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : DeleteDownloadUseCase {
    override suspend fun invoke(id: String): Result<Unit> {
        return repository.deleteDownloadFile(id)
    }
}
