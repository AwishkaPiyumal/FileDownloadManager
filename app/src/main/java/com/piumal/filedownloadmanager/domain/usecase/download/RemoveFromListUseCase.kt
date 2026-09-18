package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import javax.inject.Inject

interface RemoveFromListUseCase {
    suspend operator fun invoke(id: String): Result<Unit>
}

class RemoveFromListUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : RemoveFromListUseCase {
    override suspend fun invoke(id: String): Result<Unit> {
        return runCatching {
            // Remove from list must only delete the Room record, never the physical file.
            repository.deleteDownload(id)
        }
    }
}
