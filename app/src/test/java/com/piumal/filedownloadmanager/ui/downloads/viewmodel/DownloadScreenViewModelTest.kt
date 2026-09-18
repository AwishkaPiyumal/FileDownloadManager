package com.piumal.filedownloadmanager.ui.downloads.viewmodel

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.usecase.download.FileOperationTestRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadScreenViewModelTest {

    @Test
    fun `fake repository exposes open share and show in folder helpers`() = runBlocking {
        val repository = FileOperationTestRepository()
        val item = DownloadItem(
            id = "1",
            fileName = "demo.txt",
            downloadedSize = 10,
            totalSize = 10,
            status = DownloadStatus.COMPLETED,
            url = "url",
            filePath = "/tmp/demo.txt"
        )
        repository.insertDownload(item)

        assertTrue(repository.openDownload("1").isSuccess)
        assertTrue(repository.shareDownload("1").isSuccess)
        assertTrue(repository.showInFolder("1").isSuccess)
    }
}
