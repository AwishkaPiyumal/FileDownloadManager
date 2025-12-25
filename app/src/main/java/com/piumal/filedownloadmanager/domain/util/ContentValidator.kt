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
     * Allowed file extensions that are generally safe and legal
     * These are common file types that don't typically involve piracy
     */
    private val ALLOWED_EXTENSIONS = setOf(
        // Documents
        "pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx", "csv",

        // Images
        "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico",

        // Archives
        "zip", "rar", "7z", "tar", "gz", "bz2",

        // Code/Development
        "apk", "json", "xml", "html", "css", "js", "java", "kt", "py", "cpp", "c", "h",

        // Data
        "db", "sql", "sqlite", "mdb",

        // Others
        "iso", "log", "md", "yaml", "yml"
    )

    /**
     * Restricted extensions that require extra warnings
     * These are often used for piracy but have legitimate uses
     */
    private val RESTRICTED_EXTENSIONS = setOf(
        // Media files - high piracy risk
        "mp3", "mp4", "avi", "mkv", "flv", "wmv", "mov", "m4a", "wav", "flac",
        "aac", "ogg", "webm", "m4v", "3gp", "mpeg", "mpg",

        // Executable files - malware risk
        "exe", "msi", "bat", "sh", "dll", "so", "dmg", "pkg", "deb", "rpm"
    )

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

        // Netflix (DRM-protected - ILLEGAL to download)
        "netflix.com", "nflxvideo.net", "nflxext.com", "nflximg.net",

        // Disney+ (DRM-protected)
        "disneyplus.com", "disney.com", "starwars.com", "marvel.com",

        // HBO Max (DRM-protected)
        "hbomax.com", "hbo.com", "hbogo.com", "hbonow.com",

        // Amazon Prime Video (DRM-protected)
        "primevideo.com", "amazonvideo.com", "amazon.com/gp/video",

        // Hulu (DRM-protected)
        "hulu.com", "hulustream.com",

        // Apple TV+ (DRM-protected)
        "tv.apple.com", "appletv.com",

        // Peacock, Paramount+, etc.
        "peacocktv.com", "paramountplus.com", "showtime.com",

        // Other streaming
        "vimeo.com", "dailymotion.com", "twitch.tv",

        // ==================== SOCIAL MEDIA PLATFORMS ====================
        // Facebook (violates Facebook ToS + copyright issues)
        "facebook.com", "fb.com", "fbcdn.net", "fb.watch",

        // Instagram (violates Instagram ToS + copyright)
        "instagram.com", "cdninstagram.com", "igcdn.com",

        // TikTok (violates TikTok ToS + copyright)
        "tiktok.com", "tiktokv.com", "tiktokcdn.com", "musical.ly",

        // Twitter/X (violates Twitter ToS)
        "twitter.com", "x.com", "twimg.com", "t.co",

        // Snapchat (violates Snapchat ToS)
        "snapchat.com", "snap.com",

        // Reddit (media copyright issues)
        "reddit.com", "redd.it", "v.redd.it",

        // Pinterest (copyright issues)
        "pinterest.com", "pinimg.com",

        // LinkedIn (violates LinkedIn ToS)
        "linkedin.com",

        // ==================== MUSIC STREAMING ====================
        // Spotify (DRM-protected + violates ToS)
        "spotify.com", "spotifycdn.com",

        // Apple Music (DRM-protected)
        "music.apple.com", "applemusic.com",

        // Deezer, Tidal, etc. (DRM-protected)
        "deezer.com", "tidal.com", "tidalhifi.com",

        // SoundCloud (violates ToS + copyright)
        "soundcloud.com", "sndcdn.com",

        // Pandora (DRM-protected)
        "pandora.com",

        // ==================== PIRACY/TORRENT SITES ====================
        "piratebay", "thepiratebay", "kickass", "rarbg", "yts", "1337x",
        "eztv", "limetorrent", "torrentz", "extratorrent",

        // ==================== ADULT CONTENT ====================
        // Required by Google Play Store policy
        "pornhub", "xvideos", "xnxx", "redtube", "youporn",
        "xhamster", "spankbang", "chaturbate", "onlyfans",

        // ==================== MALWARE/HACKING ====================
        "malware", "virus", "warez", "darkweb", "onion"
    )

    /**
     * Suspicious URL patterns that may indicate illegal activity
     */
    private val SUSPICIOUS_PATTERNS = listOf(
        "crack", "keygen", "patch", "serial", "pirate", "warez",
        "nulled", "leaked", "ripped", "torrent", "magnet:",
        "free-premium", "hack", "cheat", "mod-apk"
    )

    /**
     * Maximum file size (500 MB) to prevent abuse and excessive downloads
     * Large files are more likely to be pirated movies/software
     */
    private const val MAX_FILE_SIZE = 500L * 1024 * 1024 // 500 MB

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

        // Extract file extension
        val fileName = parsedUrl.path.substringAfterLast('/')
        val extension = fileName.substringAfterLast('.', "").lowercase()

        // Check if extension is blocked
        if (extension.isNotEmpty() &&
            !ALLOWED_EXTENSIONS.contains(extension) &&
            !RESTRICTED_EXTENSIONS.contains(extension)) {
            return ValidationResult(
                isValid = false,
                message = "You cannot download this file type."
            )
        }

        // Check if extension requires warning
        if (RESTRICTED_EXTENSIONS.contains(extension)) {
            return ValidationResult(
                isValid = false,
                message = "You cannot download this file type.",
                requiresWarning = false,
                warningMessage = null
            )
        }

        // All checks passed
        return ValidationResult(
            isValid = true,
            message = "URL validated successfully"
        )
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
                message = "File size exceeds maximum limit (500 MB). Large files are restricted to prevent abuse."
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

