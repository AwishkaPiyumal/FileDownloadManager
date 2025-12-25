package com.piumal.filedownloadmanager.domain.model

/**
 * Domain model representing a download item
 * Follows Clean Architecture principles
 */
data class DownloadItem(
    val id: String,
    val fileName: String,
    val downloadedSize: Long,
    val totalSize: Long,
    val status: DownloadStatus,
    val url: String,
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val scheduleTime: Long? = null  // Timestamp when download should start (null = immediate)
) {
    /**
     * Calculate download progress percentage
     * @return Progress percentage (0-100)
     */
    fun getProgressPercentage(): Int {
        return if (totalSize > 0) {
            ((downloadedSize * 100) / totalSize).toInt()
        } else {
            0
        }
    }

    /**
     * Check if download is in progress
     */
    fun isInProgress(): Boolean = status == DownloadStatus.DOWNLOADING

    /**
     * Check if download is completed
     */
    fun isCompleted(): Boolean = status == DownloadStatus.COMPLETED

    /**
     * Check if download is paused
     */
    fun isPaused(): Boolean = status == DownloadStatus.PAUSED

    /**
     * Check if download has failed
     */
    fun isFailed(): Boolean = status == DownloadStatus.FAILED
}

/**
 * Enum representing download status
 * Follows Google Play Store content policy - clear status indication
 */
enum class DownloadStatus {
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    QUEUED
}

