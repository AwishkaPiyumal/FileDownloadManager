package com.piumal.filedownloadmanager.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FileNameSanitizerTest {

    @Test
    fun `sanitize removes path traversal`() {
        val input = "../../../etc/passwd"
        val output = FileNameSanitizer.sanitize(input)
        assertFalse(output.contains("/"))
        assertFalse(output.contains(".."))
    }

    @Test
    fun `sanitize removes illegal characters`() {
        val input = "file<name>|?*.txt"
        val output = FileNameSanitizer.sanitize(input)
        assertEquals("file_name____.txt", output)
    }

    @Test
    fun `sanitize handles empty input with fallback`() {
        val input = ""
        val output = FileNameSanitizer.sanitize(input, "fallback")
        assert(output.startsWith("fallback"))
    }
}
