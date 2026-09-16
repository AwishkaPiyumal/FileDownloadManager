package com.piumal.filedownloadmanager.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.piumal.filedownloadmanager.util.Logger
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.piumal.filedownloadmanager.data.download.DownloadManager
import com.piumal.filedownloadmanager.data.download.DownloadNotificationHelper
import com.piumal.filedownloadmanager.data.download.DownloadQueueManager
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.usecase.settings.ObserveNotifyDownloadCompletionUseCase
import com.piumal.filedownloadmanager.domain.usecase.settings.ObserveNotifyDownloadFailureUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.piumal.filedownloadmanager.storage.StorageManager
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var storageManager: StorageManager
// ...

    @Inject
    lateinit var downloadDao: DownloadDao

    @Inject
    lateinit var downloadQueueManager: DownloadQueueManager

    @Inject
    lateinit var observerNotifyCompletionUseCase: ObserveNotifyDownloadCompletionUseCase

    @Inject
    lateinit var observerNotifyFailureUseCase: ObserveNotifyDownloadFailureUseCase

    @Inject
    lateinit var observeVibrateUseCase: com.piumal.filedownloadmanager.domain.usecase.settings.ObserveVibrateUseCase

    @Inject
    lateinit var observeLightUseCase: com.piumal.filedownloadmanager.domain.usecase.settings.ObserveLightUseCase

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeDownloads = mutableMapOf<String, Job>()
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationHelper: DownloadNotificationHelper

    @Volatile
    private var notifyCompletionEnabled: Boolean = true

    @Volatile
    private var notifyFailureEnabled: Boolean = true

    @Volatile
    private var vibrateEnabled: Boolean = true

    @Volatile
    private var lightEnabled: Boolean = true

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

        private const val REQUEST_CODE_PAUSE = 100
        private const val REQUEST_CODE_RESUME = 101
        private const val REQUEST_CODE_CANCEL = 102

        fun startDownload(context: Context, downloadId: String) {
            Logger.d(TAG, "DownloadService.startDownload() CALLED with ID: $downloadId")
            try {
                val intent = Intent(context, DownloadService::class.java).apply {
                    action = ACTION_START_DOWNLOAD
                    putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                }
                Logger.d(TAG, "Intent created, starting service...")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val result = context.startForegroundService(intent)
                    Logger.d(TAG, "startForegroundService returned: $result")
                } else {
                    val result = context.startService(intent)
                    Logger.d(TAG, "startService returned: $result")
                }
                Logger.d(TAG, "Service start command sent successfully")
            } catch (e: Exception) {
                Logger.e(TAG, "EXCEPTION starting service", e)
            }
        }

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
        Logger.d(TAG, "=== Service onCreate() called ===")
        try {
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            Logger.d(TAG, "NotificationManager obtained")
            notificationHelper = DownloadNotificationHelper(this, storageManager)
            Logger.d(TAG, "NotificationHelper initialized")
            serviceScope.launch {
                try {
                    observerNotifyCompletionUseCase().collect { enabled ->
                        notifyCompletionEnabled = enabled
                        Logger.d(TAG, "notifyCompletionEnabled updated: $enabled")
                    }
                } catch (e: Exception) {
                    Logger.d(TAG, "notify completion observer cancelled (expected on service shutdown)")
                }
            }
            serviceScope.launch {
                try {
                    observerNotifyFailureUseCase().collect { enabled ->
                        notifyFailureEnabled = enabled
                        Logger.d(TAG, "notifyFailureEnabled updated: $enabled")
                    }
                } catch (e: Exception) {
                    Logger.d(TAG, "notify failure observer cancelled (expected on service shutdown)")
                }
            }
            serviceScope.launch {
                try {
                    observeVibrateUseCase().collect { enabled ->
                        vibrateEnabled = enabled
                        Logger.d(TAG, "vibrateEnabled updated: $enabled")
                    }
                } catch (e: Exception) {
                    Logger.d(TAG, "vibrate observer cancelled (expected on service shutdown)")
                }
            }
            serviceScope.launch {
                try {
                    observeLightUseCase().collect { enabled ->
                        lightEnabled = enabled
                        Logger.d(TAG, "lightEnabled updated: $enabled")
                    }
                } catch (e: Exception) {
                    Logger.d(TAG, "light observer cancelled (expected on service shutdown)")
                }
            }

            // Reconcile any downloads left stuck in DOWNLOADING status from a previous
            // process/service death (OOM kill, force-stop, dataSync foreground timeout, etc).
            // Without this, resumeAllPendingDownloads() never picks them up again because it
            // only queries PAUSED/QUEUED/FAILED, so they'd show a frozen progress bar forever.
            serviceScope.launch {
                try {
                    downloadQueueManager.syncWithDatabase()
                    Logger.d(TAG, "Synced queue state with database on service start")
                } catch (e: Exception) {
                    Logger.e(TAG, "Error syncing queue with database on start", e)
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error initializing service components", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(TAG, "=== onStartCommand called ===")
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                Logger.d(TAG, "ACTION_START_DOWNLOAD - Download ID: $downloadId")
                if (downloadId != null) startDownload(downloadId)
            }
            ACTION_PAUSE_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                if (downloadId != null) pauseDownload(downloadId)
            }
            ACTION_RESUME_DOWNLOAD, ACTION_RETRY_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                if (downloadId != null) startDownload(downloadId)
            }
            ACTION_CANCEL_DOWNLOAD -> {
                val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
                if (downloadId != null) cancelDownload(downloadId)
            }
            ACTION_RESUME_ALL_PENDING -> {
                resumeAllPendingDownloads()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Android 15+ (API 35+): dataSync foreground services get a 6-hour budget per rolling
    // 24-hour window while the app is backgrounded. When it's exhausted, the system calls this
    // and expects stopSelf() within a few seconds - if we don't call it, the OS throws an
    // uncatchable ForegroundServiceDidNotStopInTimeException that kills the whole app process,
    // and any later attempt to re-enter foreground that day throws
    // ForegroundServiceStartNotAllowedException instead of starting. This override is additive:
    // on API < 35 the platform never calls it, so behavior there is unchanged. Reopening the app
    // (bringing it to the foreground) resets the 6-hour budget, so downloads can resume normally
    // afterwards; see onDestroy()/syncWithDatabase() for how in-flight state is reconciled.
    override fun onTimeout(startId: Int, fgsType: Int) {
        super.onTimeout(startId, fgsType)
        Logger.w(TAG, "onTimeout(): dataSync 6h/24h background budget exhausted (fgsType=$fgsType) - stopping service")
        isForegroundStarted = false
        foregroundDownloadId = null
        stopSelf()
    }

    override fun onDestroy() {
        Logger.d(TAG, "Service destroyed")
        NotificationManagerCompat.from(this).cancel(FOREGROUND_NOTIFICATION_ID)
        if (isForegroundStarted) {
            try {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } catch (_: Exception) {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN
            )
            notificationManager.createNotificationChannel(channel)
            Logger.d(TAG, "Notification channel created")
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setShowWhen(false)
            .setContentTitle("Download Manager")
            .setContentText("Preparing downloads...")
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setProgress(0, 0, true)
            .build()
    }

     private var isForegroundStarted = false
     private var foregroundDownloadId: String? = null

     private fun startDownload(downloadId: String) {
        Logger.d(TAG, "=== startDownload called with ID: $downloadId ===")
        activeDownloads[downloadId]?.cancel()
        val job = serviceScope.launch {
            try {
                val canStart = downloadQueueManager.tryStartDownload(downloadId)
                if (!canStart) {
                    Logger.d(TAG, "Download $downloadId queued - parallel limit reached")
                    activeDownloads.remove(downloadId)
                    checkAndStopService()
                    return@launch
                }
                val downloadEntity = downloadDao.getDownloadById(downloadId)
                if (downloadEntity == null) {
                    Logger.e(TAG, "Download not found: $downloadId")
                    delay(100)
                    val retryEntity = downloadDao.getDownloadById(downloadId)
                    if (retryEntity == null) {
                        Logger.e(TAG, "Download still not found after retry: $downloadId")
                        downloadQueueManager.onDownloadComplete(downloadId)
                        activeDownloads.remove(downloadId)
                        checkAndStopService()
                        return@launch
                    }
                }
                val downloadItem = downloadEntity!!.toDomainModel()
                if (!isForegroundStarted) {
                    try {
                        createNotificationChannel()
                        // IMPORTANT: the foreground-service anchor notification must use its own
                        // fixed, dedicated ID (FOREGROUND_NOTIFICATION_ID) - never a per-download
                        // notification ID. Per-download progress/completed/failed notifications are
                        // posted separately (see DownloadNotificationHelper, keyed by
                        // getNotificationIdForDownload) via plain notify() as progress flows in below.
                        // Reusing the same ID for both used to mean stopForeground(REMOVE) in
                        // checkAndStopService() would delete the just-shown "Download Complete"
                        // notification the instant the last active download finished.
                        val notification = createForegroundNotification()
                        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
                        foregroundDownloadId = downloadId
                        isForegroundStarted = true
                        Logger.d(TAG, "Service started in foreground")
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error starting foreground", e)
                    }
                }
                downloadManager.downloadFile(downloadItem)
                    .catch { e ->
                        Logger.e(TAG, "Download error", e)
                        downloadDao.updateStatus(downloadId, DownloadStatus.FAILED.name, System.currentTimeMillis())

                        notificationHelper.showFailedNotification(
                            downloadId, 
                            downloadItem.fileName, 
                            e.message,
                            vibrateEnabled,
                            lightEnabled
                        )
                    }
                    .collect { progress ->
                        downloadDao.updateProgress(
                            downloadId,
                            progress.downloadedBytes,
                            progress.totalBytes,
                            System.currentTimeMillis()
                        )
                        if (progress.status != downloadItem.status) {
                            downloadDao.updateStatus(
                                downloadId,
                                progress.status.name,
                                System.currentTimeMillis()
                            )
                        }
                        val percentage = if (progress.totalBytes > 0) {
                            ((progress.downloadedBytes.toFloat() / progress.totalBytes.toFloat()) * 100).toInt()
                        } else {
                            0
                        }
                        when (progress.status) {
                            DownloadStatus.DOWNLOADING -> {
                                Logger.d(TAG, "STATUS: DOWNLOADING - $percentage%")
                                // Don't show progress notification when nearly complete (95%+)
                                // This prevents showing 95-99% that doesn't reflect completion state
                                if (percentage < 95) {
                                    notificationHelper.showProgressNotification(
                                        downloadId,
                                        downloadItem.fileName,
                                        progress.downloadedBytes,
                                        progress.totalBytes,
                                        percentage,
                                        DownloadStatus.DOWNLOADING
                                    )
                                } else {
                                    Logger.d(TAG, "Download at $percentage% - not updating notification (close to completion)")
                                }
                            }
                            DownloadStatus.COMPLETED -> {
                                Logger.d(TAG, "=== DOWNLOAD COMPLETED: $downloadId ===")
                                Logger.d(TAG, "notifyCompletionEnabled: $notifyCompletionEnabled")
                                
                                // Small delay to ensure settings are fully propagated
                                delay(100)

                                if (notifyCompletionEnabled) {
                                    Logger.d(TAG, "ACTION: Showing COMPLETION notification")


                                    // Show completion notification to replace progress notification
                                    notificationHelper.showCompletedNotification(
                                        downloadId,
                                        downloadItem.fileName,
                                        downloadItem.filePath,
                                        vibrateEnabled,
                                        lightEnabled
                                    )
                                } else {
                                    Logger.d(TAG, "ACTION: Toggle OFF - Removing notification completely from panel")
                                    try {
                                        val notificationId = notificationHelper.getNotificationIdForDownload(downloadId)
                                        Logger.d(TAG, "Cancelling notification ID: $notificationId")
                                        // NOTE: do not call stopForeground() here - other downloads may
                                        // still be active. checkAndStopService() in the finally block
                                        // below is the single source of truth for whether the service
                                        // should leave the foreground state.
                                        notificationManager.cancel(notificationId)
                                        Logger.d(TAG, "Notification removed from panel successfully")
                                    } catch (e: Exception) {
                                        Logger.e(TAG, "ERROR removing notification: ${e.message}", e)
                                    }
                                }
                                downloadQueueManager.onDownloadComplete(downloadId)
                                activeDownloads.remove(downloadId)
                                Logger.d(TAG, "Download marked complete and removed from active set")
                            }
                            DownloadStatus.FAILED -> {
                                Logger.d(TAG, "=== DOWNLOAD FAILED: $downloadId ===")
                                Logger.d(TAG, "notifyFailureEnabled: $notifyFailureEnabled")

                                // Small delay to ensure settings are fully propagated
                                delay(100)

                                if (notifyFailureEnabled) {
                                    notificationHelper.showFailedNotification(
                                        downloadId,
                                        downloadItem.fileName,
                                        progress.error,
                                        vibrateEnabled,
                                        lightEnabled
                                    )
                                }
                                downloadQueueManager.onDownloadComplete(downloadId)
                                activeDownloads.remove(downloadId)
                            }
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                Logger.e(TAG, "Download exception", e)
                downloadDao.updateStatus(downloadId, DownloadStatus.FAILED.name, System.currentTimeMillis())
                downloadQueueManager.onDownloadComplete(downloadId)
                activeDownloads.remove(downloadId)
            } finally {
                checkAndStopService()
            }
        }
        activeDownloads[downloadId] = job
    }

    private fun pauseDownload(downloadId: String) {
        Logger.d(TAG, "=== PAUSE DOWNLOAD CALLED ===")
        activeDownloads[downloadId]?.cancel()
        activeDownloads.remove(downloadId)
        serviceScope.launch {
            try {
                downloadQueueManager.onDownloadPaused(downloadId)
                val downloadEntity = downloadDao.getDownloadById(downloadId)
                if (downloadEntity != null) {
                    val downloadItem = downloadEntity.toDomainModel()
                    val percentage = if (downloadItem.totalSize > 0) {
                        ((downloadItem.downloadedSize.toFloat() / downloadItem.totalSize.toFloat()) * 100).toInt()
                    } else {
                        0
                    }
                    downloadDao.updateStatus(downloadId, DownloadStatus.PAUSED.name, System.currentTimeMillis())
                    notificationHelper.showProgressNotification(
                        downloadId,
                        downloadItem.fileName,
                        downloadItem.downloadedSize,
                        downloadItem.totalSize,
                        percentage,
                        DownloadStatus.PAUSED
                    )
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error updating pause status", e)
            }
            checkAndStopService()
        }
    }

    private fun cancelDownload(downloadId: String) {
        Logger.d(TAG, "=== CANCEL DOWNLOAD CALLED: $downloadId ===")
        activeDownloads[downloadId]?.cancel()
        activeDownloads.remove(downloadId)
        serviceScope.launch {
            // Update database status to CANCELLED
            downloadDao.updateStatus(downloadId, DownloadStatus.CANCELLED.name, System.currentTimeMillis())
            downloadQueueManager.onDownloadComplete(downloadId)
            
            // Remove notification
            notificationManager.cancel(notificationHelper.getNotificationIdForDownload(downloadId))
            checkAndStopService()
        }
    }

    private fun resumeAllPendingDownloads() {
        serviceScope.launch {
            try {
                Logger.d(TAG, "Waiting 2 seconds for network to stabilize...")
                delay(2000)
                val pendingDownloads = downloadDao.getDownloadsByStatuses(
                    listOf(
                        DownloadStatus.PAUSED.name,
                        DownloadStatus.QUEUED.name,
                        DownloadStatus.FAILED.name
                    )
                )
                Logger.d(TAG, "Found ${pendingDownloads.size} pending downloads")
                if (pendingDownloads.isEmpty()) {
                    Logger.d(TAG, "No downloads to resume")
                    checkAndStopService()
                    return@launch
                }

                if (!isForegroundStarted) {
                    try {
                        createNotificationChannel()
                        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
                        isForegroundStarted = true
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error starting foreground for pending downloads", e)
                    }
                }

                pendingDownloads.forEach { download ->
                    Logger.d(TAG, "Retrying: ${download.id}")
                    downloadDao.updateStatus(download.id, DownloadStatus.QUEUED.name, System.currentTimeMillis())
                    startDownload(download.id)
                    delay(500)
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error in resumeAllPendingDownloads", e)
            }
        }
    }

    private fun checkAndStopService() {
        if (activeDownloads.isEmpty()) {
            Logger.d(TAG, "No active downloads, stopping service")
            isForegroundStarted = false
            foregroundDownloadId = null
            NotificationManagerCompat.from(this).cancel(FOREGROUND_NOTIFICATION_ID)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }
}


