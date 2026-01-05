package com.piumal.filedownloadmanager.domain.usecase

import com.piumal.filedownloadmanager.domain.util.ContentValidator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case to extract filename from URL
 * Follows Clean Architecture - encapsulates business logic
 */
@Singleton
class ExtractFileNameUseCase @Inject constructor() {
    operator fun invoke(url: String): String {
        return ContentValidator.extractFileName(url)
            ?: "download_${System.currentTimeMillis()}"
    }
}

