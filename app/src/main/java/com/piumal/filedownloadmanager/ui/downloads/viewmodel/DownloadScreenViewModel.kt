package com.piumal.filedownloadmanager.ui.downloads.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piumal.filedownloadmanager.domain.model.DownloadConfig
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import com.piumal.filedownloadmanager.domain.usecase.DeleteSelectedDownloadsUseCase
import com.piumal.filedownloadmanager.domain.usecase.DownloadFilterType
import com.piumal.filedownloadmanager.domain.usecase.FilterDownloadsUseCase
import com.piumal.filedownloadmanager.domain.usecase.SortDownloadsUseCase
import com.piumal.filedownloadmanager.domain.usecase.StartDownloadUseCase
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
 *
 * UPDATED: Integrated with real download functionality
 */
@HiltViewModel
class DownloadScreenViewModel @Inject constructor(
    private val sortDownloadsUseCase: SortDownloadsUseCase,
    private val filterDownloadsUseCase: FilterDownloadsUseCase,
    private val startDownloadUseCase: StartDownloadUseCase,
    private val deleteSelectedDownloadsUseCase: DeleteSelectedDownloadsUseCase,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow(DownloadScreenUiState())

    // Public immutable state for UI observation
    val uiState: StateFlow<DownloadScreenUiState> = _uiState.asStateFlow()

    /**
     * Initialize - observe downloads from repository
     */
    init {
        observeDownloads()
    }

    /**
     * Observe downloads from repository (real-time updates)
     */
    private fun observeDownloads() {
        viewModelScope.launch {
            downloadRepository.getAllDownloads().collect { downloads ->
                _uiState.update { currentState ->
                    currentState.copy(
                        allDownloads = downloads,
                        displayedDownloads = getFilteredAndSortedDownloads(
                            allDownloads = downloads,
                            filterType = currentState.selectedFilter,
                            sortOption = currentState.selectedSortOption
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Start a new download
     * Called when user confirms download in AddDownloadDialog
     *
     * @param config Download configuration from dialog (includes scheduleTime)
     * @param forceNewDownload If true, forces new download with renamed file
     */
    fun startDownload(config: DownloadConfig, forceNewDownload: Boolean = false) {
        Log.d("DownloadScreenVM", "=== startDownload called ===")
        Log.d("DownloadScreenVM", "URL: ${config.url}")
        Log.d("DownloadScreenVM", "FileName: ${config.fileName}")
        Log.d("DownloadScreenVM", "FilePath: ${config.filePath}")
        Log.d("DownloadScreenVM", "ForceNewDownload: $forceNewDownload")

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadInProgress = true, downloadError = null) }
            Log.d("DownloadScreenVM", "Starting download use case...")

            startDownloadUseCase(
                url = config.url,
                fileName = config.fileName,
                destinationPath = config.filePath,
                scheduleTime = config.scheduleTime,
                forceNewDownload = forceNewDownload
            ).collect { result ->
                Log.d("DownloadScreenVM", "Received result from use case")
                result.onSuccess { downloadItem ->
                    // Download started successfully
                    Log.d("DownloadScreenVM", "Download started successfully: ID=${downloadItem.id}, Status=${downloadItem.status}")
                    val message = if (config.scheduleTime != null) {
                        "Download scheduled: ${downloadItem.fileName}"
                    } else {
                        "Download started: ${downloadItem.fileName}"
                    }

                    Log.d("DownloadScreenVM", "Success message: $message")

                    _uiState.update {
                        it.copy(
                            isDownloadInProgress = false,
                            downloadSuccess = true,
                            successMessage = message,
                            showFileExistsDialog = false,
                            pendingDownloadConfig = null,
                            existingFileName = null
                        )
                    }
                    Log.d("DownloadScreenVM", "UI state updated with success")
                    // Clear success message after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    _uiState.update { it.copy(downloadSuccess = false, successMessage = null) }
                }.onFailure { error ->
                    // Download failed to start
                    Log.e("DownloadScreenVM", "Download failed to start", error)

                    // Check if it's a FileAlreadyExistsException
                    if (error is com.piumal.filedownloadmanager.domain.usecase.FileAlreadyExistsException) {
                        // Show file exists dialog
                        Log.d("DownloadScreenVM", "File already exists, showing confirmation dialog")
                        _uiState.update {
                            it.copy(
                                isDownloadInProgress = false,
                                showFileExistsDialog = true,
                                existingFileName = error.fileName,
                                pendingDownloadConfig = config
                            )
                        }
                    } else {
                        // Other error - show error message
                        val errorMessage = error.message ?: "Failed to start download"
                        _uiState.update {
                            it.copy(
                                isDownloadInProgress = false,
                                downloadError = errorMessage
                            )
                        }
                        Log.d("DownloadScreenVM", "UI state updated with error: ${error.message}")
                    }
                }
            }
            Log.d("DownloadScreenVM", "=== startDownload completed ===")
        }
    }

    /**
     * Handle user clicking Continue on file exists dialog
     * Downloads the file again with a renamed filename
     */
    fun continueDownloadWithRename() {
        val config = _uiState.value.pendingDownloadConfig
        if (config != null) {
            Log.d("DownloadScreenVM", "Continuing download with rename")
            startDownload(config, forceNewDownload = true)
        }
    }

    /**
     * Handle user clicking Cancel on file exists dialog
     */
    fun dismissFileExistsDialog() {
        _uiState.update {
            it.copy(
                showFileExistsDialog = false,
                existingFileName = null,
                pendingDownloadConfig = null
            )
        }
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
     * Pause a download
     */
    fun pauseDownload(downloadId: String) {
        viewModelScope.launch {
            try {
                downloadRepository.pauseDownload(downloadId)
                Log.d("DownloadScreenVM", "Download paused: $downloadId")
            } catch (e: Exception) {
                Log.e("DownloadScreenVM", "Error pausing download", e)
            }
        }
    }

    /**
     * Resume a paused download
     */
    fun resumeDownload(downloadId: String) {
        viewModelScope.launch {
            try {
                downloadRepository.resumeDownload(downloadId)
                Log.d("DownloadScreenVM", "Download resumed: $downloadId")
            } catch (e: Exception) {
                Log.e("DownloadScreenVM", "Error resuming download", e)
            }
        }
    }

    /**
     * Retry a failed download
     */
    fun retryDownload(downloadId: String) {
        viewModelScope.launch {
            try {
                downloadRepository.retryDownload(downloadId)
                Log.d("DownloadScreenVM", "Download retried: $downloadId")
            } catch (e: Exception) {
                Log.e("DownloadScreenVM", "Error retrying download", e)
            }
        }
    }

    /**
     * Handle download item more options click
     */
    fun onDownloadItemMoreClick(item: DownloadItem) {
        // TODO: Show options menu for this item
        // This will be implemented when we add item actions
    }

    // =============================================
    // SELECTION MODE FUNCTIONS
    // =============================================

    /**
     * Enter selection mode
     * Called when user clicks "Select" from More Options Menu
     * or long presses a download item
     */
    fun enterSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = true) }
        Log.d("DownloadScreenVM", "Entered selection mode")
    }

    /**
     * Exit selection mode and clear all selections
     * Called when user clicks close button in selection header
     */
    fun exitSelectionMode() {
        _uiState.update {
            it.copy(
                isSelectionMode = false,
                selectedDownloadIds = emptySet()
            )
        }
        Log.d("DownloadScreenVM", "Exited selection mode")
    }

    /**
     * Toggle selection of a single download item
     * @param downloadId ID of the download to toggle
     */
    fun toggleSelection(downloadId: String) {
        _uiState.update { currentState ->
            val currentSelection = currentState.selectedDownloadIds
            val newSelection = if (currentSelection.contains(downloadId)) {
                currentSelection - downloadId
            } else {
                currentSelection + downloadId
            }

            // If no items are selected after toggle, exit selection mode
            if (newSelection.isEmpty()) {
                currentState.copy(
                    isSelectionMode = false,
                    selectedDownloadIds = emptySet()
                )
            } else {
                currentState.copy(selectedDownloadIds = newSelection)
            }
        }
        Log.d("DownloadScreenVM", "Toggled selection for: $downloadId")
    }

    /**
     * Handle long press on download item
     * Enters selection mode and selects the item
     * @param downloadId ID of the long-pressed download
     */
    fun onItemLongPress(downloadId: String) {
        _uiState.update { currentState ->
            if (!currentState.isSelectionMode) {
                // Enter selection mode and select this item
                currentState.copy(
                    isSelectionMode = true,
                    selectedDownloadIds = setOf(downloadId)
                )
            } else {
                // Already in selection mode, just toggle
                val newSelection = if (currentState.selectedDownloadIds.contains(downloadId)) {
                    currentState.selectedDownloadIds - downloadId
                } else {
                    currentState.selectedDownloadIds + downloadId
                }

                if (newSelection.isEmpty()) {
                    currentState.copy(
                        isSelectionMode = false,
                        selectedDownloadIds = emptySet()
                    )
                } else {
                    currentState.copy(selectedDownloadIds = newSelection)
                }
            }
        }
        Log.d("DownloadScreenVM", "Long pressed item: $downloadId")
    }

    /**
     * Select all currently displayed downloads
     */
    fun selectAll() {
        _uiState.update { currentState ->
            val allIds = currentState.displayedDownloads.map { it.id }.toSet()
            currentState.copy(
                isSelectionMode = true,
                selectedDownloadIds = allIds
            )
        }
        Log.d("DownloadScreenVM", "Selected all downloads")
    }

    /**
     * Deselect all downloads
     */
    fun deselectAll() {
        _uiState.update { it.copy(selectedDownloadIds = emptySet()) }
        Log.d("DownloadScreenVM", "Deselected all downloads")
    }

    // =============================================
    // DELETE SELECTED FUNCTIONS
    // =============================================

    /**
     * Show delete selected confirmation dialog
     * Called when user clicks delete button in selection header
     */
    fun showDeleteSelectedDialog() {
        if (_uiState.value.selectedDownloadIds.isNotEmpty()) {
            _uiState.update { it.copy(showDeleteSelectedDialog = true) }
            Log.d("DownloadScreenVM", "Showing delete selected dialog")
        }
    }

    /**
     * Dismiss delete selected confirmation dialog
     */
    fun dismissDeleteSelectedDialog() {
        _uiState.update { it.copy(showDeleteSelectedDialog = false) }
        Log.d("DownloadScreenVM", "Dismissed delete selected dialog")
    }

    /**
     * Confirm and execute deletion of selected downloads
     * Deletes files from storage and removes from database
     */
    fun confirmDeleteSelected() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedDownloadIds
            Log.d("DownloadScreenVM", "Confirming delete of ${selectedIds.size} items")

            // Hide dialog first
            _uiState.update { it.copy(showDeleteSelectedDialog = false) }

            // Execute deletion
            deleteSelectedDownloadsUseCase(selectedIds).fold(
                onSuccess = { deletedCount ->
                    Log.d("DownloadScreenVM", "Successfully deleted $deletedCount downloads")
                    // Exit selection mode after successful deletion
                    _uiState.update {
                        it.copy(
                            isSelectionMode = false,
                            selectedDownloadIds = emptySet(),
                            downloadSuccess = true,
                            successMessage = "Deleted $deletedCount ${if (deletedCount == 1) "file" else "files"}"
                        )
                    }
                    // Clear success message after delay
                    kotlinx.coroutines.delay(3000)
                    _uiState.update { it.copy(downloadSuccess = false, successMessage = null) }
                },
                onFailure = { error ->
                    Log.e("DownloadScreenVM", "Failed to delete downloads: ${error.message}")
                    _uiState.update {
                        it.copy(
                            downloadError = "Failed to delete files: ${error.message}"
                        )
                    }
                }
            )
        }
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
    val showFileExistsDialog: Boolean = false,
    val showDeleteSelectedDialog: Boolean = false,
    val existingFileName: String? = null,
    val pendingDownloadConfig: DownloadConfig? = null,
    val isLoading: Boolean = true,
    val isDownloadInProgress: Boolean = false,
    val downloadSuccess: Boolean = false,
    val successMessage: String? = null,
    val downloadError: String? = null,
    // Selection mode states
    val isSelectionMode: Boolean = false,
    val selectedDownloadIds: Set<String> = emptySet()
) {
    /**
     * Get count of selected items
     */
    val selectedCount: Int get() = selectedDownloadIds.size

    /**
     * Check if a download item is selected
     */
    fun isSelected(downloadId: String): Boolean = selectedDownloadIds.contains(downloadId)
}

