package com.piumal.filedownloadmanager.domain.util

import org.junit.Assert.assertTrue
import org.junit.Test

class ContentValidatorRegressionTest {

    @Test
    fun `validateMimeType should allow legitimate files with application-octet-stream type if extension is safe`() {
        val url = "https://example.com/data.zip"
        val mimeType = "application/octet-stream"
        
        val result = ContentValidator.validateMimeType(mimeType, url)
        
        assertTrue("Should allow .zip file with octet-stream MIME type", result.isValid)
    }

    @Test
    fun `validateMimeType should allow legitimate files with unknown type if extension is safe`() {
        val url = "https://example.com/archive.tar.gz"
        val mimeType = "application/x-unknown-type"
        
        val result = ContentValidator.validateMimeType(mimeType, url)
        
        assertTrue("Should allow .tar.gz file with unknown MIME type", result.isValid)
    }
}
