package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MoveToUseCaseTest {

    @Test
    fun `move to updates file path and deletes original file`() = runBlocking {
        val repository = FileOperationTestRepository()
        val useCase = MoveToUseCaseImpl(repository)
        val source = File.createTempFile("move_usecase", ".txt")
        source.writeText("payload")
        val destinationDir = File(System.getProperty("java.io.tmpdir"), "move_usecase_dest_${System.nanoTime()}")
        destinationDir.mkdirs()
        val destinationPath = File(destinationDir, "final.txt").absolutePath
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

        val result = useCase("42", destinationPath)

        assertTrue(result.isSuccess)
        val updated = repository.getDownloadById("42")
        assertTrue(updated != null)
        assertEquals(destinationPath, updated!!.filePath)
        assertFalse(source.exists())
        assertTrue(File(destinationPath).exists())
        assertEquals("payload", File(destinationPath).readText())
    }
}

