package com.piumal.filedownloadmanager.domain.manager

import android.content.Context
import android.util.Log
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DownloadQueueManager
 *
 * Manages download queue with parallel download limits.
 * Implements FIFO (First In First Out) queue behavior.
 *
 * Features:
 * - Respects parallel download limit from settings
 * - Queues downloads when limit is reached
 * - Automatically starts next download when one completes
 * - Thread-safe operations using Mutex
 *
 * Architecture: Domain layer manager following Clean Architecture
 */
@Singleton
class DownloadQueueManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao
) {
    companion object {
        private const val TAG = "DownloadQueueManager"
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_PARALLEL_DOWNLOAD = "parallel_download"
        private const val DEFAULT_PARALLEL_LIMIT = 3
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    // Track currently active (downloading) download IDs
    private val _activeDownloads = MutableStateFlow<Set<String>>(emptySet())
    val activeDownloads: StateFlow<Set<String>> = _activeDownloads.asStateFlow()

    // SharedPreferences for getting parallel download limit
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Listener to auto-refresh queue when parallel limit changes
    private val prefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_PARALLEL_DOWNLOAD) {
            Log.d(TAG, "Parallel download limit changed, refreshing queue")
            refreshQueue()
        }
    }

    init {
        // Register listener for preference changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    /**
     * Get current parallel download limit from settings
     */
    fun getParallelLimit(): Int {
        return sharedPreferences.getInt(KEY_PARALLEL_DOWNLOAD, DEFAULT_PARALLEL_LIMIT)
    }

    /**
     * Check if a new download can start immediately
     * @return true if current active downloads are less than the limit
     */
    suspend fun canStartDownload(): Boolean = mutex.withLock {
        val limit = getParallelLimit()
        val activeCount = _activeDownloads.value.size
        val canStart = activeCount < limit
        Log.d(TAG, "canStartDownload: active=$activeCount, limit=$limit, canStart=$canStart")
        return canStart
    }

    /**
     * Try to start a download
     * @param downloadId The download ID to start
     * @return true if download can start immediately, false if it should be queued
     */
    suspend fun tryStartDownload(downloadId: String): Boolean = mutex.withLock {
        val limit = getParallelLimit()
        val activeCount = _activeDownloads.value.size

        Log.d(TAG, "tryStartDownload: id=$downloadId, active=$activeCount, limit=$limit")

        return if (activeCount < limit) {
            // Can start immediately
            _activeDownloads.value = _activeDownloads.value + downloadId
            Log.d(TAG, "Download $downloadId started, now active: ${_activeDownloads.value.size}")
            true
        } else {
            // Queue this download - update status to QUEUED
            Log.d(TAG, "Download $downloadId queued (limit reached)")
            downloadDao.updateStatus(
                id = downloadId,
                status = DownloadStatus.QUEUED.name,
                updatedAt = System.currentTimeMillis()
            )
            false
        }
    }

    /**
     * Mark a download as completed (or failed/cancelled)
     * This frees up a slot for the next queued download
     * @param downloadId The download ID that completed
     */
    suspend fun onDownloadComplete(downloadId: String) = mutex.withLock {
        Log.d(TAG, "onDownloadComplete: $downloadId")
        _activeDownloads.value = _activeDownloads.value - downloadId
        Log.d(TAG, "Download $downloadId removed from active, now active: ${_activeDownloads.value.size}")

        // Start next queued download
        startNextQueuedDownload()
    }

    /**
     * Mark a download as paused
     * This frees up a slot for the next queued download
     * @param downloadId The download ID that was paused
     */
    suspend fun onDownloadPaused(downloadId: String) = mutex.withLock {
        Log.d(TAG, "onDownloadPaused: $downloadId")
        _activeDownloads.value = _activeDownloads.value - downloadId
        Log.d(TAG, "Download $downloadId paused, now active: ${_activeDownloads.value.size}")

        // Start next queued download
        startNextQueuedDownload()
    }

    /**
     * Start the next queued download (FIFO order)
     * Called internally after a download completes or is paused
     */
    private suspend fun startNextQueuedDownload() {
        val limit = getParallelLimit()
        val currentActive = _activeDownloads.value.size

        if (currentActive >= limit) {
            Log.d(TAG, "Still at limit, not starting next queued")
            return
        }

        // Get oldest queued download (FIFO - first in, first out)
        val queuedDownloads = downloadDao.getDownloadsByStatusOrderedByCreated(DownloadStatus.QUEUED.name)

        if (queuedDownloads.isEmpty()) {
            Log.d(TAG, "No queued downloads to start")
            return
        }

        // Start as many as we can up to the limit
        val slotsAvailable = limit - currentActive
        val downloadsToStart = queuedDownloads.take(slotsAvailable)

        downloadsToStart.forEach { download ->
            Log.d(TAG, "Starting queued download: ${download.fileName} (ID: ${download.id})")
            _activeDownloads.value = _activeDownloads.value + download.id

            // Update status and trigger download via service
            downloadDao.updateStatus(
                id = download.id,
                status = DownloadStatus.DOWNLOADING.name,
                updatedAt = System.currentTimeMillis()
            )

            // Start the download via DownloadService
            com.piumal.filedownloadmanager.data.download.DownloadService.startDownload(
                context,
                download.id
            )
        }
    }

    /**
     * Force refresh the queue (e.g., after settings change)
     * Called when parallel download limit changes
     */
    fun refreshQueue() {
        scope.launch {
            mutex.withLock {
                Log.d(TAG, "Refreshing queue after settings change")
                startNextQueuedDownload()
            }
        }
    }

    /**
     * Get count of currently active downloads
     */
    fun getActiveDownloadCount(): Int = _activeDownloads.value.size

    /**
     * Check if a specific download is currently active
     */
    fun isDownloadActive(downloadId: String): Boolean = _activeDownloads.value.contains(downloadId)

    /**
     * Clear all active downloads (for service restart scenarios)
     */
    suspend fun clearActiveDownloads() = mutex.withLock {
        Log.d(TAG, "Clearing all active downloads")
        _activeDownloads.value = emptySet()
    }

    /**
     * Sync active downloads with database (for app restart scenarios)
     * Marks any DOWNLOADING items as QUEUED if they're not actually running
     */
    suspend fun syncWithDatabase() = mutex.withLock {
        Log.d(TAG, "Syncing active downloads with database")

        // Get all downloads marked as DOWNLOADING in database
        val downloadingItems = downloadDao.getDownloadsByStatusOrderedByCreated(DownloadStatus.DOWNLOADING.name)

        // If we have downloading items but no active tracking, they may have been interrupted
        // Mark them as QUEUED so they can restart properly
        downloadingItems.forEach { download ->
            if (!_activeDownloads.value.contains(download.id)) {
                Log.d(TAG, "Found orphaned downloading item: ${download.id}, marking as QUEUED")
                downloadDao.updateStatus(
                    id = download.id,
                    status = DownloadStatus.QUEUED.name,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }

        // Try to start queued downloads
        startNextQueuedDownload()
    }
}

