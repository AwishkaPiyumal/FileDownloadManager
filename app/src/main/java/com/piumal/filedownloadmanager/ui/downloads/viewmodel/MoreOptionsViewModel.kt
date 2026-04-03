package com.piumal.filedownloadmanager.ui.downloads.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piumal.filedownloadmanager.domain.usecase.download.PauseAllDownloadsUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.ResumeAllDownloadsUseCase
import com.piumal.filedownloadmanager.domain.usecase.download.RetryAllDownloadsUseCase
import com.piumal.filedownloadmanager.ui.downloads.components.MoreMenuAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the More Options Menu state and actions
 *
 * This ViewModel follows MVVM architecture and manages:
 * - Menu visibility state
 * - Handling user menu selections
 * - Coordinating actions with download management
 */
@HiltViewModel
class MoreOptionsViewModel @Inject constructor(
    private val pauseAllDownloadsUseCase: PauseAllDownloadsUseCase,
    private val resumeAllDownloadsUseCase: ResumeAllDownloadsUseCase,
    private val retryAllDownloadsUseCase: RetryAllDownloadsUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "MoreOptionsViewModel"
    }

    // State for menu visibility
    private val _isMenuExpanded = MutableStateFlow(false)
    val isMenuExpanded: StateFlow<Boolean> = _isMenuExpanded.asStateFlow()

    // State for selection mode - shared with MainScreen to hide more_vert icon
    private val _isSelectionModeActive = MutableStateFlow(false)
    val isSelectionModeActive: StateFlow<Boolean> = _isSelectionModeActive.asStateFlow()

    // State for selected count - shared with MainScreen for delete button
    private val _selectedCount = MutableStateFlow(0)
    val selectedCount: StateFlow<Int> = _selectedCount.asStateFlow()

    // Event flow for triggering selection mode in DownloadScreen
    private val _selectionModeEvent = MutableSharedFlow<Boolean>()
    val selectionModeEvent: SharedFlow<Boolean> = _selectionModeEvent.asSharedFlow()

    // Event flow for triggering delete action in DownloadScreen
    private val _deleteSelectedEvent = MutableSharedFlow<Unit>()
    val deleteSelectedEvent: SharedFlow<Unit> = _deleteSelectedEvent.asSharedFlow()

    // Event flow for showing toast messages to user
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()


    /**
     * Show the more options menu
     */
    fun showMenu() {
        _isMenuExpanded.value = true
    }

    /**
     * Hide the more options menu
     */
    fun hideMenu() {
        _isMenuExpanded.value = false
    }

    /**
     * Toggle the menu visibility
     */
    fun toggleMenu() {
        _isMenuExpanded.value = !_isMenuExpanded.value
    }

    /**
     * Handle menu item selection
     * @param action The selected menu action
     */
    fun onMenuItemSelected(action: MoreMenuAction) {
        viewModelScope.launch {
            when (action) {
                is MoreMenuAction.Select -> handleSelect()
                is MoreMenuAction.PauseAll -> handlePauseAll()
                is MoreMenuAction.ResumeAll -> handleResumeAll()
                is MoreMenuAction.RetryAll -> handleRetryAll()
                is MoreMenuAction.HowToDownload -> handleHowToDownload()
            }
        }
    }

    /**
     * Enable selection mode for downloads
     * Emits event to trigger selection mode in DownloadScreen
     */
    private suspend fun handleSelect() {
        _selectionModeEvent.emit(true)
    }

    /**
     * Pause all active downloads
     * Uses PauseAllDownloadsUseCase to pause all DOWNLOADING and QUEUED items
     */
    private fun handlePauseAll() {
        viewModelScope.launch {
            Log.d(TAG, "Pause All action triggered")
            pauseAllDownloadsUseCase().fold(
                onSuccess = { pausedCount ->
                    Log.d(TAG, "Successfully paused $pausedCount downloads")
                    val message = if (pausedCount > 0) {
                        "Paused $pausedCount download${if (pausedCount > 1) "s" else ""}"
                    } else {
                        "No active downloads to pause"
                    }
                    _toastMessage.emit(message)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to pause downloads: ${error.message}")
                    _toastMessage.emit("Failed to pause downloads")
                }
            )
        }
    }

    /**
     * Resume all paused downloads
     * Uses ResumeAllDownloadsUseCase to resume all PAUSED items
     */
    private fun handleResumeAll() {
        viewModelScope.launch {
            Log.d(TAG, "Resume All action triggered")
            resumeAllDownloadsUseCase().fold(
                onSuccess = { resumedCount ->
                    Log.d(TAG, "Successfully resumed $resumedCount downloads")
                    val message = if (resumedCount > 0) {
                        "Resumed $resumedCount download${if (resumedCount > 1) "s" else ""}"
                    } else {
                        "No paused downloads to resume"
                    }
                    _toastMessage.emit(message)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to resume downloads: ${error.message}")
                    _toastMessage.emit("Failed to resume downloads")
                }
            )
        }
    }

    /**
     * Retry all failed downloads
     * Uses RetryAllDownloadsUseCase to retry all FAILED items
     */
    private fun handleRetryAll() {
        viewModelScope.launch {
            Log.d(TAG, "Retry All action triggered")
            retryAllDownloadsUseCase().fold(
                onSuccess = { retriedCount ->
                    Log.d(TAG, "Successfully retried $retriedCount downloads")
                    val message = if (retriedCount > 0) {
                        "Retrying $retriedCount download${if (retriedCount > 1) "s" else ""}"
                    } else {
                        "No failed downloads to retry"
                    }
                    _toastMessage.emit(message)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to retry downloads: ${error.message}")
                    _toastMessage.emit("Failed to retry downloads")
                }
            )
        }
    }


    /**
     * Show help/tutorial on how to download
     */
    private fun handleHowToDownload() {
        // TODO: Implement navigation to help screen or show tutorial
        // Navigate to help screen or show a dialog with instructions
    }

    /**
     * Set selection mode active state
     * Called from DownloadScreen when selection mode changes
     */
    fun setSelectionModeActive(isActive: Boolean) {
        _isSelectionModeActive.value = isActive
    }

    /**
     * Set selected count
     * Called from DownloadScreen when selection changes
     */
    fun setSelectedCount(count: Int) {
        _selectedCount.value = count
    }

    /**
     * Trigger delete selected action
     * Called from MainScreen when delete button is clicked
     */
    fun triggerDeleteSelected() {
        viewModelScope.launch {
            _deleteSelectedEvent.emit(Unit)
        }
    }
}

