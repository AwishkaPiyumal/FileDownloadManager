package com.piumal.filedownloadmanager

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FileDownloadApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("FileDownloadApp", "Application onCreate() called - Hilt initialized")
    }
}