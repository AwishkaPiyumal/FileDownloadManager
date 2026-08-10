package com.piumal.filedownloadmanager.ui.downloads.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piumal.filedownloadmanager.domain.model.DownloadConfig
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import com.piumal.filedownloadmanager.domain.usecase.download.OpenDownloadUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.ShareDownloadUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.RenameDownloadUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.DeleteDownloadUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.DeleteSelectedDownloadsUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.DownloadFilterType
import com.piumal.filedownloadmanager.domain.usecase.download.FilterDownloadsUseCase
import com.piumal.filedownloadmanager.domain.usecase.settings.ObserveAutoRemoveCompletedUseCase
import com.piumal.filedownloadmanager.domain.usecase.settings.ObserveAutoRetryFailedUseCase
import com.piumal.filedownloadmanager.domain.usecase.settings.ObserveHiddenCompletedIdsUseCase
import com.piumal.filedownloadmanager.domain.usecase.settings.AddHiddenCompletedIdsUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.SortDownloadsUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.StartDownloadUseCase
import com.piumal.filedownloadmanager.ui.downloads.components.SortOption
import com.piumal.filedownloadmanager.ui.downloads.components.SortCategory
import com.piumal.filedownloadmanager.ui.downloads.components.SortOrder
import com.piumal.filedownloadmanager.domain.usecase.download.FileAlreadyExistsException
import com.piumal.filedownloadmanager.domain.usecase.download.MoveToUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.RemoveFromListUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.ShowInFolderUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.ShowInfoUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.SortOption as DomainSortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
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
    private val openDownloadUseCase: OpenDownloadUseCase,
    private val shareDownloadUseCase: ShareDownloadUseCase,
    private val renameDownloadUseCase: RenameDownloadUseCase,
    private val deleteDownloadUseCase: DeleteDownloadUseCase,
    private val showInFolderUseCase: ShowInFolderUseCase,
    private val showInfoUseCase: ShowInfoUseCase,
    private val moveToUseCase: MoveToUseCase,
    private val removeFromListUseCase: RemoveFromListUseCase,
    private val downloadRepository: DownloadRepository,
    private val observeAutoRemoveCompletedUseCase: ObserveAutoRemoveCompletedUseCase,
    private val observeAutoRetryFailedUseCase: ObserveAutoRetryFailedUseCase,
    private val observeHiddenCompletedIdsUseCase: ObserveHiddenCompletedIdsUseCase,
    private val addHiddenCompletedIdsUseCase: AddHiddenCompletedIdsUseCase
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow(DownloadScreenUiState())

    // Tracks whether completed downloads should be hidden
    private var hideCompleted: Boolean = false

    // Tracks whether auto-retry is enabled
    private var autoRetryEnabled: Boolean = false

    // Track which failed IDs have already been retried to avoid loops
    private val retriedFailedIds = mutableSetOf<String>()

    // Tracks set of hidden completed ids
    private var hiddenCompletedIds: Set<String> = emptySet()

    // Public immutable state for UI observation
    val uiState: StateFlow<DownloadScreenUiState> = _uiState.asStateFlow()

    /**
     * Initialize - observe downloads from repository and settings
     */
    init {
        Log.d("DownloadScreenVM", "ViewModel init started")
        try {
            // Observe hidden IDs first so we have persisted set available
            observeHiddenCompletedIds()
            observeAutoRemoveSetting()
            observeAutoRetrySetting()
            observeDownloads()
            Log.d("DownloadScreenVM", "ViewModel init completed successfully")
        } catch (e: Exception) {
            Log.e("DownloadScreenVM", "Error during ViewModel init", e)
        }
    }

    /**
     * Observe changes to the "Automatically remove completed" setting
     */
    private fun observeAutoRemoveSetting() {
        viewModelScope.launch {
            try {
                observeAutoRemoveCompletedUseCase().collect { value ->
                    // Update local flag
                    hideCompleted = value

                    if (value) {
                        // Persist and locally track currently completed downloads immediately so UI updates without waiting
                        // Get the current downloads snapshot directly from repository (avoids waiting for UI state to refresh)
                        val currentAll = try {
                            downloadRepository.getAllDownloads().first()
                        } catch (e: Exception) {
                            Log.e("DownloadScreenVM", "Failed to get snapshot of downloads", e)
                            _uiState.value.allDownloads
                        }
                        val toHideNow = currentAll.filter { it.isCompleted() && !hiddenCompletedIds.contains(it.id) }.map { it.id }.toSet()
                        if (toHideNow.isNotEmpty()) {
                            try {
                                addHiddenCompletedIdsUseCase(toHideNow)
                                // update local set immediately to avoid waiting for prefs listener
                                hiddenCompletedIds = hiddenCompletedIds + toHideNow
                                Log.d("DownloadScreenVM", "Persisted hidden IDs on toggle: $toHideNow. Hidden now total=${hiddenCompletedIds.size}")
                            } catch (e: Exception) {
                                Log.e("DownloadScreenVM", "Failed to persist hidden completed ids on toggle", e)
                            }
                        }

                        // Update UI state immediately using the repository snapshot so UI reflects changes without waiting
                        val snapshotFilteredAndSorted = getFilteredAndSortedDownloads(
                            allDownloads = currentAll,
                            filterType = _uiState.value.selectedFilter,
                            sortOption = _uiState.value.selectedSortOption
                        )
                        _uiState.update { currentState ->
                            currentState.copy(
                                allDownloads = currentAll,
                                displayedDownloads = applyAutoRemoveFilter(snapshotFilteredAndSorted),
                                isLoading = false
                            )
                        }

                        }

                    // If not handled above (toggle off) recompute displayed using current UI state's allDownloads
                    if (!value) {
                        // Do not un-hide previously hidden items when the user toggles the setting off.
                        // We intentionally avoid modifying displayedDownloads here so hidden items remain hidden.
                        Log.d("DownloadScreenVM", "Auto-remove toggled OFF; preserving previously hidden completed items (count=${hiddenCompletedIds.size})")
                    }

                 }
             } catch (e: Exception) {
                 Log.e("DownloadScreenVM", "Error observing auto-remove setting", e)
             }
         }
     }

    /**
     * Observe changes to the "Automatically retry failed downloads" setting
     */
    private fun observeAutoRetrySetting() {
        viewModelScope.launch {
            try {
                observeAutoRetryFailedUseCase().collect { value ->
                    autoRetryEnabled = value
                }
            } catch (e: Exception) {
                Log.e("DownloadScreenVM", "Error observing auto-retry setting", e)
            }
        }
    }

    /**
     * Observe changes to the hidden completed IDs
     */
    private fun observeHiddenCompletedIds() {
        viewModelScope.launch {
            try {
                observeHiddenCompletedIdsUseCase().collect { ids ->
                    hiddenCompletedIds = ids
                    // recompute displayed with new hidden set
                    _uiState.update { currentState ->
                        val filteredAndSorted = getFilteredAndSortedDownloads(
                            allDownloads = currentState.allDownloads,
                            filterType = currentState.selectedFilter,
                            sortOption = currentState.selectedSortOption
                        )
                        currentState.copy(
                            displayedDownloads = applyAutoRemoveFilter(filteredAndSorted)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadScreenVM", "Error observing hidden completed ids", e)
            }
        }
    }

    /**
     * Observe downloads from repository (real-time updates)
     */
    private fun observeDownloads() {
        viewModelScope.launch {
            try {
                var previousFailedIds: Set<String> = emptySet()
                downloadRepository.getAllDownloads().collect { downloads ->
                    // Compute current failed IDs and detect newly failed items since last emission
                    val currentFailedIds = downloads.filter { it.isFailed() }.map { it.id }.toSet()
                    val newlyFailed = currentFailedIds - previousFailedIds

                    // Attempt retries for newly failed items if setting enabled
                    if (autoRetryEnabled) {
                        newlyFailed.forEach { failedId ->
                            if (!retriedFailedIds.contains(failedId)) {
                                try {
                                    // call repository retry (this should schedule/start retry)
                                    downloadRepository.retryDownload(failedId)
                                    retriedFailedIds.add(failedId)
                                    Log.d("DownloadScreenVM", "Auto-retry triggered for: $failedId")
                                } catch (e: Exception) {
                                    Log.e("DownloadScreenVM", "Failed to auto-retry download $failedId", e)
                                }
                            }
                        }
                    }

                    // Clean up retried set for items that are no longer failed
                    clearRetriedIfStateChanged(downloads)

                    // Update previous failed ids for next emission
                    previousFailedIds = currentFailedIds

                    _uiState.update { currentState ->
                        val filteredAndSorted = getFilteredAndSortedDownloads(
                            allDownloads = downloads,
                            filterType = currentState.selectedFilter,
                            sortOption = currentState.selectedSortOption
                        )
                        currentState.copy(
                            allDownloads = downloads,
                            displayedDownloads = applyAutoRemoveFilter(filteredAndSorted),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadScreenVM", "Error observing downloads", e)
                _uiState.update { it.copy(isLoading = false, downloadError = "Failed to load downloads") }
            }
        }
    }

    /**
     * Apply auto-remove completed setting to a list of downloads
     */
    private fun applyAutoRemoveFilter(items: List<DownloadItem>): List<DownloadItem> {
        // First remove any items that are explicitly hidden (persisted)
        var result = items.filter { !hiddenCompletedIds.contains(it.id) }
        // If hideCompleted setting is on, remove currently completed items as well and persist them
        if (hideCompleted) {
            val toHideNow = result.filter { it.isCompleted() }.map { it.id }.toSet()
            if (toHideNow.isNotEmpty()) {
                try {
                    addHiddenCompletedIdsUseCase(toHideNow)
                } catch (e: Exception) {
                    Log.e("DownloadScreenVM", "Failed to persist hidden completed ids", e)
                }
            }
            result = result.filter { !it.isCompleted() }
        }
        return result
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
                    kotlinx.coroutines.delay(3.seconds)
                    _uiState.update { it.copy(downloadSuccess = false, successMessage = null) }
                }.onFailure { error ->
                    // Download failed to start
                    Log.e("DownloadScreenVM", "Download failed to start", error)

                    // Check if it's a FileAlreadyExistsException
                    if (error is FileAlreadyExistsException) {
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
                val filteredAndSorted = getFilteredAndSortedDownloads(
                    allDownloads = currentState.allDownloads,
                    filterType = filterType,
                    sortOption = currentState.selectedSortOption
                )
                currentState.copy(
                    selectedFilter = filterType,
                    displayedDownloads = applyAutoRemoveFilter(filteredAndSorted)
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
                val filteredAndSorted = getFilteredAndSortedDownloads(
                    allDownloads = currentState.allDownloads,
                    filterType = currentState.selectedFilter,
                    sortOption = sortOption
                )
                currentState.copy(
                    selectedSortOption = sortOption,
                    displayedDownloads = applyAutoRemoveFilter(filteredAndSorted)
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

    fun onErrorConsumed() {
        _uiState.update { it.copy(downloadError = null) }
    }

    /**
     * Handle download item more options click
     */
    @Suppress("UNUSED_PARAMETER")
    fun onDownloadItemMoreClick(item: DownloadItem) {
        // TODO: Show options menu for this item
        // This will be implemented when we add item actions
    }

    fun openDownload(id: String) {
        viewModelScope.launch {
            if (!runIfCompleted(id, "Open")) return@launch
            openDownloadUseCase(id).onFailure { error ->
                _uiState.update { it.copy(downloadError = error.message ?: "Failed to open file") }
            }
        }
    }

    fun shareDownload(id: String) {
        viewModelScope.launch {
            if (!runIfCompleted(id, "Share")) return@launch
            shareDownloadUseCase(id).onFailure { error ->
                _uiState.update { it.copy(downloadError = error.message ?: "Failed to share file") }
            }
        }
    }

    fun renameDownload(id: String, newName: String) {
        viewModelScope.launch {
            renameDownloadUseCase(id, newName)
        }
    }

    fun deleteSingleDownload(id: String) {
        viewModelScope.launch {
            deleteDownloadUseCase(id)
        }
    }

    fun showInFolder(id: String) {
        viewModelScope.launch {
            if (!runIfCompleted(id, "Show in folder")) return@launch
            showInFolderUseCase(id).onFailure { error ->
                val errorMessage = error.localizedMessage ?: "Failed to show file in folder"
                _uiState.update { it.copy(downloadError = errorMessage) }
            }
        }
    }

    fun showInfo(id: String) {
        viewModelScope.launch {
            showInfoUseCase(id)
                .onSuccess { item ->
                    if (!item.isCompleted()) {
                        _uiState.update { it.copy(downloadError = "Show info is available only for completed downloads") }
                    } else {
                        _uiState.update {
                            it.copy(
                                showInfoDialog = true,
                                infoDownloadItem = item
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(downloadError = error.message ?: "Failed to load file info") }
                }
        }
    }

    fun moveTo(id: String) {
        viewModelScope.launch {
            // Placeholder: need destination path
            moveToUseCase(id, "/default/path")
        }
    }

    /**
     * Request a move operation with an explicit destination path.
     * This is called after a folder picker returns a chosen destination (as a string).
     */
    fun moveToWithDestination(id: String, destinationPath: String) {
        viewModelScope.launch {
            try {
                val result = moveToUseCase(id, destinationPath)
                result.onSuccess {
                    _uiState.update { it.copy(downloadSuccess = true, successMessage = "Moved file") }
                    kotlinx.coroutines.delay(2500.milliseconds)
                    _uiState.update { it.copy(downloadSuccess = false, successMessage = null) }
                }.onFailure { err ->
                    Log.e("DownloadScreenVM", "Failed to move file: ${err.message}")
                    _uiState.update { it.copy(downloadError = "Failed to move file: ${err.message}") }
                }
                } catch (e: Exception) {
                Log.e("DownloadScreenVM", "Error during moveToWithDestination", e)
                _uiState.update { it.copy(downloadError = e.message ?: "Failed to move file") }
            }
        }
    }

    fun removeFromList(id: String) {
        viewModelScope.launch {
            if (!runIfCompleted(id, "Remove from list")) return@launch
            removeFromListUseCase(id).onFailure { error ->
                _uiState.update { it.copy(downloadError = error.message ?: "Failed to remove item from list") }
            }
        }
    }

    fun dismissInfoDialog() {
        _uiState.update { it.copy(showInfoDialog = false, infoDownloadItem = null) }
    }

    /**
     * Show rename dialog UI for the provided download id and default name.
     * The composable layer should observe uiState.showRenameDialog and show the dialog.
     */
    fun showRenameDialogFor(id: String, defaultName: String) {
        _uiState.update { it.copy(showRenameDialog = true, renameTargetId = id, renameDefaultName = defaultName) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(showRenameDialog = false, renameTargetId = null, renameDefaultName = null) }
    }

    fun confirmRename(newName: String) {
        val targetId = _uiState.value.renameTargetId
        if (targetId == null) return
        viewModelScope.launch {
            try {
                val result = renameDownloadUseCase(targetId, newName)
                result.onSuccess {
                    _uiState.update { it.copy(showRenameDialog = false, renameTargetId = null, renameDefaultName = null, downloadSuccess = true, successMessage = "Renamed file") }
                    kotlinx.coroutines.delay(2500.milliseconds)
                    _uiState.update { it.copy(downloadSuccess = false, successMessage = null) }
                }.onFailure { err ->
                    Log.e("DownloadScreenVM", "Rename failed: ${err.message}")
                    _uiState.update { it.copy(downloadError = "Failed to rename: ${err.message}") }
                }
            } catch (e: Exception) {
                Log.e("DownloadScreenVM", "Error renaming file", e)
                _uiState.update { it.copy(downloadError = e.message ?: "Failed to rename file") }
            }
        }
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
     * Toggle select all - if all selected, deselect all; otherwise select all
     */
    fun toggleSelectAll() {
        _uiState.update { currentState ->
            val allIds = currentState.displayedDownloads.map { it.id }.toSet()
            val isAllSelected = currentState.selectedDownloadIds.size == allIds.size && allIds.isNotEmpty()

            if (isAllSelected) {
                // Deselect all
                currentState.copy(selectedDownloadIds = emptySet())
            } else {
                // Select all
                currentState.copy(
                    isSelectionMode = true,
                    selectedDownloadIds = allIds
                )
            }
        }
        Log.d("DownloadScreenVM", "Toggled select all")
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
                    kotlinx.coroutines.delay(3.seconds)
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
     * Helper to recompute displayed downloads from current UI state and local flags
     */
    private fun recomputeDisplayed() {
        _uiState.update { currentState ->
            val filteredAndSorted = getFilteredAndSortedDownloads(
                allDownloads = currentState.allDownloads,
                filterType = currentState.selectedFilter,
                sortOption = currentState.selectedSortOption
            )
            currentState.copy(
                displayedDownloads = applyAutoRemoveFilter(filteredAndSorted)
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
        val domainOption = toDomainSortOption(sortOption)
        val sortedDownloads = sortDownloadsUseCase(filteredDownloads, domainOption)

        return sortedDownloads
    }

    /**
     * Map UI SortOption (category+order) to domain SortOption enum
     */
    private fun toDomainSortOption(option: SortOption): DomainSortOption {
        return when (option.category) {
            SortCategory.DATE -> if (option.order == SortOrder.ASCENDING) DomainSortOption.DATE_ASC else DomainSortOption.DATE_DESC
            SortCategory.NAME -> if (option.order == SortOrder.ASCENDING) DomainSortOption.NAME_ASC else DomainSortOption.NAME_DESC
            SortCategory.SIZE -> if (option.order == SortOrder.ASCENDING) DomainSortOption.SIZE_ASC else DomainSortOption.SIZE_DESC
        }
    }

    /**
     * Clear retried IDs if the download state has changed from FAILED
     * This allows items to be retried if they fail again in the future
     */
    private fun clearRetriedIfStateChanged(currentDownloads: List<DownloadItem>) {
        // Compute current failed IDs and retain only those in the retried set
        val failedIds = currentDownloads.filter { it.isFailed() }.map { it.id }.toSet()
        retriedFailedIds.retainAll(failedIds)
        Log.d("DownloadScreenVM", "RetriedFailedIds after cleanup: $retriedFailedIds")
    }

    private fun runIfCompleted(id: String, actionLabel: String): Boolean {
        val item = _uiState.value.allDownloads.firstOrNull { it.id == id }
        if (item == null) {
            _uiState.update { it.copy(downloadError = "$actionLabel failed: item not found") }
            return false
        }
        if (!item.isCompleted()) {
            _uiState.update { it.copy(downloadError = "$actionLabel is available only when the download is completed") }
            return false
        }
        return true
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
    val showRenameDialog: Boolean = false,
    val renameTargetId: String? = null,
    val renameDefaultName: String? = null,
    val showInfoDialog: Boolean = false,
    val infoDownloadItem: DownloadItem? = null,
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
