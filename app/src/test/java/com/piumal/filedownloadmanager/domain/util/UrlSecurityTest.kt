package com.piumal.filedownloadmanager.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSecurityTest {

    @Test
    fun `https URL should be valid`() {
        val url = "https://example.com/file.zip"
        assertTrue(ContentValidator.validateDownloadUrl(url).isValid)
    }

    @Test
    fun `http URL should be invalid`() {
        val url = "http://example.com/file.zip"
        assertFalse(ContentValidator.validateDownloadUrl(url).isValid)
    }

    @Test
    fun `malformed URL should be invalid`() {
        val url = "not-a-url"
        assertFalse(ContentValidator.validateDownloadUrl(url).isValid)
    }

    @Test
    fun `HTTPS URL without extension should be valid`() {
        val url = "https://example.com/file"
        assertTrue(ContentValidator.validateDownloadUrl(url).isValid)
    }

    @Test
    fun `HTTPS URL with query parameters should be valid`() {
        val url = "https://example.com/file.zip?token=123"
        assertTrue(ContentValidator.validateDownloadUrl(url).isValid)
    }

    @Test
    fun `HTTPS CDN URL should be valid`() {
        val url = "https://cdn.example.com/file.zip"
        assertTrue(ContentValidator.validateDownloadUrl(url).isValid)
    }
}
