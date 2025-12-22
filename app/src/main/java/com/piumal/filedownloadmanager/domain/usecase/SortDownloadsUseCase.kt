package com.piumal.filedownloadmanager.domain.usecase

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.ui.downloads.components.SortCategory
import com.piumal.filedownloadmanager.ui.downloads.components.SortOption
import com.piumal.filedownloadmanager.ui.downloads.components.SortOrder
import javax.inject.Inject

/**
 * Use case for sorting download items
 * Implements Clean Architecture principles
 * Follows Single Responsibility Principle
 */
class SortDownloadsUseCase @Inject constructor() {

    /**
     * Sort download items based on the selected sort option
     * @param downloads List of download items to sort
     * @param sortOption The sort option (category and order)
     * @return Sorted list of download items
     */
    operator fun invoke(
        downloads: List<DownloadItem>,
        sortOption: SortOption
    ): List<DownloadItem> {
        // Create a sorted copy of the list based on category
        val sortedList = when (sortOption.category) {
            SortCategory.DATE -> sortByDate(downloads, sortOption.order)
            SortCategory.NAME -> sortByName(downloads, sortOption.order)
            SortCategory.SIZE -> sortBySize(downloads, sortOption.order)
        }
        return sortedList
    }

    /**
     * Sort downloads by creation date
     * @param downloads List of downloads
     * @param order Ascending (oldest first) or Descending (newest first)
     * @return Sorted list
     */
    private fun sortByDate(
        downloads: List<DownloadItem>,
        order: SortOrder
    ): List<DownloadItem> {
        return when (order) {
            SortOrder.ASCENDING -> downloads.sortedBy { it.createdAt }
            SortOrder.DESCENDING -> downloads.sortedByDescending { it.createdAt }
        }
    }

    /**
     * Sort downloads by file name
     * @param downloads List of downloads
     * @param order Ascending (A-Z) or Descending (Z-A)
     * @return Sorted list
     */
    private fun sortByName(
        downloads: List<DownloadItem>,
        order: SortOrder
    ): List<DownloadItem> {
        return when (order) {
            SortOrder.ASCENDING -> downloads.sortedBy { it.fileName.lowercase() }
            SortOrder.DESCENDING -> downloads.sortedByDescending { it.fileName.lowercase() }
        }
    }

    /**
     * Sort downloads by file size
     * @param downloads List of downloads
     * @param order Ascending (smallest first) or Descending (largest first)
     * @return Sorted list
     */
    private fun sortBySize(
        downloads: List<DownloadItem>,
        order: SortOrder
    ): List<DownloadItem> {
        return when (order) {
            SortOrder.ASCENDING -> downloads.sortedBy { it.totalSize }
            SortOrder.DESCENDING -> downloads.sortedByDescending { it.totalSize }
        }
    }
}

