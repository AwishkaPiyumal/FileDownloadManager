package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CopyToUseCaseTest {

    @Test
    fun `copy to updates file path and retains original file`() = runBlocking {
        val repository = FileOperationTestRepository()
        val useCase = CopyToUseCaseImpl(repository)
        val source = File.createTempFile("copy_usecase", ".txt")
        source.writeText("payload")
        val destinationDir = File(System.getProperty("java.io.tmpdir"), "copy_usecase_dest_${System.nanoTime()}")
        destinationDir.mkdirs()
        val destinationPath = destinationDir.absolutePath
        val expectedFile = File(destinationPath, source.name)
        val item = DownloadItem(
            id = "42",
            fileName = source.name,
            downloadedSize = 10,
            totalSize = 10,
            status = DownloadStatus.COMPLETED,
            url = "url",
            filePath = source.absolutePath
        )
        repository.insertDownload(item)

        val result = useCase(item, destinationPath)

        assertTrue(result.isSuccess)
        
        // Verify original file still exists
        assertTrue(source.exists())
        assertEquals("payload", source.readText())
        
        // Verify new file exists
        assertTrue(expectedFile.exists())
        assertEquals("simulated_copy_content", expectedFile.readText())
    }
}
