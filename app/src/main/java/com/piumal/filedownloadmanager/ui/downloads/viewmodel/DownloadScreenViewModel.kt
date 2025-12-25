package com.piumal.filedownloadmanager.ui.downloads.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piumal.filedownloadmanager.domain.model.DownloadConfig
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
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
     */
    fun startDownload(config: DownloadConfig) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadInProgress = true, downloadError = null) }

            startDownloadUseCase(
                url = config.url,
                fileName = config.fileName,
                destinationPath = config.filePath,
                scheduleTime = config.scheduleTime
            ).collect { result ->
                result.onSuccess { downloadItem ->
                    // Download started successfully
                    val message = if (config.scheduleTime != null) {
                        "Download scheduled: ${downloadItem.fileName}"
                    } else {
                        "Download started: ${downloadItem.fileName}"
                    }

                    _uiState.update {
                        it.copy(
                            isDownloadInProgress = false,
                            downloadSuccess = true,
                            successMessage = message
                        )
                    }
                    // Clear success message after 3 seconds
                    kotlinx.coroutines.delay(3000)
                    _uiState.update { it.copy(downloadSuccess = false, successMessage = null) }
                }.onFailure { error ->
                    // Download failed to start
                    _uiState.update {
                        it.copy(
                            isDownloadInProgress = false,
                            downloadError = error.message ?: "Failed to start download"
                        )
                    }
                }
            }
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
    val isLoading: Boolean = true,
    val isDownloadInProgress: Boolean = false,
    val downloadSuccess: Boolean = false,
    val successMessage: String? = null,
    val downloadError: String? = null
)

