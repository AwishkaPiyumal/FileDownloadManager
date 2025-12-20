package com.piumal.filedownloadmanager.ui.downloads.viewmodel

import androidx.lifecycle.ViewModel
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.usecase.FormatFileSizeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for DownloadItem component
 * Handles business logic and state management
 * Follows MVVM architecture pattern
 */
@HiltViewModel
class DownloadItemViewModel @Inject constructor(
    private val formatFileSizeUseCase: FormatFileSizeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadItemState())
    val state: StateFlow<DownloadItemState> = _state.asStateFlow()

    /**
     * Initialize with download item data
     */
    fun setDownloadItem(item: DownloadItem) {
        _state.value = _state.value.copy(
            downloadItem = item,
            formattedSize = formatSize(item),
            progressPercentage = item.getProgressPercentage()
        )
    }

    /**
     * Handle more options button click
     */
    fun onMoreOptionsClick() {
        _state.value = _state.value.copy(
            showOptionsMenu = true
        )
    }

    /**
     * Dismiss options menu
     */
    fun onDismissOptionsMenu() {
        _state.value = _state.value.copy(
            showOptionsMenu = false
        )
    }

    /**
     * Handle pause/resume action
     */
    fun onPauseResumeClick() {
        // TODO: Implement pause/resume logic in repository
        onDismissOptionsMenu()
    }

    /**
     * Handle cancel action
     */
    fun onCancelClick() {
        // TODO: Implement cancel logic in repository
        onDismissOptionsMenu()
    }

    /**
     * Handle delete action
     */
    fun onDeleteClick() {
        // TODO: Implement delete logic in repository
        onDismissOptionsMenu()
    }

    /**
     * Handle open file action (for completed downloads)
     */
    fun onOpenFileClick() {
        // TODO: Implement open file logic
        onDismissOptionsMenu()
    }

    /**
     * Format file size based on download status
     */
    private fun formatSize(item: DownloadItem): String {
        return if (item.isCompleted()) {
            formatFileSizeUseCase(item.totalSize)
        } else {
            formatFileSizeUseCase.formatProgress(item.downloadedSize, item.totalSize)
        }
    }
}

/**
 * UI state for download item
 */
data class DownloadItemState(
    val downloadItem: DownloadItem? = null,
    val formattedSize: String = "",
    val progressPercentage: Int = 0,
    val showOptionsMenu: Boolean = false
)

