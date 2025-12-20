package com.piumal.filedownloadmanager.ui.downloads.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piumal.filedownloadmanager.domain.model.DownloadConfig
import com.piumal.filedownloadmanager.domain.usecase.ExtractFileNameUseCase
import com.piumal.filedownloadmanager.domain.usecase.GetClipboardUrlUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for AddDownloadDialog
 * Manages state and business logic for the download dialog
 * Follows MVVM architecture pattern
 */
class AddDownloadViewModel(
    private val getClipboardUrlUseCase: GetClipboardUrlUseCase,
    private val extractFileNameUseCase: ExtractFileNameUseCase,
    private val defaultDownloadPath: String = "/storage/emulated/0/Download/FileDownloadManager"
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(AddDownloadUiState())
    val uiState: StateFlow<AddDownloadUiState> = _uiState.asStateFlow()

    /**
     * Initialize dialog - load clipboard URL if available
     */
    fun initialize() {
        viewModelScope.launch {
            val clipboardUrl = getClipboardUrlUseCase()
            if (clipboardUrl != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        url = clipboardUrl,
                        fileName = extractFileNameUseCase(clipboardUrl)
                    )
                }
            }
        }
    }

    /**
     * Update URL and auto-extract filename if needed
     */
    fun onUrlChanged(url: String) {
        _uiState.update { currentState ->
            val newFileName = if (currentState.fileName.isEmpty()) {
                extractFileNameUseCase(url)
            } else {
                currentState.fileName
            }

            currentState.copy(
                url = url,
                fileName = newFileName
            )
        }
    }

    /**
     * Update file path
     */
    fun onFilePathChanged(filePath: String) {
        _uiState.update { it.copy(filePath = filePath) }
    }

    /**
     * Update file name
     */
    fun onFileNameChanged(fileName: String) {
        _uiState.update { it.copy(fileName = fileName) }
    }

    /**
     * Update schedule time
     */
    fun onScheduleTimeChanged(scheduleTime: Long?) {
        _uiState.update { it.copy(scheduleTime = scheduleTime) }
    }

    /**
     * Toggle schedule picker visibility
     */
    fun onSchedulePickerToggle(show: Boolean) {
        _uiState.update { it.copy(showSchedulePicker = show) }
    }

    /**
     * Validate if download can be started
     */
    fun isValid(): Boolean {
        val state = _uiState.value
        return state.url.isNotEmpty() && state.fileName.isNotEmpty()
    }

    /**
     * Get download configuration
     */
    fun getDownloadConfig(): DownloadConfig {
        val state = _uiState.value
        return DownloadConfig(
            url = state.url,
            filePath = state.filePath,
            fileName = state.fileName,
            scheduleTime = state.scheduleTime
        )
    }

    /**
     * Reset dialog state
     */
    fun reset() {
        _uiState.value = AddDownloadUiState(filePath = defaultDownloadPath)
    }
}

/**
 * UI State for AddDownloadDialog
 * Represents all the state needed for the UI
 */
data class AddDownloadUiState(
    val url: String = "",
    val filePath: String = "/storage/emulated/0/Download/FileDownloadManager",
    val fileName: String = "",
    val scheduleTime: Long? = null,
    val showSchedulePicker: Boolean = false
)

