package com.piumal.filedownloadmanager.storage

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.io.OutputStream

/**
 * Interface for abstracting file operations.
 * Allows handling of both local files (via File paths) and SAF (via Content URIs).
 */
interface StorageManager {
    fun exists(uriString: String): Boolean
    fun getInputStream(uriString: String): InputStream?
    fun getOutputStream(uriString: String, append: Boolean = false): OutputStream?
    fun delete(uriString: String): Boolean
    fun getLength(uriString: String): Long
    fun getShareableUri(context: Context, uriString: String): Uri?
}

/**
 * Implementation of StorageManager.
 */
class StorageManagerImpl(private val context: Context) : StorageManager {
    // ... existing implementations ...

    override fun getShareableUri(context: Context, uriString: String): Uri? {
        return if (uriString.startsWith("content://")) {
            Uri.parse(uriString)
        } else {
            // Fallback for local files using FileProvider
            try {
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    java.io.File(uriString)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
// ...
    
    override fun exists(uriString: String): Boolean {
        // Simple implementation for now.
        // For local files, we might need to be careful with path traversal.
        // For SAF, we use ContentResolver.
        return try {
            if (uriString.startsWith("content://")) {
                context.contentResolver.openFileDescriptor(Uri.parse(uriString), "r")?.use { true } ?: false
            } else {
                java.io.File(uriString).exists()
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun getInputStream(uriString: String): InputStream? {
        return if (uriString.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(uriString))
        } else {
            java.io.File(uriString).inputStream()
        }
    }

    override fun getOutputStream(uriString: String, append: Boolean): OutputStream? {
        return if (uriString.startsWith("content://")) {
            val mode = if (append) "wa" else "w"
            context.contentResolver.openOutputStream(Uri.parse(uriString), mode)
        } else {
            java.io.FileOutputStream(java.io.File(uriString), append)
        }
    }

    override fun delete(uriString: String): Boolean {
        return if (uriString.startsWith("content://")) {
            try {
                androidx.documentfile.provider.DocumentFile.fromSingleUri(context, Uri.parse(uriString))?.delete() ?: false
            } catch (e: Exception) {
                false
            }
        } else {
            java.io.File(uriString).delete()
        }
    }

    override fun getLength(uriString: String): Long {
        return if (uriString.startsWith("content://")) {
            try {
                context.contentResolver.openFileDescriptor(Uri.parse(uriString), "r")?.use { it.statSize } ?: 0L
            } catch (e: Exception) {
                0L
            }
        } else {
            java.io.File(uriString).length()
        }
    }
}
