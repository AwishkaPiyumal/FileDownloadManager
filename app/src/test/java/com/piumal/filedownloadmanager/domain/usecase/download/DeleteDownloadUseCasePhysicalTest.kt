package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DeleteDownloadUseCasePhysicalTest {

    @Test
    fun `delete download physically removes file then db record`() = runBlocking {
        val repository = FileOperationTestRepository()
        val useCase = DeleteDownloadUseCaseImpl(repository)
        val file = File.createTempFile("delete_usecase", ".txt")
        file.writeText("data")
        val item = DownloadItem(
            id = "1",
            fileName = file.name,
            downloadedSize = 10,
            totalSize = 10,
            status = DownloadStatus.COMPLETED,
            url = "url",
            filePath = file.absolutePath
        )
        repository.insertDownload(item)

        val result = useCase("1")

        assertTrue(result.isSuccess)
        assertFalse(file.exists())
        assertTrue(repository.getDownloadById("1") == null)
    }
}

