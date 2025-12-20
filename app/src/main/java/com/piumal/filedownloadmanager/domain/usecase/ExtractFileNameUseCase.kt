package com.piumal.filedownloadmanager.domain.usecase

/**
 * Use case for extracting filename from URL
 * Single Responsibility Principle - handles only filename extraction logic
 */
class ExtractFileNameUseCase {

    /**
     * Extracts filename from URL
     * @param url The download URL
     * @return Extracted filename or empty string if extraction fails
     */
    operator fun invoke(url: String): String {
        return try {
            // Remove query parameters
            val cleanUrl = url.substringBefore("?")
            // Split by "/" and get last segment
            val segments = cleanUrl.split("/")
            segments.lastOrNull()?.takeIf { it.isNotEmpty() } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

