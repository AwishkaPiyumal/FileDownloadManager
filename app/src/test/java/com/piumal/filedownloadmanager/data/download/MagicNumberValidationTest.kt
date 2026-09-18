package com.piumal.filedownloadmanager.data.download

import com.piumal.filedownloadmanager.domain.util.ContentValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MagicNumberValidationTest {

    @Test
    fun `test malicious signatures detected`() {
        // MZ signature (Windows PE)
        val mzBuffer = byteArrayOf(0x4D, 0x5A)
        assertTrue(ContentValidator.isMaliciousSignature(mzBuffer))

        // ELF signature (Linux Executable)
        val elfBuffer = byteArrayOf(0x7F, 0x45, 0x4C, 0x46)
        assertTrue(ContentValidator.isMaliciousSignature(elfBuffer))

        // Script signature
        val scriptBuffer = byteArrayOf(0x23, 0x21)
        assertTrue(ContentValidator.isMaliciousSignature(scriptBuffer))
    }

    @Test
    fun `test legitimate signatures accepted`() {
        // PDF signature
        val pdfBuffer = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        assertFalse(ContentValidator.isMaliciousSignature(pdfBuffer))

        // ZIP signature
        val zipBuffer = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        assertFalse(ContentValidator.isMaliciousSignature(zipBuffer))
    }
}
