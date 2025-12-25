package com.piumal.filedownloadmanager.domain.usecase

import com.piumal.filedownloadmanager.domain.util.ContentValidator
import javax.inject.Inject

/**
 * Use Case to extract filename from URL
 * Follows Clean Architecture - encapsulates business logic
 */
class ExtractFileNameUseCase @Inject constructor() {
    operator fun invoke(url: String): String {
        return ContentValidator.extractFileName(url)
            ?: "download_${System.currentTimeMillis()}"
    }
}

