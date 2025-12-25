package com.piumal.filedownloadmanager.domain.usecase

import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use Case to get URL from clipboard
 * Follows Clean Architecture - encapsulates business logic
 */
class GetClipboardUrlUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(): String? {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboard?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                // Only return if it looks like a URL
                if (text.startsWith("http://", ignoreCase = true) ||
                    text.startsWith("https://", ignoreCase = true)) {
                    text
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

