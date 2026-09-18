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

        // Compare with a trailing separator so a sibling directory whose name merely starts
        // with the same characters (e.g. ".../Downloads-evil" vs ".../Downloads") isn't
        // mistaken for a subdirectory by a plain string prefix check.
        val isExactMatch = canonicalFile.path == canonicalDir.path
        val isContained = canonicalFile.path.startsWith(canonicalDir.path + File.separator)

        if (!isExactMatch && !isContained) {
            throw SecurityException("Filesystem access violation: Attempted access outside approved directory.")
        }
    }
}
