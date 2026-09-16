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
import com.piumal.filedownloadmanager.util.Logger
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
        try {
            System.loadLibrary("sqlcipher")
        } catch (e: UnsatisfiedLinkError) {
            // Don't let a missing/incompatible native lib for this device's ABI take down the
            // whole app before anything else has a chance to run. The Room/SQLCipher database
            // will fail when it's actually opened (see AppModule.provideDownloadDatabase), which
            // surfaces as a narrower, more diagnosable failure than a startup crash here.
            Logger.e(TAG, "Failed to load sqlcipher native library", e)
        }
        Logger.d(TAG, "Application onCreate() - Hilt initialized")

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Logger.d(TAG, "=== Network AVAILABLE: $network ===")
                Logger.d(TAG, "isNetworkLost was: $isNetworkLost")

                if (isNetworkLost) {
                    Logger.d(TAG, "Network was lost before - scheduling download retry")
                    isNetworkLost = false

                    // Use handler to add delay before retrying
                    handler.postDelayed({
                        resumeFailedDownloads()
                    }, RETRY_DELAY_MS)
                }
            }

            override fun onLost(network: Network) {
                Logger.d(TAG, "=== Network LOST: $network ===")

                // Check if there's still an active network
                val activeNetwork = connectivityManager.activeNetwork
                val capabilities = if (activeNetwork != null) {
                    connectivityManager.getNetworkCapabilities(activeNetwork)
                } else null

                val stillConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                if (!stillConnected) {
                    Logger.d(TAG, "No active network - marking as lost")
                    isNetworkLost = true
                } else {
                    Logger.d(TAG, "Still have active network connection")
                }
            }

            override fun onUnavailable() {
                Logger.d(TAG, "=== Network UNAVAILABLE ===")
                isNetworkLost = true
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            Logger.d(TAG, "Network callback registered successfully")

            // Check initial network state
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = if (activeNetwork != null) {
                connectivityManager.getNetworkCapabilities(activeNetwork)
            } else null

            isNetworkLost = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true ||
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) != true

            Logger.d(TAG, "Initial network state - isNetworkLost: $isNetworkLost")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to register network callback", e)
        }
    }

    private fun resumeFailedDownloads() {
        Logger.d(TAG, "=== Resuming failed downloads ===")
        try {
            val intent = Intent(this, DownloadService::class.java).apply {
                action = DownloadService.ACTION_RESUME_ALL_PENDING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Logger.d(TAG, "Successfully triggered resume all pending downloads")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to start download service for retry", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
                Logger.d(TAG, "Network callback unregistered")
            } catch (e: Exception) {
                Logger.e(TAG, "Error unregistering network callback", e)
            }
        }
    }
}
