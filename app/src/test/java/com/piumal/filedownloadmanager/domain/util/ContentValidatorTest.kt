package com.piumal.filedownloadmanager.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentValidatorTest {

    @Test
    fun `validateMimeType should pass for known safe extension even with unknown MIME type`() {
        // This test case represents the bug:
        // .png is a safe extension, but "application/x-unknown-type" is not in ALLOWED_MIME_TYPES
        val url = "https://example.com/image.png"
        val mimeType = "application/x-unknown-type"
        
        val result = ContentValidator.validateMimeType(mimeType, url)
        
        assertTrue("Should be valid for safe extension despite unknown MIME type", result.isValid)
    }

    @Test
    fun `validateMimeType should fail for unsafe file type`() {
        val url = "https://example.com/malicious.exe"
        val mimeType = "application/x-msdownload"
        
        val result = ContentValidator.validateMimeType(mimeType, url)
        
        assertFalse("Should be invalid for blocked MIME type", result.isValid)
    }

    @Test
    fun `validateFileSize should pass for size below limit`() {
        val size = ContentValidator.getMaxFileSize() - 1024L
        assertTrue(ContentValidator.validateFileSize(size).isValid)
    }

    @Test
    fun `validateFileSize should pass for size exactly at limit`() {
        val size = ContentValidator.getMaxFileSize()
        assertTrue(ContentValidator.validateFileSize(size).isValid)
    }

    @Test
    fun `validateFileSize should fail for size above limit`() {
        val size = ContentValidator.getMaxFileSize() + 1024L
        assertFalse(ContentValidator.validateFileSize(size).isValid)
    }
}
