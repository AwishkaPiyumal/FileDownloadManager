package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for sorting downloads.
 */
@Singleton
class SortDownloadsUseCase @Inject constructor() {
    operator fun invoke(items: List<DownloadItem>, option: SortOption): List<DownloadItem> {
        return when (option) {
            SortOption.DATE_DESC -> items.sortedByDescending { it.createdAt }
            SortOption.DATE_ASC -> items.sortedBy { it.createdAt }
            SortOption.NAME_ASC -> items.sortedBy { it.fileName }
            SortOption.NAME_DESC -> items.sortedByDescending { it.fileName }
            SortOption.SIZE_ASC -> items.sortedBy { it.totalSize }
            SortOption.SIZE_DESC -> items.sortedByDescending { it.totalSize }
        }
    }
}

enum class SortOption {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    NAME_DESC,
    SIZE_ASC,
    SIZE_DESC
}
