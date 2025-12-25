package com.piumal.filedownloadmanager.domain.model

/**
 * Download Configuration
 * Contains all information needed to start a download
 */
data class DownloadConfig(
    val url: String,
    val filePath: String,
    val fileName: String,
    val scheduleTime: Long? = null
)

