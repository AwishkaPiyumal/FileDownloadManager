package com.piumal.filedownloadmanager.util

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

/**
 * Extracts the audio track from a local video file into a standalone audio file, entirely
 * on-device: no network calls, no third-party platform involved - this operates only on a video
 * file the user already has, obtained through the app's normal validated download flow.
 *
 * Deliberately built on Android's own MediaExtractor/MediaMuxer APIs only, not a bundled encoder
 * library:
 *  - No new native dependency, so no APK size hit, nothing to keep 16 KB-page-size aligned (a
 *    real, confirmed blocker with the MP3/LAME path this project briefly went down - see git
 *    history / prior conversation - and no licensing question to resolve (the MP3 path's only
 *    viable off-the-shelf option, naman14/TAndroidLame, turned out to be GPLv3-licensed, which
 *    is a poor fit for a proprietary app).
 *  - No re-encoding: the compressed audio samples are copied from the source container straight
 *    into a new one, so this is fast and doesn't lose additional quality the way a
 *    decode-then-re-encode pass would.
 *
 * The trade-off: output is .m4a (AAC-in-MP4), matching whatever codec the source video's audio
 * track already used - not .mp3. An .m4a file plays normally in any standard music player, file
 * manager, or when shared - it just isn't literally three letters "mp3". Producing a true .mp3
 * would require a real MP3 encoder (Android has no built-in one), which is exactly the
 * dependency this approach avoids.
 */
object AudioExtractor {

    sealed class ExtractionError(message: String) : Exception(message) {
        class NoAudioTrack(fileName: String) : ExtractionError("No audio track found in \"$fileName\"")
        class SourceUnreadable(fileName: String, cause: Throwable) :
            ExtractionError("Could not read \"$fileName\": ${cause.message}")
    }

    /**
     * Extracts the first audio track found in [sourceVideo] into [outputFile].
     *
     * Runs on [Dispatchers.IO]. On any failure, [outputFile] is deleted if it was partially
     * created, so callers never end up with a truncated/corrupt file left behind.
     */
    suspend fun extractAudioTrack(sourceVideo: File, outputFile: File): Result<File> =
        withContext(Dispatchers.IO) {
            var extractor: MediaExtractor? = null
            var muxer: MediaMuxer? = null
            var muxerStarted = false

            try {
                extractor = MediaExtractor().apply {
                    try {
                        setDataSource(sourceVideo.absolutePath)
                    } catch (e: Exception) {
                        throw ExtractionError.SourceUnreadable(sourceVideo.name, e)
                    }
                }

                var audioTrackIndex = -1
                var audioFormat: MediaFormat? = null
                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                    if (mime.startsWith("audio/")) {
                        audioTrackIndex = i
                        audioFormat = format
                        break
                    }
                }

                if (audioTrackIndex < 0 || audioFormat == null) {
                    throw ExtractionError.NoAudioTrack(sourceVideo.name)
                }

                extractor.selectTrack(audioTrackIndex)

                muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val muxerTrackIndex = muxer.addTrack(audioFormat)
                muxer.start()
                muxerStarted = true

                val buffer = ByteBuffer.allocate(1 * 1024 * 1024) // 1 MB scratch buffer for sample data
                val bufferInfo = MediaCodec.BufferInfo()

                while (true) {
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break

                    bufferInfo.offset = 0
                    bufferInfo.size = sampleSize
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    bufferInfo.flags = extractor.sampleFlags

                    muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                    extractor.advance()
                }

                Result.success(outputFile)
            } catch (e: Exception) {
                outputFile.delete()
                Result.failure(e)
            } finally {
                // stop()/release() can themselves throw if the muxer never started successfully
                // or the source had zero samples; none of that should mask the real result above.
                if (muxerStarted) {
                    try {
                        muxer?.stop()
                    } catch (_: Exception) {
                    }
                }
                try {
                    muxer?.release()
                } catch (_: Exception) {
                }
                extractor?.release()
            }
        }
}
