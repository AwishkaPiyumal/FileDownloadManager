package com.piumal.filedownloadmanager.util

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteOrder
import com.naman14.androidlame.AndroidLame
import com.naman14.androidlame.LameBuilder

/**
 * Selectable output quality for MP3 extraction. kbps values are passed straight through to the
 * encoder as a constant bitrate target.
 */
enum class Mp3Bitrate(val kbps: Int, val label: String) {
    HIGH(320, "320 kbps (High quality)"),
    STANDARD(128, "128 kbps (Standard, smaller file)")
}

/**
 * Abstraction over a real MP3 encoder, so the decode/orchestration pipeline in [AudioExtractor]
 * can be written and reasoned about independently of exactly which native encoder library ends
 * up wired in. [Lame3Mp3Encoder] below is a concrete integration sketch - read its doc comment
 * before relying on it, since it needs a native dependency this project doesn't have yet and I
 * can't compile-verify it in this environment.
 */
interface Mp3Encoder {
    /** Must be called exactly once, before any [encode] calls. */
    fun init(sampleRateHz: Int, channelCount: Int, bitrateKbps: Int)

    /** Encodes [sampleCount] interleaved 16-bit PCM samples from [samples]; returns MP3 bytes produced (may be empty - encoders buffer internally). */
    fun encode(samples: ShortArray, sampleCount: Int): ByteArray

    /** Call once after the last [encode], to flush any buffered frames. Returns final bytes, if any. */
    fun flush(): ByteArray

    /** Releases native resources. Safe to call multiple times / before init. */
    fun release()
}

/**
 * Extracts the audio track from a local video file into a standalone MP3 file, entirely
 * on-device: no network calls, no third-party platform involved - this operates only on a video
 * file the user already has, obtained through the app's normal validated download flow.
 *
 * Pipeline: MediaExtractor demuxes the source container -> MediaCodec (built into Android; no
 * dependency needed) decodes the compressed audio track to raw PCM -> the supplied [Mp3Encoder]
 * re-encodes that PCM to MP3 at the requested bitrate -> bytes are written straight to
 * [outputFile]. This *is* a real re-encode (unlike a container remux), so it takes CPU time
 * roughly proportional to audio duration, and produces a genuine .mp3 rather than a repackaged
 * copy of the original codec.
 */
object AudioExtractor {

    sealed class ExtractionError(message: String) : Exception(message) {
        class NoAudioTrack(fileName: String) : ExtractionError("No audio track found in \"$fileName\"")
        class SourceUnreadable(fileName: String, cause: Throwable) :
            ExtractionError("Could not read \"$fileName\": ${cause.message}")
        class DecoderUnavailable(mime: String, cause: Throwable) :
            ExtractionError("No audio decoder available for $mime: ${cause.message}")
    }

    /**
     * Runs on [Dispatchers.IO]. On any failure, [outputFile] is deleted if it was partially
     * created, so callers never end up with a truncated/corrupt file left behind.
     *
     * @param encoderFactory Defaults to [Lame3Mp3Encoder] - override in tests, or if you wire up
     *   a different encoder implementation.
     */
    suspend fun extractAudioAsMp3(
        sourceVideo: File,
        outputFile: File,
        bitrate: Mp3Bitrate,
        encoderFactory: () -> Mp3Encoder = { Lame3Mp3Encoder() }
    ): Result<File> = withContext(Dispatchers.IO) {
        var extractor: MediaExtractor? = null
        var decoder: MediaCodec? = null
        val encoder = encoderFactory()
        var output: FileOutputStream? = null

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

            val mime = audioFormat.getString(MediaFormat.KEY_MIME)!!
            // The decoder's actual output format (read below via INFO_OUTPUT_FORMAT_CHANGED,
            // falling back to these) is the source of truth for what the encoder needs to match -
            // it's what the PCM buffers we hand the encoder actually contain.
            var sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            decoder = try {
                MediaCodec.createDecoderByType(mime)
            } catch (e: Exception) {
                throw ExtractionError.DecoderUnavailable(mime, e)
            }
            decoder.configure(audioFormat, null, null, 0)
            decoder.start()

            output = FileOutputStream(outputFile)
            var encoderInitialized = false

            val bufferInfo = MediaCodec.BufferInfo()
            var inputEOS = false
            var outputEOS = false
            val timeoutUs = 10_000L

            while (!outputEOS) {
                if (!inputEOS) {
                    val inputIndex = decoder.dequeueInputBuffer(timeoutUs)
                    if (inputIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputIndex)!!
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputEOS = true
                        } else {
                            decoder.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputIndex = decoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
                when {
                    outputIndex >= 0 -> {
                        if (bufferInfo.size > 0) {
                            if (!encoderInitialized) {
                                encoder.init(sampleRate, channelCount, bitrate.kbps)
                                encoderInitialized = true
                            }

                            val outBuffer = decoder.getOutputBuffer(outputIndex)!!
                            outBuffer.position(bufferInfo.offset)
                            outBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            // PCM from MediaCodec is little-endian; ByteBuffer defaults to
                            // BIG_ENDIAN, so this must be set explicitly or every sample comes out
                            // byte-swapped (silent corruption, not a crash - the file would
                            // "extract" fine and just sound like static).
                            outBuffer.order(ByteOrder.LITTLE_ENDIAN)

                            val shortBuffer = outBuffer.asShortBuffer()
                            val samples = ShortArray(shortBuffer.remaining())
                            shortBuffer.get(samples)

                            val mp3Bytes = encoder.encode(samples, samples.size)
                            if (mp3Bytes.isNotEmpty()) output.write(mp3Bytes)
                        }

                        decoder.releaseOutputBuffer(outputIndex, false)

                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputEOS = true
                        }
                    }
                    outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val newFormat = decoder.outputFormat
                        sampleRate = newFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        channelCount = newFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    }
                    // INFO_TRY_AGAIN_LATER (and the legacy INFO_OUTPUT_BUFFERS_CHANGED): nothing
                    // to do, loop again.
                }
            }

            if (!encoderInitialized) {
                // Degenerate case: an audio track with zero samples in it. Initialize anyway so
                // flush() below has a valid (if empty) encoder to call.
                encoder.init(sampleRate, channelCount, bitrate.kbps)
            }
            val finalBytes = encoder.flush()
            if (finalBytes.isNotEmpty()) output.write(finalBytes)

            Result.success(outputFile)
        } catch (e: Exception) {
            outputFile.delete()
            Result.failure(e)
        } finally {
            try { output?.flush() } catch (_: Exception) {}
            try { output?.close() } catch (_: Exception) {}
            try { encoder.release() } catch (_: Exception) {}
            try { decoder?.stop() } catch (_: Exception) {}
            try { decoder?.release() } catch (_: Exception) {}
            extractor?.release()
        }
    }
}

/**
 * Reference integration for [Mp3Encoder] using a LAME-based Android wrapper (e.g.
 * `com.github.naman14:TAndroidLame` / the `NorthernCaptain` or `droidapp` forks of the same
 * project, distributed via JitPack). Method names/signatures below (`encodeBufferInterleaved`,
 * `lameFlush`, the `LameBuilder` setters) match that library's documented API as of this writing.
 *
 * IMPORTANT - what you need to do before this works:
 * 1. Add the JitPack repository and a real version coordinate to your Gradle files (none of this
 *    project's build files were available to me, so I can't add this myself):
 *      settings.gradle: maven { url = uri("https://jitpack.io") } (alongside mavenCentral())
 *      app build.gradle: implementation("com.github.naman14:TAndroidLame:<version>")
 *      (check the project's releases/JitPack page for the current version tag)
 * 2. Fix the import below to match whichever fork/package you actually add - I have documented
 *    evidence of the method signatures, not a compiled copy of the library, so the exact package
 *    name may differ by fork.
 * 3. LICENSING - LAME itself is LGPL-2.1. Bundled as a compiled native library in an Android app
 *    (effectively static linking), LGPL compliance generally means providing the license text and
 *    making the corresponding LAME source available (a standard "open source licenses" screen
 *    covers this for many apps) - confirm the specifics with your own legal/compliance process
 *    before shipping; this isn't legal advice.
 * 4. The native .so this pulls in is exactly the kind of dependency the 16 KB page-size
 *    requirement covers (see the earlier audit report) - check Play Console's pre-launch report
 *    for an alignment warning on it specifically.
 * 5. I cannot compile or run this - please verify it end-to-end on a real device before shipping.
 */
class Lame3Mp3Encoder : Mp3Encoder {

    private var lame: AndroidLame? = null
    private var channelCount: Int = 2
    private var scratch = ByteArray(0)

    override fun init(sampleRateHz: Int, channelCount: Int, bitrateKbps: Int) {
        this.channelCount = channelCount

        val mode = if (channelCount == 1) LameBuilder.Mode.MONO else LameBuilder.Mode.STEREO
        lame = LameBuilder()
            .setInSampleRate(sampleRateHz)
            .setOutChannels(channelCount)
            .setOutBitrate(bitrateKbps)
            .setOutSampleRate(sampleRateHz)
            .setMode(mode)
            .setQuality(5)
            .build() as AndroidLame
    }

    override fun encode(samples: ShortArray, sampleCount: Int): ByteArray {
        // LAME's own documented worst-case output size: 1.25 * numSamples + 7200 bytes.
        val required = (1.25 * sampleCount).toInt() + 7200
        if (scratch.size < required) scratch = ByteArray(required)

        // encodeBufferInterleaved's "samples" parameter is frames per channel, not the total
        // interleaved array length - divide by channelCount (verified against two independent
        // sources; still worth double-checking against whichever fork you add).
        val framesPerChannel = sampleCount / channelCount



        val bytesWritten = lame!!.encodeBufferInterLeaved(samples, framesPerChannel, scratch)
        return if (bytesWritten > 0) scratch.copyOf(bytesWritten) else ByteArray(0)
    }

    override fun flush(): ByteArray {
        val buf = ByteArray(7200)

        val bytesWritten = lame?.flush(buf) ?: 0
        return if (bytesWritten > 0) buf.copyOf(bytesWritten) else ByteArray(0)
    }

    override fun release() {
        lame = null
    }
}
