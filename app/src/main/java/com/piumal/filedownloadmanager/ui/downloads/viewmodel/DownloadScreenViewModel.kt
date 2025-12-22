package com.piumal.filedownloadmanager.ui.downloads.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.usecase.DownloadFilterType
import com.piumal.filedownloadmanager.domain.usecase.FilterDownloadsUseCase
import com.piumal.filedownloadmanager.domain.usecase.SortDownloadsUseCase
import com.piumal.filedownloadmanager.ui.downloads.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for DownloadScreen
 * Manages UI state and business logic
 * Follows MVVM architecture pattern
 * Uses Clean Architecture with Use Cases
 */
@HiltViewModel
class DownloadScreenViewModel @Inject constructor(
    private val sortDownloadsUseCase: SortDownloadsUseCase,
    private val filterDownloadsUseCase: FilterDownloadsUseCase
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow(DownloadScreenUiState())

    // Public immutable state for UI observation
    val uiState: StateFlow<DownloadScreenUiState> = _uiState.asStateFlow()

    /**
     * Initialize with sample downloads
     * In production, this would fetch from repository
     */
    init {
        loadSampleDownloads()
    }

    /**
     * Set the filter type (All, Active, Completed)
     * Automatically updates the displayed downloads
     */
    fun setFilterType(filterType: DownloadFilterType) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    selectedFilter = filterType,
                    displayedDownloads = getFilteredAndSortedDownloads(
                        allDownloads = currentState.allDownloads,
                        filterType = filterType,
                        sortOption = currentState.selectedSortOption
                    )
                )
            }
        }
    }

    /**
     * Set the sort option
     * Automatically updates the displayed downloads for all filter states
     */
    fun setSortOption(sortOption: SortOption) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    selectedSortOption = sortOption,
                    displayedDownloads = getFilteredAndSortedDownloads(
                        allDownloads = currentState.allDownloads,
                        filterType = currentState.selectedFilter,
                        sortOption = sortOption
                    )
                )
            }
        }
    }

    /**
     * Show sort bottom sheet
     */
    fun showSortSheet() {
        _uiState.update { it.copy(showSortSheet = true) }
    }

    /**
     * Hide sort bottom sheet
     */
    fun hideSortSheet() {
        _uiState.update { it.copy(showSortSheet = false) }
    }

    /**
     * Show add download dialog
     */
    fun showAddDownloadDialog() {
        _uiState.update { it.copy(showAddDownloadDialog = true) }
    }

    /**
     * Hide add download dialog
     */
    fun hideAddDownloadDialog() {
        _uiState.update { it.copy(showAddDownloadDialog = false) }
    }

    /**
     * Handle more options click for a download item
     */
    fun onDownloadItemMoreClick(item: DownloadItem) {
        // TODO: Show options menu for this item
        // This will be implemented when we add item actions
    }

    /**
     * Apply filter and sort to downloads list
     * This is the core logic that combines both operations
     */
    private fun getFilteredAndSortedDownloads(
        allDownloads: List<DownloadItem>,
        filterType: DownloadFilterType,
        sortOption: SortOption
    ): List<DownloadItem> {
        // Step 1: Filter downloads based on selected filter
        val filteredDownloads = filterDownloadsUseCase(allDownloads, filterType)

        // Step 2: Sort filtered downloads based on selected sort option
        val sortedDownloads = sortDownloadsUseCase(filteredDownloads, sortOption)

        return sortedDownloads
    }

    /**
     * Load sample downloads for testing
     * In production, this would be replaced with repository call
     */
    private fun loadSampleDownloads() {
        viewModelScope.launch {
            val samples = getSampleDownloads()
            _uiState.update { currentState ->
                currentState.copy(
                    allDownloads = samples,
                    displayedDownloads = getFilteredAndSortedDownloads(
                        allDownloads = samples,
                        filterType = currentState.selectedFilter,
                        sortOption = currentState.selectedSortOption
                    ),
                    isLoading = false
                )
            }
        }
    }

    /**
     * Get sample downloads for UI testing
     * TODO: Replace with actual repository implementation
     */
    private fun getSampleDownloads(): List<DownloadItem> {
        return listOf(
            DownloadItem(
                id = "1",
                fileName = "video_tutorial_android_jetpack_compose.mp4",
                downloadedSize = 25 * 1024 * 1024, // 25 MB
                totalSize = 100 * 1024 * 1024,     // 100 MB
                status = com.piumal.filedownloadmanager.domain.model.DownloadStatus.DOWNLOADING,
                url = "https://example.com/video.mp4",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/",
                createdAt = System.currentTimeMillis() - 3600000 // 1 hour ago
            ),
            DownloadItem(
                id = "2",
                fileName = "document_presentation.pdf",
                downloadedSize = 50 * 1024 * 1024, // 50 MB
                totalSize = 50 * 1024 * 1024,      // 50 MB
                status = com.piumal.filedownloadmanager.domain.model.DownloadStatus.COMPLETED,
                url = "https://example.com/document.pdf",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/",
                createdAt = System.currentTimeMillis() - 7200000 // 2 hours ago
            ),
            DownloadItem(
                id = "3",
                fileName = "large_archive_backup.zip",
                downloadedSize = 150 * 1024 * 1024, // 150 MB
                totalSize = 500 * 1024 * 1024,      // 500 MB
                status = com.piumal.filedownloadmanager.domain.model.DownloadStatus.PAUSED,
                url = "https://example.com/archive.zip",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/",
                createdAt = System.currentTimeMillis() - 1800000 // 30 minutes ago
            ),
            DownloadItem(
                id = "4",
                fileName = "image_collection.jpg",
                downloadedSize = 2 * 1024 * 1024, // 2 MB
                totalSize = 10 * 1024 * 1024,     // 10 MB
                status = com.piumal.filedownloadmanager.domain.model.DownloadStatus.FAILED,
                url = "https://example.com/image.jpg",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/",
                createdAt = System.currentTimeMillis() - 10800000 // 3 hours ago
            ),
            DownloadItem(
                id = "5",
                fileName = "software_installer.exe",
                downloadedSize = 80 * 1024 * 1024,  // 80 MB
                totalSize = 200 * 1024 * 1024,      // 200 MB
                status = com.piumal.filedownloadmanager.domain.model.DownloadStatus.DOWNLOADING,
                url = "https://example.com/software.exe",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/",
                createdAt = System.currentTimeMillis() - 900000 // 15 minutes ago
            ),
            DownloadItem(
                id = "6",
                fileName = "music_album.mp3",
                downloadedSize = 120 * 1024 * 1024, // 120 MB
                totalSize = 120 * 1024 * 1024,      // 120 MB
                status = com.piumal.filedownloadmanager.domain.model.DownloadStatus.COMPLETED,
                url = "https://example.com/music.mp3",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/",
                createdAt = System.currentTimeMillis() - 14400000 // 4 hours ago
            )
        )
    }
}

/**
 * UI State for Download Screen
 * Immutable data class representing all UI state
 */
data class DownloadScreenUiState(
    val allDownloads: List<DownloadItem> = emptyList(),
    val displayedDownloads: List<DownloadItem> = emptyList(),
    val selectedFilter: DownloadFilterType = DownloadFilterType.ALL,
    val selectedSortOption: SortOption = SortOption.DATE_DESC,
    val showSortSheet: Boolean = false,
    val showAddDownloadDialog: Boolean = false,
    val isLoading: Boolean = true
)

