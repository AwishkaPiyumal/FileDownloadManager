package com.piumal.filedownloadmanager.data.local.dao

import androidx.room.*
import com.piumal.filedownloadmanager.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Download operations
 *
 * Data Access Object for database operations
 * Uses Flow for reactive data observation
 *
 * @author File Download Manager Team
 */
@Dao
interface DownloadDao {

    /**
     * Get all downloads ordered by creation date (newest first)
     */
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    /**
     * Get downloads by status
     */
    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>

    /**
     * Get single download by ID
     */
    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadEntity?

    /**
     * Insert new download
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    /**
     * Update existing download
     */
    @Update
    suspend fun updateDownload(download: DownloadEntity)

    /**
     * Delete download by ID
     */
    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)

    /**
     * Delete all downloads
     */
    @Query("DELETE FROM downloads")
    suspend fun deleteAllDownloads()

    /**
     * Get download count
     */
    @Query("SELECT COUNT(*) FROM downloads")
    suspend fun getDownloadCount(): Int

    /**
     * Update download progress
     */
    @Query("UPDATE downloads SET downloadedSize = :downloadedSize, totalSize = :totalSize, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProgress(id: String, downloadedSize: Long, totalSize: Long, updatedAt: Long)

    /**
     * Update download status
     */
    @Query("UPDATE downloads SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)
}

