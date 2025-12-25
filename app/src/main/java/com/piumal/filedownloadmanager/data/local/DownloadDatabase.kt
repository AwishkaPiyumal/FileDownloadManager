package com.piumal.filedownloadmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.piumal.filedownloadmanager.data.local.dao.DownloadDao
import com.piumal.filedownloadmanager.data.local.entity.DownloadEntity

/**
 * Room Database for Download Manager
 *
 * Stores download history and state
 * Google Play Policy: Transparent data storage
 *
 * @author File Download Manager Team
 */
@Database(
    entities = [DownloadEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DownloadDatabase : RoomDatabase() {

    /**
     * Get Download DAO
     */
    abstract fun downloadDao(): DownloadDao

    companion object {
        const val DATABASE_NAME = "download_manager_db"
    }
}

