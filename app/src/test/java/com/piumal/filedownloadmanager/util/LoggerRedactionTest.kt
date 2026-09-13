package com.piumal.filedownloadmanager.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LoggerRedactionTest {

    @Test
    fun `test url redaction`() {
        val message = "Accessing URL: https://example.com/api?token=secret123&other=param"
        
        val redacted = redact(message)
        assertEquals("Accessing URL: https://example.com/api?token=REDACTED&other=param", redacted)
    }

    private fun redact(message: String): String {
        var redacted = message
        val patterns = listOf(
            Regex("(?i)(token|auth|password|api_key|secret|cookie|session_id)=[^&\\s]+"),
            Regex("(?i)Authorization: [^\\s]+"),
            Regex("(?i)https?://[^\\s?]+\\?.*(token|auth|password|api_key|secret|session_id)=[^&\\s]+")
        )
        
        for (pattern in patterns) {
            redacted = redacted.replace(pattern, "$1=REDACTED")
        }
        
        return redacted
    }
}
