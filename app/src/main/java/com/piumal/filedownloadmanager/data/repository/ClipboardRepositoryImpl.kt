package com.piumal.filedownloadmanager.data.repository

import android.content.ClipboardManager
import android.content.Context
import com.piumal.filedownloadmanager.domain.repository.ClipboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of ClipboardRepository
 * Handles system clipboard operations
 */
class ClipboardRepositoryImpl(
    private val context: Context
) : ClipboardRepository {

    override suspend fun getClipboardUrl(): String? = withContext(Dispatchers.IO) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboard?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                // Basic URL validation
                if (text.startsWith("http://") || text.startsWith("https://")) {
                    return@withContext text
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}

