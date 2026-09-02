@file:Suppress("unused")

package com.piumal.filedownloadmanager.data.download

import android.content.Context
import android.os.StatFs
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.util.ContentValidator
import com.piumal.filedownloadmanager.domain.util.DownloadStoragePaths
import com.piumal.filedownloadmanager.domain.util.FileNameSanitizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    private val context: Context
) {

    private companion object {
        private const val BUFFER_SIZE_BYTES = 64 * 1024
        private const val STORAGE_SAFETY_MARGIN_BYTES = 32L * 1024 * 1024
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    data class DownloadProgress(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val status: DownloadStatus,
        val error: String? = null
    )

    fun downloadFile(downloadItem: DownloadItem): Flow<DownloadProgress> = flow {
        val safeFileName = FileNameSanitizer.sanitize(downloadItem.fileName)
        try {
            if (!ContentValidator.isHttpOrHttps(downloadItem.url)) {
                emit(DownloadProgress(0, 0, DownloadStatus.FAILED, "Only HTTP and HTTPS URLs are supported"))
                return@flow
            }
            val validation = ContentValidator.validateDownloadUrl(downloadItem.url)
            if (!validation.isValid) {
                emit(DownloadProgress(0, 0, DownloadStatus.FAILED, validation.message))
                return@flow
            }

            val file = File(downloadItem.filePath.ifBlank { DownloadStoragePaths.getDownloadFilePath(safeFileName) })
            val downloadDir = file.parentFile ?: DownloadStoragePaths.getDownloadDirectory()
            if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                emit(DownloadProgress(0, 0, DownloadStatus.FAILED, "Failed to create download directory"))
                return@flow
            }

            var downloadedBytes = if (file.exists()) file.length() else 0L

            val requestBuilder = Request.Builder()
                .url(downloadItem.url)
                .addHeader("User-Agent", "FileDownloadManager/1.0")

            if (downloadedBytes > 0) {
                requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
            }

            val call = okHttpClient.newCall(requestBuilder.build())
            var response: Response = call.execute()

            // Handle HTTP 416 (Range Not Satisfiable)
            if (response.code == 416) {
                response.close()
                if (file.exists()) file.delete()
                downloadedBytes = 0L

                val retryRequest = Request.Builder()
                    .url(downloadItem.url)
                    .addHeader("User-Agent", "FileDownloadManager/1.0")
                    .build()

                response = okHttpClient.newCall(retryRequest).execute()
            }

            response.use { resp ->
                // Validate Content-Type
                val contentType = resp.header("Content-Type")
                val mimeTypeValidation = ContentValidator.validateMimeType(contentType, downloadItem.url)
                if (!mimeTypeValidation.isValid) {
                    resp.close()
                    throw SecurityException(mimeTypeValidation.message)
                }


                if (!resp.isSuccessful && resp.code != 206) {
                    emit(DownloadProgress(0, 0, DownloadStatus.FAILED, "HTTP ${resp.code}: ${resp.message}"))
                    return@flow
                }

                val body = resp.body ?: run {
                    emit(DownloadProgress(0, 0, DownloadStatus.FAILED, "Empty response body"))
                    return@flow
                }

                val isPartial = resp.code == 206
                val contentLength = body.contentLength()

                // Reset downloaded byte offset if server ignored Range header and returned HTTP 200
                if (!isPartial) {
                    if (file.exists()) file.delete()
                    downloadedBytes = 0L
                }

                val totalBytes = if (isPartial && contentLength != -1L) {
                    downloadedBytes + contentLength
                } else if (contentLength > 0) {
                    contentLength
                } else {
                    downloadItem.totalSize
                }

                val remainingBytes = when {
                    totalBytes > 0 -> (totalBytes - downloadedBytes).coerceAtLeast(0L)
                    contentLength > 0 -> contentLength
                    else -> 0L
                }

                if (remainingBytes > 0 && !hasEnoughStorage(downloadDir, remainingBytes)) {
                    emit(DownloadProgress(downloadedBytes, totalBytes, DownloadStatus.FAILED, "Insufficient storage space"))
                    return@flow
                }

                if (totalBytes > 0) {
                    val sizeValidation = ContentValidator.validateFileSize(totalBytes)
                    if (!sizeValidation.isValid) {
                        emit(DownloadProgress(0, totalBytes, DownloadStatus.FAILED, sizeValidation.message))
                        return@flow
                    }
                }

                var currentDownloadedBytes = downloadedBytes
                emit(DownloadProgress(currentDownloadedBytes, totalBytes, DownloadStatus.DOWNLOADING))

                val buffer = ByteArray(BUFFER_SIZE_BYTES)
                var bytesRead: Int
                var bytesSinceLastEmit = 0L
                val emitThreshold = 100 * 1024 // 100 KB
                var isFirstBuffer = true

                body.byteStream().use { inputStream ->
                    FileOutputStream(file, isPartial).use { outputStream ->
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (!currentCoroutineContext().isActive) break
                            
                            // Check for magic numbers in the very first chunk
                            if (isFirstBuffer) {
                                if (!isPartial && bytesRead >= 2) {
                                    val b1 = buffer[0].toInt() and 0xFF
                                    val b2 = buffer[1].toInt() and 0xFF
                                    
                                    // MZ (0x4D 0x5A) or #! (0x23 0x21)
                                    if ((b1 == 0x4D && b2 == 0x5A) || (b1 == 0x23 && b2 == 0x21)) {
                                        outputStream.close()
                                        file.delete()
                                        throw SecurityException("Malicious file signature detected: $b1 $b2")
                                    }
                                }
                                isFirstBuffer = false
                            }

                            outputStream.write(buffer, 0, bytesRead)
                            currentDownloadedBytes += bytesRead
                            bytesSinceLastEmit += bytesRead

                            if (bytesSinceLastEmit >= emitThreshold) {
                                emit(DownloadProgress(currentDownloadedBytes, totalBytes, DownloadStatus.DOWNLOADING))
                                bytesSinceLastEmit = 0L
                            }
                        }
                        outputStream.flush()
                    }
                }

                emit(DownloadProgress(currentDownloadedBytes, totalBytes, DownloadStatus.COMPLETED))
            }
        } catch (e: SecurityException) {
            emit(DownloadProgress(0, downloadItem.totalSize, DownloadStatus.FAILED, e.message ?: "Security violation"))
        } catch (e: IOException) {
            val file = File(downloadItem.filePath.ifBlank { DownloadStoragePaths.getDownloadFilePath(safeFileName) })
            val currentSize = if (file.exists()) file.length() else 0L
            emit(DownloadProgress(currentSize, downloadItem.totalSize, DownloadStatus.FAILED, "Network error during download"))
        } catch (e: Exception) {
            val file = File(downloadItem.filePath.ifBlank { DownloadStoragePaths.getDownloadFilePath(safeFileName) })
            val currentSize = if (file.exists()) file.length() else 0L
            emit(DownloadProgress(currentSize, downloadItem.totalSize, DownloadStatus.FAILED, "Unexpected error during download"))
        }
    }.flowOn(Dispatchers.IO)

    fun getDownloadDirectory(): File {
        return DownloadStoragePaths.getDownloadDirectory().apply { mkdirs() }
    }

    private fun hasEnoughStorage(directory: File, requiredBytes: Long): Boolean {
        if (requiredBytes <= 0L) return true

        val statFs = StatFs(directory.absolutePath)
        return statFs.availableBytes > requiredBytes + STORAGE_SAFETY_MARGIN_BYTES
    }
}