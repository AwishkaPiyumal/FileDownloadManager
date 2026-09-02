package com.piumal.filedownloadmanager.domain.util

/**
 * Normalizes user-provided or remote file names before they are written to storage.
 */
object FileNameSanitizer {

    private val illegalCharacters = Regex("[<>:\"/\\\\|?*\u0000]")

    fun sanitize(fileName: String, fallback: String = "download_") : String {
        val withoutNullBytes = fileName.replace("\u0000", "")
        val strippedPath = withoutNullBytes
            .replace('\\', '/')
            .substringAfterLast('/')
            .replace("..", "")

        val cleaned = illegalCharacters.replace(strippedPath, "_")
            .trim()
            .trim('.', ' ')

        return when {
            cleaned.isNotBlank() -> cleaned
            else -> "$fallback${System.currentTimeMillis()}"
        }
    }
}
