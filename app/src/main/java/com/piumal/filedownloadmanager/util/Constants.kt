package com.piumal.filedownloadmanager.util

@Suppress("unused")
object Constants {
    const val DATABASE_NAME = "file_download_db"
    const val DOWNLOADS_TABLE = "downloads"
    val BASE_URL: String
        get() = runCatching {
            Class.forName("com.piumal.filedownloadmanager.BuildConfig")
                .getField("API_BASE_URL")
                .get(null) as String
        }.getOrDefault("")
}
