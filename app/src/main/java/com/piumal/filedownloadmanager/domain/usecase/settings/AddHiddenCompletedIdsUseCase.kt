package com.piumal.filedownloadmanager.domain.usecase.settings

import com.piumal.filedownloadmanager.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddHiddenCompletedIdsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(ids: Set<String>) {
        settingsRepository.addHiddenCompletedIds(ids)
    }
}