package com.piumal.filedownloadmanager.storage

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class StorageManagerTest {

    private lateinit var context: Context
    private lateinit var storageManager: StorageManager
    private lateinit var tempFile: File

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        storageManager = StorageManagerImpl(context)
        tempFile = File(context.cacheDir, "test_file.txt")
        tempFile.writeText("Hello, SAF!")
    }

    @Test
    fun testExistsForLocalFile() {
        assertTrue(storageManager.exists(tempFile.absolutePath))
    }

    @Test
    fun testDeleteForLocalFile() {
        assertTrue(storageManager.exists(tempFile.absolutePath))
        assertTrue(storageManager.delete(tempFile.absolutePath))
        assertFalse(storageManager.exists(tempFile.absolutePath))
    }

    @Test
    fun testGetLengthForLocalFile() {
        assertTrue(storageManager.getLength(tempFile.absolutePath) > 0)
    }

    @Test
    fun testGetInputStreamForLocalFile() {
        val inputStream = storageManager.getInputStream(tempFile.absolutePath)
        assertTrue(inputStream != null)
        inputStream?.use {
            val content = it.bufferedReader().use { reader -> reader.readText() }
            assertTrue(content == "Hello, SAF!")
        }
    }
}
