package com.piumal.filedownloadmanager.domain.util

/**
 * Normalizes user-provided or remote file names before they are written to storage.
 */
object FileNameSanitizer {

    // Blocked: Control characters (0-31, 127), path separators (/ \), and special characters
    private val illegalCharacters = Regex("[\\x00-\\x1F\\x7F/\\\\<>:\"|?*]")

    fun sanitize(fileName: String, fallback: String = "download_") : String {
        // 1. Remove all control characters and known illegal filename characters
        var sanitized = fileName.replace(illegalCharacters, "_")
        
        // 2. Remove all path-traversal sequences (e.g., ../, ..)
        sanitized = sanitized.replace("..", "_")
        
        // 3. Ensure we are only dealing with the name part, not any path
        sanitized = sanitized.substringAfterLast('/')
        sanitized = sanitized.substringAfterLast('\\')

        // 4. Clean up whitespace and dots
        sanitized = sanitized.trim().trim('.', ' ', '_')

        // 5. Handle length and empty names
        if (sanitized.length > 200) {
            sanitized = sanitized.substring(0, 200)
        }

        return when {
            sanitized.isNotBlank() -> sanitized
            else -> "$fallback${System.currentTimeMillis()}"
        }
    }
}
