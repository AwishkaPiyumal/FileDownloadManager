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
     * Checks if the first bytes of a file match a malicious signature.
     */
    fun isMaliciousSignature(buffer: ByteArray): Boolean {
        // MZ (Windows Executable)
        if (buffer.size >= 2 && buffer[0] == 0x4D.toByte() && buffer[1] == 0x5A.toByte()) return true
        // ELF (Linux Executable)
        if (buffer.size >= 4 && buffer[0] == 0x7F.toByte() && buffer[1] == 0x45.toByte() && buffer[2] == 0x4C.toByte() && buffer[3] == 0x46.toByte()) return true
        // #! (Script)
        if (buffer.size >= 2 && buffer[0] == 0x23.toByte() && buffer[1] == 0x21.toByte()) return true

        return false
    }

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
     * Executable/installer-package extensions that must be blocked regardless of what
     * Content-Type header the server sends. This check must run BEFORE the MIME-type allow-list
     * below: an .apk file is technically a zip archive and can be served with
     * "application/zip" or "application/x-zip-compressed" - both of which are in
     * ALLOWED_MIME_TYPES - so checking MIME type first would let a relabeled (accidentally or
     * deliberately) installer file sail straight through. Verified: with the old ordering,
     * validateMimeType("application/zip", ".../app.apk") returned isValid=true.
     */
    private val BLOCKED_EXTENSIONS = setOf(
        "apk", "exe", "msi", "bat", "cmd", "sh", "dmg", "deb", "rpm"
    )

    /**
     * Validates the MIME type of a download.
     * 
     * @param mimeType The MIME type to validate (e.g., "video/mp4")
     * @return ValidationResult
     */
    fun validateMimeType(mimeType: String?, url: String): ValidationResult {
        val type = mimeType?.lowercase()?.trim() ?: ""
        val extension = getFileExtension(url)

        // Extension-based block runs first and cannot be bypassed by the MIME type below -
        // see the BLOCKED_EXTENSIONS doc comment for why the ordering matters here.
        if (extension != null && BLOCKED_EXTENSIONS.contains(extension)) {
            return ValidationResult(
                isValid = false,
                message = "This file type is blocked for security reasons."
            )
        }

        if (BLOCKED_MIME_TYPES.contains(type)) {
            return ValidationResult(
                isValid = false,
                message = "This file type is blocked for security reasons."
            )
        }

        // Allow explicitly allowed types and types starting with allowed prefixes.
        //
        // NOTE: this used to also unconditionally allow the literal string
        // "application/x-unknown-type", regardless of the file extension below. That string is
        // never produced by this codebase (a genuinely missing/empty Content-Type header already
        // becomes "" via the elvis operator above, which correctly falls through to the
        // extension check), so the only way to trigger it was a remote server sending that exact
        // non-standard header value on purpose - letting anyone who decompiles this app (trivial
        // for an Android APK) bypass every content-type check, including the apk restriction
        // above, with one response header. An unrecognized mime type now simply falls through to
        // the same extension check as every other unrecognized type, same as this comment always
        // claimed it did.
        if (ALLOWED_MIME_TYPES.contains(type) || ALLOWED_MIME_PREFIXES.any { type.startsWith(it) }) {
            return ValidationResult(
                isValid = true,
                message = "MIME type is valid"
            )
        }

        // Relaxed fallback: if the content type is unknown/generic, check the extension.
        // If extension is safe, allow it. (BLOCKED_EXTENSIONS above already ruled out apk/exe/
        // etc. regardless of MIME type, so this list doesn't need to defend against those too.)
        if (extension != null) {
            val safeExtensions = setOf(
                "mp4", "mp3", "pdf", "zip", "rar", "7z", "jpg", "jpeg", "png",
                "gif", "webp", "txt", "doc", "docx", "xls", "xlsx", "json", "iso", "tar", "gz"
            )

            if (safeExtensions.contains(extension)) {
                return ValidationResult(isValid = true, message = "Valid via extension fallback")
            }
        }
        
        // If content type is unknown/generic, and extension is missing or not explicitly safe, 
        // we might still want to allow it if it's not explicitly blocked to improve user experience, 
        // but for now, we continue to block truly unknown/unsafe types to maintain security.
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
     *
     * Matched as an exact host or a proper subdomain (see isBlockedDomain below) - NOT as a
     * bare substring. A substring check here would false-positive on any unrelated domain that
     * merely contains one of these as text, e.g. "myyoutube.com" or
     * "youtube.com.some-other-site.net" (the latter isn't even a youtube.com subdomain - it's a
     * completely different domain, "some-other-site.net", that just happens to have "youtube.com"
     * as an earlier path-like label, which a plain .contains() would incorrectly treat as blocked).
     */
    private val BLOCKED_DOMAINS = setOf(
        // ==================== VIDEO STREAMING PLATFORMS ====================
        "youtube.com", "youtu.be", "youtube-nocookie.com",

        // Strict DRM-protected streaming services
        "netflix.com", "nflxvideo.net", "nflxext.com", "nflximg.net",
        "disneyplus.com", "hbomax.com", "primevideo.com", "hulu.com",
        "tv.apple.com", "peacocktv.com", "paramountplus.com", "showtime.com",
        "spotify.com", "music.apple.com", "deezer.com", "tidal.com", "soundcloud.com"
    )

    /**
     * Downloader-tool and mirror-site *name fragments* - deliberately matched as a substring
     * anywhere in the host (see isBlockedDomain below), because these aren't registrable domains
     * on their own; they're tool/brand names that show up as part of many different mirror
     * domains (e.g. "y2mate" catches y2mate.com, y2mate.nu, y2mate-proxy7.io alike). Exact/
     * subdomain matching, as used for BLOCKED_DOMAINS above, wouldn't make sense for these.
     * Kept short and reasonably distinctive on purpose to limit false positives - see
     * SUSPICIOUS_WORD_PATTERNS below for generic English words, which use word-boundary matching
     * instead since substring matching on a common word is much more false-positive-prone.
     */
    private val BLOCKED_DOMAIN_KEYWORDS = setOf(
        "youtube-dl", "ytmp3", "y2mate", "savefrom", "keepvid", "yt1s", "ytmp4",
        "piratebay", "thepiratebay", "kickass", "rarbg", "yts", "1337x",
        "eztv", "limetorrent", "torrentz", "extratorrent"
    )

    /**
     * True if [host] is (or is a subdomain of) a BLOCKED_DOMAINS entry, or contains one of the
     * BLOCKED_DOMAIN_KEYWORDS fragments.
     */
    private fun isBlockedDomain(host: String): Boolean {
        val domain = host.lowercase()
        val isBlockedExactOrSubdomain = BLOCKED_DOMAINS.any { blocked ->
            domain == blocked || domain.endsWith(".$blocked")
        }
        val containsBlockedKeyword = BLOCKED_DOMAIN_KEYWORDS.any { domain.contains(it) }
        return isBlockedExactOrSubdomain || containsBlockedKeyword
    }

    /**
     * Suspicious URL/filename patterns that may indicate illegal activity.
     *
     * Matched with word boundaries (see containsSuspiciousPattern below), not a bare substring -
     * a plain .contains("hack") would block a perfectly legitimate file named
     * "life-hacks-guide.pdf" or "growth-hacking-101.pdf". Word-boundary matching still catches
     * "software-hack-tool.zip" or "game-cheat-download.exe" (hyphens/punctuation count as
     * boundaries), it just requires the exact word rather than any substring.
     */
    private val SUSPICIOUS_WORD_PATTERNS = listOf(
        "crack", "keygen", "pirate", "warez",
        "nulled", "leaked", "ripped", "torrent",
        "hack", "cheat", "mod-apk"
    )

    /** Literal (non-word) patterns not suited to \b word-boundary matching. */
    private val SUSPICIOUS_LITERAL_PATTERNS = listOf("magnet:")

    private fun containsSuspiciousPattern(text: String): Boolean {
        val lower = text.lowercase()
        val wordHit = SUSPICIOUS_WORD_PATTERNS.any { word ->
            Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(lower)
        }
        val literalHit = SUSPICIOUS_LITERAL_PATTERNS.any { lower.contains(it) }
        return wordHit || literalHit
    }

    /**
     * Maximum file size (5 GB) to support enterprise-grade downloads while preserving abuse controls.
     */
    private const val MAX_FILE_SIZE = 5000L * 1024 * 1024

    fun getMaxFileSize(): Long = MAX_FILE_SIZE

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

        // Enforce HTTPS only
        if (!isSecureConnection(url)) {
            return ValidationResult(
                isValid = false,
                message = "Only HTTPS URLs are supported"
            )
        }

        // Check for blocked domains
        val domain = parsedUrl.host.lowercase()
        if (isBlockedDomain(domain)) {
            return ValidationResult(
                isValid = false,
                message = "You cannot download from this platform."
            )
        }

        // Check for suspicious patterns in URL
        if (containsSuspiciousPattern(url)) {
            return ValidationResult(
                isValid = false,
                message = "You cannot download this content."
            )
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
        if (containsSuspiciousPattern(fileName)) {
            return ValidationResult(
                isValid = false,
                message = "You cannot download this content."
            )
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
     * Get file extension from URL.
     *
     * LIMITATION: this only looks at the URL path, so it returns null for dynamic download
     * endpoints with no file-like path segment (e.g. "https://example.com/download?id=123",
     * a very common pattern for cloud storage and file-hosting links). The authoritative source
     * for a download's real filename is the HTTP response's Content-Disposition header, when
     * present - see [extractFileNameFromContentDisposition]. This function (and
     * [extractFileName] below) should be treated as a best-effort fallback for when that header
     * is absent, not the primary source of truth.
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
     * Extract file name from URL.
     *
     * LIMITATION: same as [getFileExtension] above - URL-path-only, so it fails for dynamic
     * download endpoints. Prefer [extractFileNameFromContentDisposition] when a Content-Disposition
     * header is available; use this only as the fallback when it isn't.
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
     * Extracts a filename from a Content-Disposition header value per RFC 6266, e.g.
     * `attachment; filename="report.pdf"` or the UTF-8 extended form
     * `attachment; filename*=UTF-8''report%20final.pdf`. Prefers the extended form when both are
     * present, since it's the one that correctly represents non-ASCII names.
     *
     * Not currently called anywhere in the download pipeline - this is a ready-to-use utility,
     * not yet wired in. To use it: read `response.header("Content-Disposition")` where the HTTP
     * response is available (DownloadManager.downloadFile), and prefer its result over
     * [extractFileName] when non-null. Still run the result through [FileNameSanitizer] exactly
     * as today, since this parses a value that ultimately comes from a remote server.
     */
    fun extractFileNameFromContentDisposition(headerValue: String?): String? {
        if (headerValue.isNullOrBlank()) return null

        Regex("filename\\*=UTF-8''([^;]+)", RegexOption.IGNORE_CASE)
            .find(headerValue)
            ?.groupValues?.get(1)
            ?.let { encoded ->
                return try {
                    java.net.URLDecoder.decode(encoded.trim(), "UTF-8")
                } catch (e: Exception) {
                    null
                }
            }

        Regex("filename=\"?([^\";]+)\"?", RegexOption.IGNORE_CASE)
            .find(headerValue)
            ?.groupValues?.get(1)
            ?.trim()
            ?.let { if (it.isNotEmpty()) return it }

        return null
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

