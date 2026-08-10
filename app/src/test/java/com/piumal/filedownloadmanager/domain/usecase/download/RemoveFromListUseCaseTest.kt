package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RemoveFromListUseCaseTest {

    @Test
    fun `remove from list keeps physical file and deletes db row only`() = runBlocking {
        val repository = FileOperationTestRepository()
        val useCase = RemoveFromListUseCaseImpl(repository)
        val file = File.createTempFile("remove_list", ".txt")
        file.writeText("keep me")
        val item = DownloadItem(
            id = "99",
            fileName = file.name,
            downloadedSize = 1,
            totalSize = 1,
            status = DownloadStatus.COMPLETED,
            url = "url",
            filePath = file.absolutePath
        )
        repository.insertDownload(item)

        val result = useCase("99")

        assertTrue(result.isSuccess)
        assertTrue(file.exists())
        assertNull(repository.getDownloadById("99"))
    }
}

