package com.piumal.filedownloadmanager.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * DownloadService - Foreground Service
 *
 * Purpose:
 * - Runs downloads in background even when app is closed
 * - Shows persistent notification during downloads
 * - Survives app closure and screen lock
 * - Handles multiple simultaneous downloads
 *
 */
@AndroidEntryPoint
class DownloadService : Service() {

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var downloadDao: DownloadDao

    @Inject
    lateinit var downloadQueueManager: DownloadQueueManager

    @Inject
    lateinit var observerNotifyCompletionUseCase: com.piumal.filedownloadmanager.domain.usecase.settings.ObserveNotifyDownloadCompletionUseCase

    @Inject
    lateinit var observerNotifyFailureUseCase: com.piumal.filedownloadmanager.domain.usecase.settings.ObserveNotifyDownloadFailureUseCase

    // Coroutine scope for service
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Track active download jobs
    private val activeDownloads = mutableMapOf<String, Job>()

    // Notification manager
    private lateinit var notificationManager: NotificationManager

    // Notification helper for download notifications
    private lateinit var notificationHelper: DownloadNotificationHelper

    // Cached notification preference flags (updated from settings repository)
    @Volatile
    private var notifyCompletionEnabled: Boolean = true

    @Volatile
    private var notifyFailureEnabled: Boolean = true

    companion object {
        private const val TAG = "DownloadService"
        private const val NOTIFICATION_CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Downloads"
        private const val FOREGROUND_NOTIFICATION_ID = 1001

        const val ACTION_START_DOWNLOAD = "ACTION_START_DOWNLOAD"
        const val ACTION_PAUSE_DOWNLOAD = "ACTION_PAUSE_DOWNLOAD"
        const val ACTION_RESUME_DOWNLOAD = "ACTION_RESUME_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD"
        const val ACTION_RESUME_ALL_PENDING = "ACTION_RESUME_ALL_PENDING"
        const val ACTION_RETRY_DOWNLOAD = "ACTION_RETRY_DOWNLOAD"

        const val EXTRA_DOWNLOAD_ID = "EXTRA_DOWNLOAD_ID"

        // Notification request codes
        private const val REQUEST_CODE_PAUSE = 100
        private const val REQUEST_CODE_RESUME = 101
        private const val REQUEST_CODE_CANCEL = 102

        /**
         * Helper method to start download from any context
         */
        fun startDownload(context: Context, downloadId: String) {
            Log.d(TAG, "DownloadService.startDownload() CALLED with ID: $downloadId")
            try {
                val intent = Intent(context, DownloadService::class.java).apply {
                    action = ACTION_START_DOWNLOAD
                    putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                }

                Log.d(TAG, "Intent created, starting service...")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val result = context.startForegroundService(intent)
                    Log.d(TAG, "startForegroundService returned: $result")
                } else {
                    val result = context.startService(intent)
                    Log.d(TAG, "startService returned: $result")
                }
                Log.d(TAG, "Service start command sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "EXCEPTION starting service", e)
            }
        }

        /**
         * Resume all pending downloads (called on boot or network restore)
         */
        fun resumeAllPending(context: Context) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_RESUME_ALL_PENDING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== Service onCreate() called ===")

        try {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Log.d(TAG, "NotificationManager obtained")

            // Initialize notification helper
            notificationHelper = DownloadNotificationHelper(this)
            Log.d(TAG, "NotificationHelper initialized")

            // Observe settings so we always have the latest preference without races
            serviceScope.launch {
                try {
                    observerNotifyCompletionUseCase().collect { enabled ->
                        notifyCompletionEnabled = enabled
                        Log.d(TAG, "notifyCompletionEnabled updated: $enabled")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error observing notify completion setting", e)
                }
            }

            serviceScope.launch {
                try {
                    observerNotifyFailureUseCase().collect { enabled ->
                        notifyFailureEnabled = enabled
                        Log.d(TAG, "notifyFailureEnabled updated: $enabled")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error observing notify failure setting", e)
                }
            }

            // Note: We'll start foreground when first download starts
            // No need to show service notification immediately
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing service components", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "=== onStartCommand called ===")
        Log.d(TAG, "Action: ${intent?.action}")
        Log.d(TAG, "Flags: $flags, StartId: $startId")
        Log.d(TAG, "Intent extras: ${intent?.extras}")

        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                Log.d(TAG, "ACTION_START_DOWNLOAD - Download ID: $downloadId")
                if (downloadId != null) {
                    startDownload(downloadId)
                } else {
                    Log.e(TAG, "Download ID is null!")
                }
            }
            ACTION_PAUSE_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                Log.d(TAG, "ACTION_PAUSE_DOWNLOAD - Download ID: $downloadId")
                Log.d(TAG, "PAUSE button clicked - processing immediately")
                if (downloadId != null) {
                    pauseDownload(downloadId)
                } else {
                    Log.e(TAG, "Download ID is null for pause!")
                }
            }
            ACTION_RESUME_DOWNLOAD, ACTION_RETRY_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                Log.d(TAG, "ACTION_RESUME/RETRY_DOWNLOAD - Download ID: $downloadId")
                Log.d(TAG, "RESUME/RETRY button clicked - processing immediately")
                if (downloadId != null) {
                    startDownload(downloadId)
                } else {
                    Log.e(TAG, "Download ID is null for resume!")
                }
            }
            ACTION_CANCEL_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                Log.d(TAG, "ACTION_CANCEL_DOWNLOAD - Download ID: $downloadId")
                if (downloadId != null) {
                    cancelDownload(downloadId)
                }
            }
            ACTION_RESUME_ALL_PENDING -> {
                Log.d(TAG, "ACTION_RESUME_ALL_PENDING - Network restored, retrying failed downloads")

                // Start foreground immediately to prevent service from being killed
                if (!isForegroundStarted) {
                    try {
                        createNotificationChannel()
                        val notification = createForegroundNotification()
                        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
                        isForegroundStarted = true
                        Log.d(TAG, "Foreground started for resume all pending")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting foreground for resume all", e)
                    }
                }

                resumeAllPendingDownloads()
            }
            else -> {
                Log.w(TAG, "Unknown or null action received: ${intent?.action}")
            }
        }

        // DON'T check and stop service here - let downloads start first
        // Service will stop when downloads complete (in startDownload finally block)

        // START_STICKY ensures service restarts if killed by system
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")

        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Create notification channel (required for Android 8+)
     * This channel has minimum importance to reduce visual disruption
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN // MIN importance - minimal visual disruption
            ).apply {
                description = "Download service"
                setShowBadge(false) // No badge
                enableVibration(false) // No vibration
                setSound(null, null) // No sound
                enableLights(false) // No LED light
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created with IMPORTANCE_MIN")
        }
    }

    /**
     * Create minimal foreground service notification
     * Required by Android for foreground service, but kept minimal
     * Will be immediately replaced by actual download notification
     */
    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setShowWhen(false)
            .setContentTitle("")
            .setContentText("")
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .build()
    }

    // Track if foreground is already started to prevent duplicate notifications
    private var isForegroundStarted = false

    // Store the current foreground notification download ID
    private var foregroundDownloadId: String? = null

    /**
     * Start a download
     * Respects parallel download limit via DownloadQueueManager
     */
    private fun startDownload(downloadId: String) {
        Log.d(TAG, "=== startDownload called with ID: $downloadId ===")

        // Cancel if already running
        activeDownloads[downloadId]?.cancel()

        val job = serviceScope.launch {
            try {
                // Check if we can start this download (parallel limit check)
                val canStart = downloadQueueManager.tryStartDownload(downloadId)
                if (!canStart) {
                    Log.d(TAG, "Download $downloadId queued - parallel limit reached")
                    // Download is queued, will be started automatically when a slot opens
                    activeDownloads.remove(downloadId)
                    checkAndStopService()
                    return@launch
                }

                Log.d(TAG, "Querying database for download ID: $downloadId")
                // Get download item from database
                val downloadEntity = downloadDao.getDownloadById(downloadId)

                if (downloadEntity == null) {
                    Log.e(TAG, "Download not found in database: $downloadId")
                    Log.e(TAG, "This might be a database timing issue. Download was just inserted.")
                    // Try one more time after a short delay
                    kotlinx.coroutines.delay(100)
                    val retryEntity = downloadDao.getDownloadById(downloadId)
                    if (retryEntity == null) {
                        Log.e(TAG, "Download still not found after retry: $downloadId")
                        downloadQueueManager.onDownloadComplete(downloadId)
                        activeDownloads.remove(downloadId)
                        checkAndStopService()
                        return@launch
                    }
                }

                Log.d(TAG, "Found download entity, converting to domain model")
                // Convert to domain model
                val downloadItem = downloadEntity!!.toDomainModel()

                Log.d(TAG, "Starting download: ${downloadItem.fileName} (URL: ${downloadItem.url})")

                // Start foreground service with actual download notification (not empty)
                // This prevents the extra empty notification from appearing
                if (!isForegroundStarted) {
                    try {
                        createNotificationChannel()
                        val notificationId = notificationHelper.getNotificationIdForDownload(downloadId)
                        val notification = notificationHelper.createProgressNotificationForForeground(
                            downloadId = downloadId,
                            fileName = downloadItem.fileName,
                            downloadedSize = 0,
                            totalSize = downloadItem.totalSize,
                            percentage = 0,
                            status = DownloadStatus.DOWNLOADING
                        )
                        startForeground(notificationId, notification)
                        foregroundDownloadId = downloadId
                        isForegroundStarted = true
                        Log.d(TAG, "Service started in foreground with download notification")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting foreground", e)
                    }
                }

                // Start download and collect progress
                downloadManager.downloadFile(downloadItem)
                    .catch { e ->
                        Log.e(TAG, "Download error", e)
                        downloadDao.updateStatus(
                            id = downloadId,
                            status = DownloadStatus.FAILED.name,
                            updatedAt = System.currentTimeMillis()
                        )
                        // Show failed notification
                        notificationHelper.showFailedNotification(
                            downloadId = downloadId,
                            fileName = downloadItem.fileName,
                            errorMessage = e.message
                        )
                    }
                    .collect { progress ->
                        // Update database with progress
                        downloadDao.updateProgress(
                            id = downloadId,
                            downloadedSize = progress.downloadedBytes,
                            totalSize = progress.totalBytes,
                            updatedAt = System.currentTimeMillis()
                        )

                        // Update status if changed
                        if (progress.status != downloadItem.status) {
                            downloadDao.updateStatus(
                                id = downloadId,
                                status = progress.status.name,
                                updatedAt = System.currentTimeMillis()
                            )
                        }

                        // Calculate percentage
                        val percentage = if (progress.totalBytes > 0) {
                            ((progress.downloadedBytes.toFloat() / progress.totalBytes.toFloat()) * 100).toInt()
                        } else {
                            0
                        }

                        // Update notification based on status
                        when (progress.status) {
                            DownloadStatus.DOWNLOADING -> {
                                // Show progress notification
                                notificationHelper.showProgressNotification(
                                    downloadId = downloadId,
                                    fileName = downloadItem.fileName,
                                    downloadedSize = progress.downloadedBytes,
                                    totalSize = progress.totalBytes,
                                    percentage = percentage,
                                    status = DownloadStatus.DOWNLOADING
                                )
                            }
                            DownloadStatus.COMPLETED -> {
                                // Show completion notification only if user has enabled it in settings
                                if (notifyCompletionEnabled) {
                                    notificationHelper.showCompletedNotification(
                                        downloadId = downloadId,
                                        fileName = downloadItem.fileName,
                                        filePath = downloadItem.filePath
                                    )
                                } else {
                                    Log.d(TAG, "Notify completion disabled by user; skipping notification for $downloadId")
                                    // Keep the progress notification visible but do not transition to a 'completed' notification.
                                    // Show a static progress/paused notification at 100% so the panel doesn't mark it completed.
                                    try {
                                        notificationHelper.showProgressNotification(
                                            downloadId = downloadId,
                                            fileName = downloadItem.fileName,
                                            downloadedSize = downloadItem.totalSize,
                                            totalSize = downloadItem.totalSize,
                                            percentage = 100,
                                            status = DownloadStatus.PAUSED
                                        )
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to update progress notification when completion notifications are disabled", e)
                                    }
                                }

                                // Notify queue manager to start next download
                                downloadQueueManager.onDownloadComplete(downloadId)
                                activeDownloads.remove(downloadId)
                            }
                            DownloadStatus.FAILED -> {
                                // Show failed notification only if user enabled it
                                if (notifyFailureEnabled) {
                                    notificationHelper.showFailedNotification(
                                        downloadId = downloadId,
                                        fileName = downloadItem.fileName,
                                        errorMessage = progress.error
                                    )
                                } else {
                                    Log.d(TAG, "Notify failure disabled by user; skipping failed notification for $downloadId")
                                }
                                // Notify queue manager to start next download
                                downloadQueueManager.onDownloadComplete(downloadId)
                                activeDownloads.remove(downloadId)
                            }
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Download exception", e)
                downloadDao.updateStatus(
                    id = downloadId,
                    status = DownloadStatus.FAILED.name,
                    updatedAt = System.currentTimeMillis()
                )
                // Notify queue manager to start next download
                downloadQueueManager.onDownloadComplete(downloadId)
                activeDownloads.remove(downloadId)
            } finally {
                checkAndStopService()
            }
        }

        activeDownloads[downloadId] = job
    }

    /**
     * Pause a download
     * Optimized for immediate response - notification stays visible with Resume button
     */
    private fun pauseDownload(downloadId: String) {
        Log.d(TAG, "=== PAUSE DOWNLOAD CALLED - Processing immediately ===")

        // STEP 1: Cancel the download job immediately (fast operation)
        activeDownloads[downloadId]?.cancel()
        activeDownloads.remove(downloadId)
        Log.d(TAG, "Download job cancelled immediately")

        // STEP 2: Update notification and database, and notify queue manager
        serviceScope.launch {
            try {
                // Notify queue manager that this download is paused (frees up a slot)
                downloadQueueManager.onDownloadPaused(downloadId)

                // Get download info for notification
                val downloadEntity = downloadDao.getDownloadById(downloadId)
                if (downloadEntity != null) {
                    val downloadItem = downloadEntity.toDomainModel()
                    val percentage = if (downloadItem.totalSize > 0) {
                        ((downloadItem.downloadedSize.toFloat() / downloadItem.totalSize.toFloat()) * 100).toInt()
                    } else {
                        0
                    }

                    // Update status in database
                    downloadDao.updateStatus(
                        id = downloadId,
                        status = DownloadStatus.PAUSED.name,
                        updatedAt = System.currentTimeMillis()
                    )
                    Log.d(TAG, "Database updated with PAUSED status")

                    // IMPORTANT: Show paused notification with Resume button
                    // This notification will persist even after service stops
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        notificationHelper.showProgressNotification(
                            downloadId = downloadId,
                            fileName = downloadItem.fileName,
                            downloadedSize = downloadItem.downloadedSize,
                            totalSize = downloadItem.totalSize,
                            percentage = percentage,
                            status = DownloadStatus.PAUSED
                        )
                        Log.d(TAG, "Notification updated to PAUSED with Resume button")
                    }
                } else {
                    Log.e(TAG, "Download entity not found: $downloadId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating pause status", e)
            }

            // Check if we should stop service AFTER notification is shown
            // This ensures notification is displayed before service potentially stops
            checkAndStopService()
        }
    }

    /**
     * Cancel a download
     */
    private fun cancelDownload(downloadId: String) {
        activeDownloads[downloadId]?.cancel()
        activeDownloads.remove(downloadId)

        serviceScope.launch {
            downloadDao.updateStatus(
                id = downloadId,
                status = DownloadStatus.FAILED.name,
                updatedAt = System.currentTimeMillis()
            )
            // Notify queue manager to start next download
            downloadQueueManager.onDownloadComplete(downloadId)
        }

        // Cancel notification
        notificationManager.cancel(downloadId.hashCode())

        checkAndStopService()
    }

    private fun resumeAllPendingDownloads() {
        Log.d(TAG, "=== resumeAllPendingDownloads called ===")

        serviceScope.launch {
            try {
                // Wait for network to stabilize before retrying
                Log.d(TAG, "Waiting 2 seconds for network to stabilize...")
                kotlinx.coroutines.delay(2000)

                val pendingDownloads = downloadDao.getDownloadsByStatuses(
                    listOf(
                        DownloadStatus.PAUSED.name,
                        DownloadStatus.QUEUED.name,
                        DownloadStatus.FAILED.name
                    )
                )

                Log.d(TAG, "Found ${pendingDownloads.size} pending/failed downloads to resume")

                if (pendingDownloads.isEmpty()) {
                    Log.d(TAG, "No downloads to resume")
                    checkAndStopService()
                    return@launch
                }

                pendingDownloads.forEach { download ->
                    Log.d(TAG, "Retrying download: ${download.fileName} (ID: ${download.id}, status: ${download.status})")

                    // Update status to QUEUED before starting
                    downloadDao.updateStatus(download.id, DownloadStatus.QUEUED.name, System.currentTimeMillis())

                    startDownload(download.id)
                    kotlinx.coroutines.delay(500)
                }

                Log.d(TAG, "Finished queuing all pending downloads for retry")
            } catch (e: Exception) {
                Log.e(TAG, "Error in resumeAllPendingDownloads", e)
            }
        }
    }

    /**
     * Format file size for display
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    // Old notification methods - Replaced by DownloadNotificationHelper
    // Kept for reference, can be removed after testing
    /*
    private fun showDownloadNotification(
        notificationId: Int,
        fileName: String,
        status: String,
        progress: Int,
        downloadId: String,
        currentStatus: DownloadStatus
    ) { ... }

    private fun showDownloadNotification(
        notificationId: Int,
        fileName: String,
        status: String,
        progress: Int
    ) { ... }
    */

    /**
     * Check if service should stop (no active downloads)
     * Uses STOP_FOREGROUND_DETACH to keep notifications visible
     */
    private fun checkAndStopService() {
        if (activeDownloads.isEmpty()) {
            Log.d(TAG, "No active downloads, stopping service")
            isForegroundStarted = false // Reset flag so next download can start foreground properly
            foregroundDownloadId = null // Reset foreground download ID

            // Use STOP_FOREGROUND_DETACH to keep existing notifications visible
            // This allows paused download notifications to remain in notification panel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(false) // false = don't remove notification
            }
            stopSelf()
        }
    }
}

