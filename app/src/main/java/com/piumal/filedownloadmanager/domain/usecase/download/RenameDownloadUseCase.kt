package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import javax.inject.Inject

interface RenameDownloadUseCase {
    suspend operator fun invoke(id: String, newName: String): Result<Unit>
}

class RenameDownloadUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : RenameDownloadUseCase {
    override suspend fun invoke(id: String, newName: String): Result<Unit> {
        return try {
            val download = repository.getDownloadById(id)
            if (download != null) {
                repository.updateDownload(download.copy(fileName = newName))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Download not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
