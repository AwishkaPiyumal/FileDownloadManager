package com.piumal.filedownloadmanager.storage

import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class FileOperationsTest {

    @Test
    fun `checkContainment allows files inside directory`() {
        val dir = File("downloads")
        val file = File(dir, "safe.txt")
        // Since we are running in unit tests, we don't have real filesystem access,
        // but canonicalization might fail. Let's use temporary files if needed,
        // or mock the file system.
        // For now, let's assume it works based on path string.
        FileOperations.checkContainment(file, dir)
    }

    @Test
    fun `checkContainment blocks path traversal`() {
        val dir = File("downloads")
        val file = File(dir, "../etc/passwd")
        assertThrows(SecurityException::class.java) {
            FileOperations.checkContainment(file, dir)
        }
    }
}
