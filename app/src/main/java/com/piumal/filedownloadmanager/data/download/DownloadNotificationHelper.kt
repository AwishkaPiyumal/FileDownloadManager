package com.piumal.filedownloadmanager.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import com.piumal.filedownloadmanager.MainActivity
import com.piumal.filedownloadmanager.R
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import java.io.File

/**
 * DownloadNotificationHelper
 *
 * Handles all notification operations for downloads
 * Follows Clean Code Architecture - Single Responsibility
 *
 * Features:
 * - Progress notifications with live updates
 * - Completion notifications (clickable to open file)
 * - Failed notifications with retry option
 * - Pause/Resume action buttons
 * - User can dismiss notifications by swiping
 * - Works when app is open or closed
 *
 * MVVM Architecture:
 * - This is a helper in Data Layer
 * - Called by DownloadService
 * - No direct UI dependencies
 */
class DownloadNotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID_PROGRESS = "download_progress"
        private const val CHANNEL_ID_COMPLETE = "download_complete"
        private const val CHANNEL_ID_FAILED = "download_failed"

        private const val CHANNEL_NAME_PROGRESS = "Download Progress"
        private const val CHANNEL_NAME_COMPLETE = "Download Complete"
        private const val CHANNEL_NAME_FAILED = "Download Failed"

        // Base notification IDs
        private const val NOTIFICATION_ID_BASE = 2000

        // Action request codes
        private const val REQUEST_CODE_PAUSE = 100
        private const val REQUEST_CODE_RESUME = 101
        private const val REQUEST_CODE_RETRY = 102
        private const val REQUEST_CODE_OPEN = 103
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels for Android 8+
     * Separate channels for progress, complete, and failed states
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val alertAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            NotificationChannel(
                CHANNEL_ID_PROGRESS,
                CHANNEL_NAME_PROGRESS,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows download progress"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }.also(notificationManager::createNotificationChannel)

            NotificationChannel(
                CHANNEL_ID_COMPLETE,
                CHANNEL_NAME_COMPLETE,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when download completes"
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                enableLights(true)
                lightColor = -0x10000
                setSound(defaultSound, alertAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }.also(notificationManager::createNotificationChannel)

            NotificationChannel(
                CHANNEL_ID_FAILED,
                CHANNEL_NAME_FAILED,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when download fails"
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                enableLights(true)
                lightColor = -0x10000
                setSound(defaultSound, alertAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }.also(notificationManager::createNotificationChannel)
        }
    }

    /**
     * Show or update download progress notification
     *
     * @param downloadId Unique download identifier
     * @param fileName Name of the file being downloaded
     * @param downloadedSize Size downloaded in bytes
     * @param totalSize Total file size in bytes
     * @param percentage Download percentage (0-100)
     * @param status Current download status
     */
    fun showProgressNotification(
        downloadId: String,
        fileName: String,
        downloadedSize: Long,
        totalSize: Long,
        percentage: Int,
        status: DownloadStatus
    ) {
        android.util.Log.d("NotificationHelper", "showProgressNotification called: $fileName, $percentage%, Status: $status")

        val notificationId = getNotificationId(downloadId)

        // Format size text
        val sizeText = formatSize(downloadedSize, totalSize)
        val statusText = when (status) {
            DownloadStatus.DOWNLOADING -> "Downloading... $percentage% • $sizeText"
            DownloadStatus.PAUSED -> "Paused • $percentage% • $sizeText"
            else -> "$percentage% • $sizeText"
        }

        android.util.Log.d("NotificationHelper", "Notification ID: $notificationId, Text: $statusText")

        // Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PROGRESS)
            .setContentTitle(fileName)
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, percentage, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(status == DownloadStatus.DOWNLOADING) // Only ongoing when actively downloading
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)

        // Add content intent to open app
        val contentIntent = createOpenAppIntent()
        builder.setContentIntent(contentIntent)

        // Add action buttons based on status
        when (status) {
            DownloadStatus.DOWNLOADING -> {
                // Add Pause button with unique request code per download
                // Use IMMUTABLE flag for better performance (no need to update)
                val pauseIntent = createActionIntent(DownloadService.ACTION_PAUSE_DOWNLOAD, downloadId)
                val pausePendingIntent = PendingIntent.getService(
                    context,
                    downloadId.hashCode() + REQUEST_CODE_PAUSE, // Unique per download
                    pauseIntent,
                    PendingIntent.FLAG_IMMUTABLE // Immutable for faster processing
                )
                builder.addAction(
                    R.drawable.pause_circle_24px,
                    "Pause",
                    pausePendingIntent
                )
            }

            DownloadStatus.PAUSED -> {
                // Add Resume button with unique request code per download
                // Use IMMUTABLE flag for better performance
                val resumeIntent = createActionIntent(DownloadService.ACTION_RESUME_DOWNLOAD, downloadId)
                val resumePendingIntent = PendingIntent.getService(
                    context,
                    downloadId.hashCode() + REQUEST_CODE_RESUME, // Unique per download
                    resumeIntent,
                    PendingIntent.FLAG_IMMUTABLE // Immutable for faster processing
                )
                builder.addAction(
                    R.drawable.play_circle_24px,
                    "Resume",
                    resumePendingIntent
                )
            }

            else -> {}
        }

        // Show notification
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * Show download completed notification
     * Clicking opens the file with appropriate app
     *
     * @param downloadId Unique download identifier
     * @param fileName Name of the downloaded file
     * @param filePath Full path to the downloaded file
     * @param vibrateEnabled Whether to vibrate on completion
     * @param lightEnabled Whether to flash light on completion
     */
    fun showCompletedNotification(
        downloadId: String,
        fileName: String,
        filePath: String,
        vibrateEnabled: Boolean = true,
        lightEnabled: Boolean = true
    ) {
        android.util.Log.d("NotificationHelper", "showCompletedNotification called: $fileName at $filePath")

        val notificationId = getNotificationId(downloadId)

        // Create intent to open file
        val openIntent = createOpenFileIntent(filePath)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_COMPLETE)
            .setContentTitle("Download Complete")
            .setContentText(fileName)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Auto dismiss when clicked
            .setOngoing(false) // Not ongoing
            .setProgress(0, 0, false) // Remove progress bar completely
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openIntent)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder.setSound(defaultSound, AudioManager.STREAM_NOTIFICATION)
        
        if (vibrateEnabled) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }
        
        if (lightEnabled) {
            builder.setLights(-0x10000, 1000, 1000)
        }

        // Show notification - replaces existing notification with same ID
        android.util.Log.d("NotificationHelper", "Displaying completion notification ID: $notificationId")
        notificationManager.notify(notificationId, builder.build())
        android.util.Log.d("NotificationHelper", "Completion notification displayed")
    }

    /**
     * Show download failed notification
     * Includes retry button
     *
     * @param downloadId Unique download identifier
     * @param fileName Name of the file
     * @param errorMessage Error message (optional)
     * @param vibrateEnabled Whether to vibrate on failure
     * @param lightEnabled Whether to flash light on failure
     */
    fun showFailedNotification(
        downloadId: String,
        fileName: String,
        errorMessage: String? = null,
        vibrateEnabled: Boolean = true,
        lightEnabled: Boolean = true
    ) {
        android.util.Log.d("NotificationHelper", "showFailedNotification called: $fileName, Error: $errorMessage")

        val notificationId = getNotificationId(downloadId)

        val message = errorMessage?.let { "Failed: $it" } ?: "Download failed"

        // Create retry intent with unique request code
        val retryIntent = createActionIntent(DownloadService.ACTION_RETRY_DOWNLOAD, downloadId)
        val retryPendingIntent = PendingIntent.getService(
            context,
            downloadId.hashCode() + REQUEST_CODE_RETRY, // Unique per download
            retryIntent,
            PendingIntent.FLAG_IMMUTABLE // Immutable for faster processing
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_FAILED)
            .setContentTitle(fileName)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setOngoing(false)
            .setProgress(0, 0, false) // Remove progress bar
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.refresh_24px,
                "Retry",
                retryPendingIntent
            )

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder.setSound(defaultSound, AudioManager.STREAM_NOTIFICATION)
        
        if (vibrateEnabled) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }
        
        if (lightEnabled) {
            builder.setLights(-0x10000, 1000, 1000)
        }

        // Add content intent to open app
        val contentIntent = createOpenAppIntent()
        builder.setContentIntent(contentIntent)

        // Show notification
        android.util.Log.d("NotificationHelper", "Displaying failed notification ID: $notificationId")
        notificationManager.notify(notificationId, builder.build())
        android.util.Log.d("NotificationHelper", "Failed notification displayed")
    }

    /**
     * Cancel/dismiss a notification
     */
    fun cancelNotification(downloadId: String) {
        val notificationId = getNotificationId(downloadId)
        notificationManager.cancel(notificationId)
    }

    /**
     * Get the notification ID for a download
     * Exposed for foreground service usage
     */
    fun getNotificationIdForDownload(downloadId: String): Int {
        return getNotificationId(downloadId)
    }

    /**
     * Create a progress notification for use with foreground service
     * Returns the notification object instead of showing it
     */
    fun createProgressNotificationForForeground(
        downloadId: String,
        fileName: String,
        downloadedSize: Long,
        totalSize: Long,
        percentage: Int,
        status: DownloadStatus
    ): Notification {
        // Format size text
        val sizeText = formatSize(downloadedSize, totalSize)
        val statusText = when (status) {
            DownloadStatus.DOWNLOADING -> "Downloading... $percentage% • $sizeText"
            DownloadStatus.PAUSED -> "Paused • $percentage% • $sizeText"
            else -> "$percentage% • $sizeText"
        }

        // Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PROGRESS)
            .setContentTitle(fileName)
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, percentage, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(status == DownloadStatus.DOWNLOADING)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)

        // Add content intent to open app
        val contentIntent = createOpenAppIntent()
        builder.setContentIntent(contentIntent)

        // Add action buttons based on status
        when (status) {
            DownloadStatus.DOWNLOADING -> {
                val pauseIntent = createActionIntent(DownloadService.ACTION_PAUSE_DOWNLOAD, downloadId)
                val pausePendingIntent = PendingIntent.getService(
                    context,
                    downloadId.hashCode() + REQUEST_CODE_PAUSE,
                    pauseIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.pause_circle_24px,
                    "Pause",
                    pausePendingIntent
                )
            }
            DownloadStatus.PAUSED -> {
                val resumeIntent = createActionIntent(DownloadService.ACTION_RESUME_DOWNLOAD, downloadId)
                val resumePendingIntent = PendingIntent.getService(
                    context,
                    downloadId.hashCode() + REQUEST_CODE_RESUME,
                    resumeIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.play_circle_24px,
                    "Resume",
                    resumePendingIntent
                )
            }
            else -> {}
        }

        return builder.build()
    }

    /**
     * Generate consistent notification ID from download ID
     */
    private fun getNotificationId(downloadId: String): Int {
        return NOTIFICATION_ID_BASE + downloadId.hashCode()
    }

    /**
     * Create intent to open the app
     */
    private fun createOpenAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Create intent to open downloaded file
     */
    private fun createOpenFileIntent(filePath: String): PendingIntent {
        val file = File(filePath)

        // Check if file exists
        if (!file.exists()) {
            android.util.Log.e("NotificationHelper", "File does not exist: $filePath")
            // Return intent to open app instead
            return createOpenAppIntent()
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Get MIME type
        val mimeType = getMimeType(filePath) ?: "*/*"
        android.util.Log.d("NotificationHelper", "Opening file: $filePath, MIME: $mimeType")

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Create chooser to let user select app
        val chooserIntent = Intent.createChooser(intent, "Open with").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN,
            chooserIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Create service action intent
     */
    private fun createActionIntent(action: String, downloadId: String): Intent {
        return Intent(context, DownloadService::class.java).apply {
            this.action = action
            putExtra(DownloadService.EXTRA_DOWNLOAD_ID, downloadId)
        }
    }

    /**
     * Format file size for display
     */
    private fun formatSize(downloadedSize: Long, totalSize: Long): String {
        return if (totalSize > 0) {
            "${formatBytes(downloadedSize)} / ${formatBytes(totalSize)}"
        } else {
            "${formatBytes(downloadedSize)} / Unknown"
        }
    }

    /**
     * Convert bytes to human-readable format
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    /**
     * Get MIME type from file path
     */
    private fun getMimeType(filePath: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        return if (extension != null) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } else {
            null
        }
    }
}

