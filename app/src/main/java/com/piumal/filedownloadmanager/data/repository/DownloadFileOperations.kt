package com.piumal.filedownloadmanager.data.repository

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
     *
     * Follows this logic:
     * 1. Get the source file
     * 2. Construct destination directory and ensure it exists
     * 3. Create destination file path with source filename
     * 4. Try direct rename first (same filesystem)
     * 5. Fall back to I/O stream copy with sync + delete
     * 6. Verify file was moved successfully
     *
     * @param sourcePath Path to the source file
     * @param destinationFolderPath Path to destination folder (or full file path)
     * @return The File object pointing to the new location
     * @throws IllegalStateException if move fails after retries
     */
    fun movePhysicalFile(sourcePath: String, destinationFolderPath: String): File {
        // 1. Get the source file
        val sourceFile = File(sourcePath)
        require(sourceFile.exists()) { "Source file does not exist: ${sourceFile.absolutePath}" }

        // 2. Construct the destination FILE (not just the folder)
        val destDir = File(destinationFolderPath)
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw IllegalStateException("Failed to create destination directory: ${destDir.absolutePath}")
            }
        }

        // If destinationFolderPath is a directory, use source filename; otherwise treat as file path
        val destFile = if (destDir.isDirectory) {
            File(destDir, sourceFile.name)
        } else {
            destDir
        }

        // Ensure parent directory exists
        val destParent = destFile.parentFile
        if (destParent != null && !destParent.exists()) {
            if (!destParent.mkdirs()) {
                throw IllegalStateException("Failed to create parent directory: ${destParent.absolutePath}")
            }
        }

        return try {
            // 3. Check if files are on the same filesystem - try rename first
            val sameFilesystem = sourceFile.parentFile?.absolutePath == destFile.parentFile?.absolutePath
            if (sameFilesystem && sourceFile.renameTo(destFile)) {
                // Direct rename succeeded
                destFile
            } else {
                // 4. Fall back to I/O stream copy with foolproof error handling
                moveUsingIoStreams(sourceFile, destFile)
            }
        } catch (security: SecurityException) {
            throw IllegalStateException("Permission denied while moving file: ${sourceFile.absolutePath}", security)
        } catch (io: Exception) {
            // Clean up partial file if copy failed
            if (destFile.exists()) {
                destFile.delete()
            }
            throw IllegalStateException("Failed to move file from ${sourceFile.absolutePath} to ${destFile.absolutePath}", io)
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

