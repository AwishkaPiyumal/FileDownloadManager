package com.piumal.filedownloadmanager.domain.usecase.settings

import com.piumal.filedownloadmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveNotifyDownloadCompletionUseCase @Inject constructor(
    private val settingsRepository: com.piumal.filedownloadmanager.domain.repository.SettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = settingsRepository.observeNotifyDownloadCompletion()
}
