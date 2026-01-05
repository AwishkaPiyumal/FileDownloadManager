package com.piumal.filedownloadmanager.data.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * NetworkChangeReceiver - Broadcast Receiver
 *
 * Monitors network connectivity changes and automatically retries failed downloads.
 *
 * Handles:
 * - WiFi to Mobile Data switching
 * - Mobile Data to WiFi switching
 * - SIM 1 to SIM 2 switching
 * - Different WiFi network connections
 * - Connection loss and restoration
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NetworkChangeReceiver"
        private var wasNetworkAvailable = false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action != "android.net.conn.CONNECTIVITY_CHANGE") {
            return
        }

        Log.d(TAG, "Network change detected")

        val isCurrentlyAvailable = isNetworkAvailable(context)

        if (isCurrentlyAvailable && !wasNetworkAvailable) {
            Log.d(TAG, "Network restored - resuming failed and pending downloads")
            DownloadService.resumeAllPending(context)
        } else if (!isCurrentlyAvailable && wasNetworkAvailable) {
            Log.d(TAG, "Network lost - downloads may fail")
        } else if (isCurrentlyAvailable) {
            Log.d(TAG, "Network type changed (still connected) - resuming failed downloads")
            DownloadService.resumeAllPending(context)
        }

        wasNetworkAvailable = isCurrentlyAvailable
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

