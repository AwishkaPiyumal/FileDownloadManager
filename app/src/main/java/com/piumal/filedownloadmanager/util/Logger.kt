package com.piumal.filedownloadmanager.util

import android.util.Log

/**
 * A safe logger wrapper to prevent sensitive data from being logged.
 */
object Logger {
    private const val TAG = "FDM_LOG"

    fun d(tag: String, message: String) {
        // Redact potential sensitive info here
        Log.d(tag, redact(message))
    }

    fun i(tag: String, message: String) {
        Log.i(tag, redact(message))
    }

    fun w(tag: String, message: String) {
        Log.w(tag, redact(message))
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        // Redact potential sensitive info from throwable message
        val safeMessage = throwable?.message?.let { redact(it) } ?: message
        if (throwable != null) {
            Log.e(tag, safeMessage, throwable)
        } else {
            Log.e(tag, safeMessage)
        }
    }

    private fun redact(message: String): String {
        // Redact potential sensitive info
        var redacted = message
        
        // Patterns to redact
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
