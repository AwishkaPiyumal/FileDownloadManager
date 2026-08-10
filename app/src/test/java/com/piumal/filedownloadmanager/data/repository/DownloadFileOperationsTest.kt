package com.piumal.filedownloadmanager.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DownloadFileOperationsTest {

    @Test
    fun `deletePhysicalFile removes existing file`() {
        val file = File.createTempFile("delete_test", ".txt")
        file.writeText("hello")

        val result = DownloadFileOperations.deletePhysicalFile(file.absolutePath)

        assertTrue(result)
        assertFalse(file.exists())
    }

    @Test
    fun `deletePhysicalFile returns true when file is already missing`() {
        val missing = File(System.getProperty("java.io.tmpdir"), "missing_${System.nanoTime()}.txt")

        val result = DownloadFileOperations.deletePhysicalFile(missing.absolutePath)

        assertTrue(result)
    }

    @Test
    fun `movePhysicalFile moves file and removes original`() {
        val source = File.createTempFile("move_test", ".txt")
        source.writeText("moved content")
        val destinationDir = File(System.getProperty("java.io.tmpdir"), "move_dest_${System.nanoTime()}")
        destinationDir.mkdirs()
        val destinationPath = File(destinationDir, "renamed.txt").absolutePath

        val target = DownloadFileOperations.movePhysicalFile(source.absolutePath, destinationPath)

        assertEquals(destinationPath, target.absolutePath)
        assertTrue(target.exists())
        assertFalse(source.exists())
        assertEquals("moved content", target.readText())
    }
}

