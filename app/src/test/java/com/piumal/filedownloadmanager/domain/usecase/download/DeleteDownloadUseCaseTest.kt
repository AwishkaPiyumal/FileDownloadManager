package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteDownloadUseCaseTest {
    
    @Test
    fun `delete download removes item from repository`() = runBlocking {
        val repository = FakeDownloadRepository()
        val useCase = DeleteDownloadUseCaseImpl(repository)
        val item = DownloadItem(id = "1", fileName = "test.txt", downloadedSize = 10, totalSize = 10, status = DownloadStatus.COMPLETED, url = "url", filePath = "path")
        repository.insertDownload(item)
        
        val result = useCase("1")
        
        assertTrue(result.isSuccess)
        assertNull(repository.getDownloadById("1"))
    }
}
