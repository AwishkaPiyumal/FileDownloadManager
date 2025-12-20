package com.piumal.filedownloadmanager.domain.usecase

import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.pow

/**
 * Use case for formatting file size in human-readable format
 * Follows single responsibility principle
 */
class FormatFileSizeUseCase @Inject constructor() {

    /**
     * Convert bytes to human-readable format (B, KB, MB, GB, TB)
     * @param bytes File size in bytes
     * @return Formatted string with appropriate unit
     */
    operator fun invoke(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

        val size = bytes / 1024.0.pow(digitGroups.toDouble())

        return String.format("%.2f %s", size, units[digitGroups])
    }

    /**
     * Format download progress string
     * @param downloadedSize Size already downloaded
     * @param totalSize Total file size
     * @return Formatted progress string (e.g., "10.5 MB / 50.0 MB")
     */
    fun formatProgress(downloadedSize: Long, totalSize: Long): String {
        return "${invoke(downloadedSize)} / ${invoke(totalSize)}"
    }
}

