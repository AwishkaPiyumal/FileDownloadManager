package com.piumal.filedownloadmanager.storage

import android.content.Context
import android.net.Uri
import com.piumal.filedownloadmanager.domain.util.DownloadStoragePaths
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

    override fun getShareableUri(context: Context, uriString: String): Uri? {
        return if (uriString.startsWith("content://")) {
            Uri.parse(uriString)
        } else {
            // Fallback for local files using FileProvider
            try {
                val file = java.io.File(uriString)
                FileOperations.checkContainment(file, DownloadStoragePaths.getDownloadDirectory())
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override fun exists(uriString: String): Boolean {
        return try {
            if (uriString.startsWith("content://")) {
                context.contentResolver.openFileDescriptor(Uri.parse(uriString), "r")?.use { true } ?: false
            } else {
                val file = java.io.File(uriString)
                FileOperations.checkContainment(file, DownloadStoragePaths.getDownloadDirectory())
                file.exists()
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun getInputStream(uriString: String): InputStream? {
        return try {
            if (uriString.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(uriString))
            } else {
                val file = java.io.File(uriString)
                FileOperations.checkContainment(file, DownloadStoragePaths.getDownloadDirectory())
                file.inputStream()
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getOutputStream(uriString: String, append: Boolean): OutputStream? {
        return try {
            if (uriString.startsWith("content://")) {
                val mode = if (append) "wa" else "w"
                context.contentResolver.openOutputStream(Uri.parse(uriString), mode)
            } else {
                val file = java.io.File(uriString)
                FileOperations.checkContainment(file, DownloadStoragePaths.getDownloadDirectory())
                java.io.FileOutputStream(file, append)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun delete(uriString: String): Boolean {
        return try {
            if (uriString.startsWith("content://")) {
                androidx.documentfile.provider.DocumentFile.fromSingleUri(context, Uri.parse(uriString))?.delete() ?: false
            } else {
                val file = java.io.File(uriString)
                FileOperations.checkContainment(file, DownloadStoragePaths.getDownloadDirectory())
                file.delete()
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun getLength(uriString: String): Long {
        return try {
            if (uriString.startsWith("content://")) {
                context.contentResolver.openFileDescriptor(Uri.parse(uriString), "r")?.use { it.statSize } ?: 0L
            } else {
                val file = java.io.File(uriString)
                FileOperations.checkContainment(file, DownloadStoragePaths.getDownloadDirectory())
                file.length()
            }
        } catch (e: Exception) {
            0L
        }
    }
}
