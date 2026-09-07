package com.piumal.filedownloadmanager.storage

import java.io.File
import java.io.IOException

/**
 * Utility to securely interact with the filesystem.
 */
object FileOperations {

    /**
     * Ensures the given file is within the approved download directory.
     * 
     * @param file The file to check.
     * @param approvedDirectory The base directory where all files must reside.
     * @throws SecurityException if the file is outside the approved directory.
     */
    fun checkContainment(file: File, approvedDirectory: File) {
        val canonicalFile = file.canonicalFile
        val canonicalDir = approvedDirectory.canonicalFile
        
        if (!canonicalFile.path.startsWith(canonicalDir.path)) {
            throw SecurityException("Filesystem access violation: Attempted access outside approved directory.")
        }
    }
}
