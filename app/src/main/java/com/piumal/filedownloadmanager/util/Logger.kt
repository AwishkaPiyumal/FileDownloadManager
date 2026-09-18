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

    // internal (not private) so tests can call the real implementation directly instead of
    // keeping a hand-copied duplicate in sync (see LoggerRedactionTest).
    internal fun redact(message: String): String {
        var redacted = message

        // key=value pairs for sensitive keys, wherever they appear (including inside URLs, so
        // this alone also covers query-string secrets - no separate URL-shaped pattern needed).
        redacted = redacted.replace(
            Regex("(?i)(token|auth|password|api_key|secret|cookie|session_id)=[^&\\s]+")
        ) { match -> "${match.groupValues[1]}=REDACTED" }

        // "Authorization: <value>" headers. This pattern has no capture group, so the
        // replacement must not reference one - it previously reused the same "$1=REDACTED"
        // replacement as the pattern above, which threw IndexOutOfBoundsException: No group 1
        // (confirmed on the JVM) for every message that actually contained an Authorization
        // header, i.e. exactly the sensitive case this function exists to redact.
        redacted = redacted.replace(
            Regex("(?i)Authorization:\\s*[^\\r\\n]+")
        ) { "Authorization: REDACTED" }

        return redacted
    }
}
