package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import javax.inject.Inject

interface ShowInFolderUseCase {
    suspend operator fun invoke(id: String): Result<Unit>
}

class ShowInFolderUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : ShowInFolderUseCase {
    override suspend fun invoke(id: String): Result<Unit> {
        return repository.showInFolder(id)
    }
}
