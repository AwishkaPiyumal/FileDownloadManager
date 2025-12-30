package com.piumal.filedownloadmanager.data.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

/**
 * NetworkChangeReceiver - Broadcast Receiver
 *
 * Purpose:
 * - Monitors network connectivity changes
 * - Automatically resumes downloads when network becomes available
 * - Handles scenarios: WiFi reconnect, mobile data on/off, airplane mode
 *
 * How it works:
 * 1. Android broadcasts CONNECTIVITY_CHANGE when network status changes
 * 2. This receiver checks if network is now available
 * 3. If available, resumes all paused/pending downloads
 * 4. Provides seamless experience (user doesn't need to manually resume)
 *
 * Scenarios handled:
 * - WiFi disconnected → reconnected
 * - Airplane mode off → on
 * - Mobile data turned back on
 * - Network lost during download → auto-resume when restored
 *
 * Google Play Policy Compliance:
 * - Uses standard Android connectivity APIs
 * - Only resumes user-initiated downloads
 * - Respects network type (can be extended to WiFi-only)
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NetworkChangeReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        // Check action string for security
        if (intent.action != "android.net.conn.CONNECTIVITY_CHANGE") {
            return
        }

        Log.d(TAG, "Network change detected")

        // Check if network is now available
        if (isNetworkAvailable(context)) {
            Log.d(TAG, "Network is available, resuming downloads")
            // Resume all pending downloads
            DownloadService.resumeAllPending(context)
        } else {
            Log.d(TAG, "Network is not available")
            // Note: Downloads will automatically pause when network fails
            // DownloadManager handles IOException and marks as FAILED
            // User can manually retry, or they'll auto-resume when network returns
        }
    }

    /**
     * Check if network is available
     *
     * @param context Application context
     * @return true if network is connected, false otherwise
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        // Android 6.0+ (API 23+) - minSdk is 24, so always use modern API
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Check if connected via WiFi, Cellular, or Ethernet
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}

