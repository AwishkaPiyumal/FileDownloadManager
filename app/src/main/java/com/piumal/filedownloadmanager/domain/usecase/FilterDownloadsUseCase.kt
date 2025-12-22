package com.piumal.filedownloadmanager.domain.usecase

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import javax.inject.Inject

/**
 * Use case for filtering download items by status
 * Implements Clean Architecture principles
 * Follows Single Responsibility Principle
 */
class FilterDownloadsUseCase @Inject constructor() {

    /**
     * Filter download items based on filter type
     * @param downloads List of all download items
     * @param filterType Type of filter (ALL, ACTIVE, COMPLETED)
     * @return Filtered list of download items
     */
    operator fun invoke(
        downloads: List<DownloadItem>,
        filterType: DownloadFilterType
    ): List<DownloadItem> {
        return when (filterType) {
            DownloadFilterType.ALL -> downloads
            DownloadFilterType.ACTIVE -> filterActive(downloads)
            DownloadFilterType.COMPLETED -> filterCompleted(downloads)
        }
    }

    /**
     * Filter only active downloads (downloading or queued)
     * @param downloads List of all downloads
     * @return List of active downloads
     */
    private fun filterActive(downloads: List<DownloadItem>): List<DownloadItem> {
        return downloads.filter {
            it.status == DownloadStatus.DOWNLOADING ||
            it.status == DownloadStatus.QUEUED
        }
    }

    /**
     * Filter only completed downloads
     * @param downloads List of all downloads
     * @return List of completed downloads
     */
    private fun filterCompleted(downloads: List<DownloadItem>): List<DownloadItem> {
        return downloads.filter {
            it.status == DownloadStatus.COMPLETED
        }
    }
}

/**
 * Enum representing download filter types
 */
enum class DownloadFilterType {
    ALL,
    ACTIVE,
    COMPLETED
}

