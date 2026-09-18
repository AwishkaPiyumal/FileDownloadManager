package com.piumal.filedownloadmanager.domain.util

import android.os.Environment
import java.io.File

/**
 * Canonical shared download storage location used across the app.
 *
 * Downloads are stored in the public Downloads collection so they are visible
 * in the device's file manager and match the file paths persisted to Room.
 */
object DownloadStoragePaths {

    const val DOWNLOAD_FOLDER_NAME = "File Download Manager"
    const val DEFAULT_UI_FOLDER_LABEL = "Download/File Download Manager"

    fun getDownloadDirectory(): File {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            DOWNLOAD_FOLDER_NAME
        )
    }

    fun getDownloadFile(fileName: String): File {
        return File(getDownloadDirectory(), fileName)
    }

    fun getDownloadFilePath(fileName: String): String {
        return getDownloadFile(fileName).absolutePath
    }
}
