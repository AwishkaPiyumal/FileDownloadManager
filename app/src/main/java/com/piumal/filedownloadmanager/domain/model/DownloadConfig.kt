package com.piumal.filedownloadmanager.domain.model

/**
 * Domain model representing download configuration
 * @param url The download URL
 * @param filePath The destination file path
 * @param fileName The name of the file to be saved
 * @param scheduleTime Optional scheduled time for download (null for immediate download)
 */
data class DownloadConfig(
    val url: String = "",
    val filePath: String = "",
    val fileName: String = "",
    val scheduleTime: Long? = null
)

