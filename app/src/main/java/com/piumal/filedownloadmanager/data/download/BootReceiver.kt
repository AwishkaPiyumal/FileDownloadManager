package com.piumal.filedownloadmanager.data.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BootReceiver - Broadcast Receiver
 *
 * Purpose:
 * - Automatically resumes downloads after device reboot
 * - Ensures scheduled downloads are restarted
 * - Critical for user experience (downloads don't get lost on reboot)
 *
 * How it works:
 * 1. Android sends BOOT_COMPLETED broadcast when device starts
 * 2. This receiver catches the broadcast
 * 3. Starts DownloadService to resume pending/paused downloads
 * 4. DownloadService checks database for incomplete downloads and resumes them
 *
 * Google Play Policy Compliance:
 * - Uses standard Android API
 * - Only accesses app's own data
 * - Doesn't drain battery (only runs once at boot)
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        Log.d(TAG, "Boot completed, action: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                // Device has booted, resume all pending downloads
                Log.d(TAG, "Resuming pending downloads after boot")
                DownloadService.resumeAllPending(context)
            }
        }
    }
}

