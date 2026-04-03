package com.piumal.filedownloadmanager.domain.usecase.util

import com.piumal.filedownloadmanager.domain.repository.ClipboardRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case to get URL from clipboard
 * Follows Clean Architecture - encapsulates business logic
 * Uses ClipboardRepository for clipboard access
 */
@Singleton
class GetClipboardUrlUseCase @Inject constructor(
    private val clipboardRepository: ClipboardRepository
) {
    suspend operator fun invoke(): String? {
        return clipboardRepository.getClipboardUrl()
    }
}