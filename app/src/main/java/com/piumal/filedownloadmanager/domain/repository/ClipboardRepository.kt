package com.piumal.filedownloadmanager.domain.repository

/**
 * Repository interface for clipboard operations
 * Provides abstraction for system clipboard access
 */
interface ClipboardRepository {
    /**
     * Retrieves URL from clipboard if available
     * @return URL string or null if clipboard doesn't contain valid URL
     */
    suspend fun getClipboardUrl(): String?
}

