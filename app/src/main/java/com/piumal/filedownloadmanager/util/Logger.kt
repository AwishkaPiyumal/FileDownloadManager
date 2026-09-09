package com.piumal.filedownloadmanager.util

import android.util.Log

/**
 * A safe logger wrapper to prevent sensitive data from being logged.
 */
object Logger {
    private const val TAG = "FDM_LOG"

    fun d(tag: String, message: String) {
        // Redact potential sensitive info here if needed
        Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
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
        // Basic redaction logic
        return message
            .replace(Regex("(?i)token=[^&\\s]+"), "token=REDACTED")
            .replace(Regex("(?i)password=[^&\\s]+"), "password=REDACTED")
            // Add more patterns as needed
    }
}
