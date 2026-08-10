package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import javax.inject.Inject

interface MoveToUseCase {
    suspend operator fun invoke(id: String, destinationPath: String): Result<Unit>
}

class MoveToUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : MoveToUseCase {
    override suspend fun invoke(id: String, destinationPath: String): Result<Unit> {
        return repository.moveDownloadFile(id, destinationPath)
    }
}
