package com.piumal.filedownloadmanager.ui.downloads

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.usecase.download.FakeDownloadRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadListViewModelTest {
    @Test
    fun `actionsEnabled is true only when status is COMPLETED`() {
        val repository = FakeDownloadRepository()
        val viewModel = DownloadListViewModel(repository)
        
        val downloads = listOf(
            DownloadItem(id = "1", fileName = "test1.txt", downloadedSize = 10, totalSize = 10, status = DownloadStatus.COMPLETED, url = "url", filePath = "path"),
            DownloadItem(id = "2", fileName = "test2.txt", downloadedSize = 5, totalSize = 10, status = DownloadStatus.DOWNLOADING, url = "url", filePath = "path")
        )
        
        viewModel.updateDownloads(downloads)
        
        val uiModel1 = viewModel.uiState.value.find { it.id == "1" }
        val uiModel2 = viewModel.uiState.value.find { it.id == "2" }
        
        assertTrue(uiModel1?.actionsEnabled == true)
        assertFalse(uiModel2?.actionsEnabled == true)
    }
}
