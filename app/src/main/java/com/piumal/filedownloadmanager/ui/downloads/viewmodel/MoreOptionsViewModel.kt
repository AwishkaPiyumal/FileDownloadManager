package com.piumal.filedownloadmanager.ui.downloads.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piumal.filedownloadmanager.domain.usecase.PauseAllDownloadsUseCase
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
    private val pauseAllDownloadsUseCase: PauseAllDownloadsUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "MoreOptionsViewModel"
    }

    // State for menu visibility
    private val _isMenuExpanded = MutableStateFlow(false)
    val isMenuExpanded: StateFlow<Boolean> = _isMenuExpanded.asStateFlow()

    // Event flow for triggering selection mode in DownloadScreen
    private val _selectionModeEvent = MutableSharedFlow<Boolean>()
    val selectionModeEvent: SharedFlow<Boolean> = _selectionModeEvent.asSharedFlow()

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
                is MoreMenuAction.RemoveAll -> handleRemoveAll()
                is MoreMenuAction.DeleteAll -> handleDeleteAll()
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
     */
    private fun handleResumeAll() {
        // TODO: Implement resume all downloads
        // Call resumeAllDownloadsUseCase when implemented
    }

    /**
     * Retry all failed downloads
     */
    private fun handleRetryAll() {
        // TODO: Implement retry all failed downloads
        // Call retryAllDownloadsUseCase when implemented
    }

    /**
     * Remove all downloads from the list (keeps files)
     */
    private fun handleRemoveAll() {
        // TODO: Implement remove all downloads from list
        // Call removeAllDownloadsUseCase when implemented
        // This removes records but keeps downloaded files
    }

    /**
     * Delete all downloads and their files
     */
    private fun handleDeleteAll() {
        // TODO: Implement delete all downloads and files
        // Call deleteAllDownloadsUseCase when implemented
        // This removes records AND deletes the files
        // Should show confirmation dialog before executing
    }

    /**
     * Show help/tutorial on how to download
     */
    private fun handleHowToDownload() {
        // TODO: Implement navigation to help screen or show tutorial
        // Navigate to help screen or show a dialog with instructions
    }
}

