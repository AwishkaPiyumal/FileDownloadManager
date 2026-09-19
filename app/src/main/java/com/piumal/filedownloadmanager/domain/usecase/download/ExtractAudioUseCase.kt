package com.piumal.filedownloadmanager.domain.usecase.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.repository.DownloadRepository
import com.piumal.filedownloadmanager.domain.util.DownloadStoragePaths
import com.piumal.filedownloadmanager.domain.util.FileNameSanitizer
import com.piumal.filedownloadmanager.util.AudioExtractor
import com.piumal.filedownloadmanager.util.MediaFileTypes
import java.io.File
import javax.inject.Inject

/**
 * Extracts just the audio track from an already-downloaded video into a new, separate file -
 * purely local processing on a file the user already has (see AudioExtractor's doc comment for
 * why this doesn't touch the network or any remote platform - it's audio/video processing, not
 * a way to fetch anything new). The result is inserted as its own completed download, so it
 * shows up in the list - and inherits Open/Share/Delete/etc. - the same as anything else there.
 */
interface ExtractAudioUseCase {
    suspend operator fun invoke(sourceDownloadId: String): Result<DownloadItem>
}

class ExtractAudioUseCaseImpl @Inject constructor(
    private val repository: DownloadRepository
) : ExtractAudioUseCase {

    override suspend fun invoke(sourceDownloadId: String): Result<DownloadItem> = runCatching {
        val source = repository.getDownloadById(sourceDownloadId)
            ?: throw IllegalArgumentException("Download not found")

        if (source.status != DownloadStatus.COMPLETED) {
            throw IllegalStateException("Only completed downloads can have their audio extracted")
        }
        if (!MediaFileTypes.isVideoFile(source.fileName)) {
            throw IllegalStateException("\"${source.fileName}\" isn't a recognized video file")
        }

        val sourceFile = File(source.filePath)
        if (!sourceFile.exists()) {
            throw IllegalStateException("The original file is no longer on disk")
        }

        val outputDir = DownloadStoragePaths.getDownloadDirectory().apply { mkdirs() }
        val sanitizedName = FileNameSanitizer.sanitize("${sourceFile.nameWithoutExtension}.m4a")
        val outputFileName = uniqueFileNameIn(outputDir, sanitizedName)
        val outputFile = File(outputDir, outputFileName)

        val extractedFile = AudioExtractor.extractAudioTrack(sourceFile, outputFile).getOrThrow()

        val audioItem = DownloadItem(
            id = System.currentTimeMillis().toString(),
            fileName = outputFileName,
            downloadedSize = extractedFile.length(),
            totalSize = extractedFile.length(),
            status = DownloadStatus.COMPLETED,
            url = source.url, // provenance: which download this audio was extracted from
            filePath = extractedFile.absolutePath,
            createdAt = System.currentTimeMillis()
        )

        repository.insertDownload(audioItem)
        audioItem
    }

    /** Same "name(1).ext, name(2).ext, ..." collision-avoidance scheme used elsewhere (StartDownloadUseCase). */
    private fun uniqueFileNameIn(directory: File, baseName: String): String {
        if (!File(directory, baseName).exists()) return baseName

        val nameWithoutExt = File(baseName).nameWithoutExtension
        val extension = File(baseName).extension
        var counter = 1
        var candidate: String
        do {
            candidate = if (extension.isNotEmpty()) "$nameWithoutExt($counter).$extension" else "$nameWithoutExt($counter)"
            counter++
        } while (File(directory, candidate).exists())
        return candidate
    }
}
