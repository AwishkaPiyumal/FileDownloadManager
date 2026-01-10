package com.piumal.filedownloadmanager

import android.app.Application
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.piumal.filedownloadmanager.data.download.DownloadService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FileDownloadApplication : Application() {

    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isNetworkLost = false
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "FileDownloadApp"
        private const val RETRY_DELAY_MS = 3000L
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate() - Hilt initialized")

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "=== Network AVAILABLE: $network ===")
                Log.d(TAG, "isNetworkLost was: $isNetworkLost")

                if (isNetworkLost) {
                    Log.d(TAG, "Network was lost before - scheduling download retry")
                    isNetworkLost = false

                    // Use handler to add delay before retrying
                    handler.postDelayed({
                        resumeFailedDownloads()
                    }, RETRY_DELAY_MS)
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "=== Network LOST: $network ===")

                // Check if there's still an active network
                val activeNetwork = connectivityManager.activeNetwork
                val capabilities = if (activeNetwork != null) {
                    connectivityManager.getNetworkCapabilities(activeNetwork)
                } else null

                val stillConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                if (!stillConnected) {
                    Log.d(TAG, "No active network - marking as lost")
                    isNetworkLost = true
                } else {
                    Log.d(TAG, "Still have active network connection")
                }
            }

            override fun onUnavailable() {
                Log.d(TAG, "=== Network UNAVAILABLE ===")
                isNetworkLost = true
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            Log.d(TAG, "Network callback registered successfully")

            // Check initial network state
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = if (activeNetwork != null) {
                connectivityManager.getNetworkCapabilities(activeNetwork)
            } else null

            isNetworkLost = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true ||
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) != true

            Log.d(TAG, "Initial network state - isNetworkLost: $isNetworkLost")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    private fun resumeFailedDownloads() {
        Log.d(TAG, "=== Resuming failed downloads ===")
        try {
            val intent = Intent(this, DownloadService::class.java).apply {
                action = DownloadService.ACTION_RESUME_ALL_PENDING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Log.d(TAG, "Successfully triggered resume all pending downloads")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start download service for retry", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
                Log.d(TAG, "Network callback unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering network callback", e)
            }
        }
    }
}