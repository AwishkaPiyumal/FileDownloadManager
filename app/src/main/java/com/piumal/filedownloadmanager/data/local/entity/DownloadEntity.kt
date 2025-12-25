package com.piumal.filedownloadmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus

/**
 * Room Database Entity for Download Item
 *
 * Data layer entity that maps to domain model
 * Follows Clean Architecture separation
 *
 * @author File Download Manager Team
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val downloadedSize: Long,
    val totalSize: Long,
    val status: String, // Stored as String for Room compatibility
    val url: String,
    val filePath: String,
    val createdAt: Long,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Convert Entity to Domain Model
     */
    fun toDomainModel(): DownloadItem {
        return DownloadItem(
            id = id,
            fileName = fileName,
            downloadedSize = downloadedSize,
            totalSize = totalSize,
            status = DownloadStatus.valueOf(status),
            url = url,
            filePath = filePath,
            createdAt = createdAt
        )
    }

    companion object {
        /**
         * Convert Domain Model to Entity
         */
        fun fromDomainModel(downloadItem: DownloadItem): DownloadEntity {
            return DownloadEntity(
                id = downloadItem.id,
                fileName = downloadItem.fileName,
                downloadedSize = downloadItem.downloadedSize,
                totalSize = downloadItem.totalSize,
                status = downloadItem.status.name,
                url = downloadItem.url,
                filePath = downloadItem.filePath,
                createdAt = downloadItem.createdAt
            )
        }
    }
}

