package com.piumal.filedownloadmanager.data.repository

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Pure file-system helpers used by the repository.
 * Kept separate so move/delete behavior can be unit tested without Android.
 */
object DownloadFileOperations {

    /**
     * Physically delete the file if it exists.
     * Returns true when the file no longer exists after the call.
     */
    fun deletePhysicalFile(filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) return true
        return file.delete()
    }

    /**
     * Move a file to [destinationFolderPath] using foolproof I/O stream fallback.
     * Includes a guaranteed writable fallback to the public Downloads directory for Scoped Storage compatibility.
     */
    fun movePhysicalFile(
        sourcePath: String,
        destinationFolderPath: String,
        context: Context? = null,
        fallbackDirFactory: () -> File = { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) }
    ): File {
        // 1. Get the source file
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) throw Exception("Source file missing: ${sourceFile.absolutePath}")

        // 2. Define target
        var targetDir = File(destinationFolderPath)

        // If target directory is not writable (Scoped Storage), try public Downloads directory fallback
        // then try internal app-specific filesDir as a guaranteed fallback.
        if (!targetDir.canWrite()) {
            val publicDownloadsDir = fallbackDirFactory() // defaults to public Downloads
            targetDir = File(publicDownloadsDir, "FileDownloadManager_Moved")

            // If still not writable, use internal app-specific storage as absolute final fallback
            if (!targetDir.canWrite() && context != null) {
                targetDir = File(context.filesDir, "FileDownloadManager_Moved")
            }

            if (!targetDir.exists()) targetDir.mkdirs()
        }

        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw IllegalStateException("Failed to create destination directory: ${targetDir.absolutePath}")
            }
        }

        val destFile = if (targetDir.isDirectory) {
            File(targetDir, sourceFile.name)
        } else {
            targetDir
        }

        return try {
            // Try direct rename
            val sameFilesystem = sourceFile.parentFile?.absolutePath == destFile.parentFile?.absolutePath
            if (sameFilesystem && sourceFile.renameTo(destFile)) {
                destFile
            } else {
                moveUsingIoStreams(sourceFile, destFile)
            }
        } catch (e: Exception) {
            if (destFile.exists()) destFile.delete()
            throw Exception("Move failed: ${e.message}")
        }
    }

    /**
     * Move file using Input/Output streams with integrity verification.
     * Includes explicit fsync to ensure data is written to disk.
     */
    private fun moveUsingIoStreams(sourceFile: File, destFile: File): File {
        // Copy using I/O streams
        try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                    }
                    // Critical: Force write to physical disk
                    output.fd.sync()

                    // Verify file sizes match
                    if (destFile.length() != sourceFile.length()) {
                        throw IllegalStateException(
                            "File size mismatch: source=${sourceFile.length()}, dest=${destFile.length()}"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            if (destFile.exists()) {
                destFile.delete()
            }
            throw IllegalStateException("I/O stream copy failed: ${e.message}", e)
        }

        // Only delete source if destination file exists and copy was successful
        if (!destFile.exists()) {
            throw IllegalStateException("Copy verification failed: destination file does not exist")
        }

        if (!sourceFile.delete()) {
            // Even if source deletion fails, destination exists, so move is technically successful
            // Log this but don't fail the operation
            System.err.println("Warning: Could not delete source file: ${sourceFile.absolutePath}")
        }

        return destFile
    }

}

