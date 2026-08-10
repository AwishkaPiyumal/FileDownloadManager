package com.piumal.filedownloadmanager.ui.downloads

import androidx.lifecycle.ViewModel
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DownloadUiModel(
    val id: String,
    val fileName: String,
    val status: DownloadStatus,
    val actionsEnabled: Boolean
)

@HiltViewModel
class DownloadListViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<DownloadUiModel>>(emptyList())
    val uiState: StateFlow<List<DownloadUiModel>> = _uiState.asStateFlow()

    fun updateDownloads(downloads: List<com.piumal.filedownloadmanager.domain.model.DownloadItem>) {
        _uiState.value = downloads.map {
            DownloadUiModel(
                id = it.id,
                fileName = it.fileName,
                status = it.status,
                actionsEnabled = it.status == DownloadStatus.COMPLETED
            )
        }
    }
}
