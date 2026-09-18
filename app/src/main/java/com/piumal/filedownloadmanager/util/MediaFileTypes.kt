package com.piumal.filedownloadmanager.util

/**
 * Shared helpers for recognizing video/audio files by extension.
 *
 * Kept as simple extension-based checks - consistent with how the rest of the app determines
 * file type (see ContentValidator.getFileExtension) - rather than inspecting file contents,
 * since this is only used to decide whether to *offer* the "Extract Audio" action in the UI.
 * The actual extraction (AudioExtractor) independently verifies the file has a real audio track
 * before doing anything, so a mislabeled extension here can't cause incorrect output - at worst
 * it would offer an action that then reports "no audio track found".
 */
object MediaFileTypes {

    private val VIDEO_EXTENSIONS = setOf(
        "mp4", "mkv", "mov", "webm", "avi", "3gp", "3gpp", "m4v", "ts", "flv"
    )

    fun isVideoFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension.isNotEmpty() && VIDEO_EXTENSIONS.contains(extension)
    }
}
