package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduled Download Manager Use Case
 *
 * Manages scheduled downloads using coroutines with delay
 * Follows MVVM and Clean Code Architecture
 *
 * Background Download Support:
 * - Uses Repository for actual downloads (which handles service internally)
 * - Downloads persist through app closure
 * - Survives device reboot (downloads restart via BootReceiver)
 *
 * This is a simplified implementation for scheduling.
 * Production apps should use WorkManager for reliability.
 */
@Singleton
class ScheduledDownloadManager @Inject constructor(
    private val repository: DownloadRepository
) {
    private val scheduledJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Schedule a download to start at specific time
     *
     * @param downloadItem The download to schedule
     * @param scheduleTime Timestamp when download should start
     */
    fun scheduleDownload(downloadItem: DownloadItem, scheduleTime: Long) {
        val currentTime = System.currentTimeMillis()
        val delay = scheduleTime - currentTime

        if (delay > 0) {
            // Cancel any existing schedule for this download
            cancelSchedule(downloadItem.id)

            // Schedule the download
            val job = scope.launch {
                // Wait until scheduled time
                delay(delay)

                // Start the download at scheduled time via repository
                try {
                    repository.startDownload(downloadItem)
                } catch (e: Exception) {
                    // Handle error - could log or notify user
                    e.printStackTrace()
                } finally {
                    // Remove from scheduled jobs
                    scheduledJobs.remove(downloadItem.id)
                }
            }

            // Store the job
            scheduledJobs[downloadItem.id] = job
        } else {
            // Time already passed, start immediately
            scope.launch {
                repository.startDownload(downloadItem)
            }
        }
    }

    /**
     * Cancel a scheduled download
     *
     * @param downloadId ID of the download to cancel
     */
    fun cancelSchedule(downloadId: String) {
        scheduledJobs[downloadId]?.cancel()
        scheduledJobs.remove(downloadId)
    }

    /**
     * Check if a download is scheduled
     */
    fun isScheduled(downloadId: String): Boolean {
        return scheduledJobs.containsKey(downloadId)
    }

    /**
     * Get count of scheduled downloads
     */
    fun getScheduledCount(): Int = scheduledJobs.size

    /**
     * Cancel all scheduled downloads
     */
    fun cancelAllSchedules() {
        scheduledJobs.values.forEach { it.cancel() }
        scheduledJobs.clear()
    }
}