package com.piumal.filedownloadmanager.domain.usecase

import com.piumal.filedownloadmanager.domain.repository.ClipboardRepository

/**
 * Use case for getting URL from clipboard
 * Encapsulates the business logic of clipboard URL retrieval
 */
class GetClipboardUrlUseCase(
    private val clipboardRepository: ClipboardRepository
) {

    /**
     * Retrieves URL from clipboard
     * @return URL string or null if not available
     */
    suspend operator fun invoke(): String? {
        return clipboardRepository.getClipboardUrl()
    }
}

