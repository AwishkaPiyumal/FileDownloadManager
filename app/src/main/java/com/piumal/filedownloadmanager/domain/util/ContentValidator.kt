package com.piumal.filedownloadmanager.domain.util

import java.net.URL

/**
 * Content Validator for Google Policy Compliance
 *
 * CRITICAL: This class ensures compliance with:
 * - Google AdMob Content Policies
 * - Google Play Store Developer Policies
 * - Copyright laws (DMCA)
 * - User safety guidelines
 *
 * @author File Download Manager Team
 * @version 1.0
 */
object ContentValidator {

    /**
     * Allowed MIME types for safer downloads.
     */
    private val ALLOWED_MIME_TYPES = setOf(
        "application/pdf",
        "application/zip",
        "application/x-zip-compressed", // Sometimes used for zip
        "application/json",
        "application/xml",
        "text/plain",
        "text/html",
        "text/css",
        "application/javascript"
    )

    private val ALLOWED_MIME_PREFIXES = listOf("video/", "audio/", "image/")

    /**
     * Dangerous MIME types that must be explicitly blocked.
     */
    private val BLOCKED_MIME_TYPES = setOf(
        "application/x-msdownload",
        "application/x-sh",
        "application/x-executable",
        //"application/octet-stream" // Often used for executables
    )

    /**
     * Validates the MIME type of a download.
     * 
     * @param mimeType The MIME type to validate (e.g., "video/mp4")
     * @return ValidationResult
     */
    fun validateMimeType(mimeType: String?, url: String): ValidationResult {
        val type = mimeType?.lowercase()?.trim() ?: ""

        if (BLOCKED_MIME_TYPES.contains(type)) {
            return ValidationResult(
                isValid = false,
                message = "This file type is blocked for security reasons."
            )
        }

        if (ALLOWED_MIME_TYPES.contains(type) || ALLOWED_MIME_PREFIXES.any { type.startsWith(it) }) {
            return ValidationResult(
                isValid = true,
                message = "MIME type is valid"
            )
        }

        // Fallback for generic or missing MIME types (e.g. octet-stream)
        if (type.isEmpty() || type == "application/octet-stream" || type == "binary/octet-stream") {
            val extension = getFileExtension(url)
            val safeExtensions = setOf(
                "mp4", "mp3", "pdf", "zip", "rar", "7z", "jpg", "jpeg", "png",
                "gif", "webp", "txt", "doc", "docx", "xls", "xlsx", "apk", "json", "iso"
            )

            if (extension != null && safeExtensions.contains(extension)) {
                return ValidationResult(isValid = true, message = "Valid via extension fallback")
            }
        }

        return ValidationResult(
            isValid = false,
            message = "Unsupported or unsafe file type: $mimeType"
        )
    }

    /**
     * Blocked domains - CRITICAL for Google Play Store & AdMob Compliance
     * Based on FDM (Free Download Manager) blocking policy
     *
     * MUST BLOCK: Streaming platforms with DRM and Terms of Service restrictions
     * Reason: Copyright infringement + violates platform ToS = App Store removal
     */
    private val BLOCKED_DOMAINS = setOf(
        // ==================== VIDEO STREAMING PLATFORMS ====================
        // YouTube (ALL variations - violates YouTube ToS + copyright)
        "youtube.com", "youtu.be", "youtube-nocookie.com",
        "youtube-dl", "ytmp3", "y2mate", "savefrom", "keepvid", "yt1s", "ytmp4",

        // Strict DRM-protected streaming services
        "netflix.com", "nflxvideo.net", "nflxext.com", "nflximg.net",
        "disneyplus.com", "hbomax.com", "primevideo.com", "hulu.com",
        "tv.apple.com", "peacocktv.com", "paramountplus.com", "showtime.com",
        "spotify.com", "music.apple.com", "deezer.com", "tidal.com", "soundcloud.com",

        // ==================== PIRACY/TORRENT SITES ====================
        "piratebay", "thepiratebay", "kickass", "rarbg", "yts", "1337x",
        "eztv", "limetorrent", "torrentz", "extratorrent", "torrent", "magnet:"
    )

    /**
     * Suspicious URL patterns that may indicate illegal activity
     */
    private val SUSPICIOUS_PATTERNS = listOf(
        "crack", "keygen", "pirate", "warez",
        "nulled", "leaked", "ripped", "torrent", "magnet:",
        "hack", "cheat", "mod-apk"
    )

    /**
     * Maximum file size (5 GB) to support enterprise-grade downloads while preserving abuse controls.
     */
    private const val MAX_FILE_SIZE = 5000L * 1024 * 1024

    /**
     * Validation result containing status and message
     */
    data class ValidationResult(
        val isValid: Boolean,
        val message: String,
        val requiresWarning: Boolean = false,
        val warningMessage: String? = null
    )

    /**
     * Comprehensive validation of download URL
     *
     * @param url The URL to validate
     * @return ValidationResult with status and message
     */
    fun validateDownloadUrl(url: String): ValidationResult {
        // Basic URL validation
        if (url.isBlank()) {
            return ValidationResult(
                isValid = false,
                message = "URL cannot be empty"
            )
        }

        // Check if URL is valid format
        val parsedUrl = try {
            URL(url)
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                message = "Invalid URL format"
            )
        }

        // Enforce HTTP/HTTPS only
        if (!isHttpOrHttps(url)) {
            return ValidationResult(
                isValid = false,
                message = "Only HTTP and HTTPS URLs are supported"
            )
        }

        // Check for blocked domains
        val domain = parsedUrl.host.lowercase()
        if (BLOCKED_DOMAINS.any { domain.contains(it) }) {
            return ValidationResult(
                isValid = false,
                message = "You cannot download from this platform."
            )
        }

        // Check for suspicious patterns in URL
        val lowerUrl = url.lowercase()
        SUSPICIOUS_PATTERNS.forEach { pattern ->
            if (lowerUrl.contains(pattern)) {
                return ValidationResult(
                    isValid = false,
                    message = "You cannot download this content."
                )
            }
        }

        // All checks passed
        return ValidationResult(
            isValid = true,
            message = "URL validated successfully"
        )
    }

    /**
     * Returns true when the URL uses a supported HTTP scheme.
     */
    fun isHttpOrHttps(url: String): Boolean {
        return url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)
    }

    /**
     * Validate file name for illegal content indicators
     *
     * @param fileName The file name to validate
     * @return ValidationResult
     */
    fun validateFileName(fileName: String): ValidationResult {
        if (fileName.isBlank()) {
            return ValidationResult(
                isValid = false,
                message = "File name cannot be empty"
            )
        }

        // Check for suspicious patterns in file name
        val lowerFileName = fileName.lowercase()
        SUSPICIOUS_PATTERNS.forEach { pattern ->
            if (lowerFileName.contains(pattern)) {
                return ValidationResult(
                    isValid = false,
                    message = "You cannot download this content."
                )
            }
        }

        // Check file name length
        if (fileName.length > 255) {
            return ValidationResult(
                isValid = false,
                message = "File name is too long (max 255 characters)"
            )
        }

        return ValidationResult(
            isValid = true,
            message = "File name is valid"
        )
    }

    /**
     * Validate file size
     *
     * @param size File size in bytes
     * @return ValidationResult
     */
    fun validateFileSize(size: Long): ValidationResult {
        if (size <= 0) {
            return ValidationResult(
                isValid = false,
                message = "Invalid file size"
            )
        }

        if (size > MAX_FILE_SIZE) {
            return ValidationResult(
                isValid = false,
                message = "File size exceeds maximum limit (5 GB)."
            )
        }

        return ValidationResult(
            isValid = true,
            message = "File size is acceptable"
        )
    }

    /**
     * Check if URL is HTTPS (secure connection)
     * Google Play encourages secure connections
     */
    fun isSecureConnection(url: String): Boolean {
        return url.startsWith("https://", ignoreCase = true)
    }

    /**
     * Get file extension from URL
     */
    fun getFileExtension(url: String): String? {
        return try {
            val parsedUrl = URL(url)
            val fileName = parsedUrl.path.substringAfterLast('/')
            val extension = fileName.substringAfterLast('.', "")
            if (extension.isNotEmpty()) extension.lowercase() else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract file name from URL
     */
    fun extractFileName(url: String): String? {
        return try {
            val parsedUrl = URL(url)
            val fileName = parsedUrl.path.substringAfterLast('/')
            if (fileName.isNotEmpty()) fileName else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generate copyright disclaimer text
     * Simplified version - one line message
     */
    fun getCopyrightDisclaimer(): String {
        return "You are responsible for ensuring you have legal rights to download this content."
    }

    /**
     * Generate terms of service text
     * Simplified version - one line message
     */
    fun getTermsOfService(): String {
        return "Use this app responsibly and only download content you have legal rights to access."
    }
}

