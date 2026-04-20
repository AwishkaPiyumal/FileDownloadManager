package com.piumal.filedownloadmanager.data.download

import android.content.Context
import android.os.Environment
import android.util.Log
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.util.ContentValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Download Manager Implementation
 *
 * Handles actual file downloads using OkHttp
 * Google Policy Compliant:
 * - Validates content before download
 * - Respects file size limits
 * - Provides download progress
 * - Handles errors gracefully
 *
 * @param context Application context
 */
@Singleton
class DownloadManager @Inject constructor(
    private val context: Context
) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Download state to track progress
     */
    data class DownloadProgress(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val status: DownloadStatus,
        val error: String? = null
    )

    /**
     * Start download with progress updates
     * Supports resumable downloads using HTTP Range header
     *
     * @param downloadItem Download item to download
     * @return Flow of download progress
     */
    fun downloadFile(downloadItem: DownloadItem): Flow<DownloadProgress> = flow {
        try {
            Log.d("DownloadManager", "=== Starting download ===")
            Log.d("DownloadManager", "URL: ${downloadItem.url}")
            Log.d("DownloadManager", "FileName: ${downloadItem.fileName}")

            // Validate URL again before download
            val validation = ContentValidator.validateDownloadUrl(downloadItem.url)
            Log.d("DownloadManager", "URL validation: ${validation.isValid} - ${validation.message}")
            if (!validation.isValid) {
                emit(DownloadProgress(
                    downloadedBytes = 0,
                    totalBytes = 0,
                    status = DownloadStatus.FAILED,
                    error = validation.message
                ))
                return@flow
            }

            // Create download directory
            val downloadDir = getDownloadDirectory()
            Log.d("DownloadManager", "Download directory: ${downloadDir.absolutePath}")
            if (!downloadDir.exists()) {
                val created = downloadDir.mkdirs()
                Log.d("DownloadManager", "Directory created: $created")
                if (!created) {
                    emit(DownloadProgress(
                        downloadedBytes = 0,
                        totalBytes = 0,
                        status = DownloadStatus.FAILED,
                        error = "Failed to create download directory"
                    ))
                    return@flow
                }
            }

            // Create file
            val file = File(downloadDir, downloadItem.fileName)
            Log.d("DownloadManager", "Target file: ${file.absolutePath}")

            // Check if file partially exists (for resume support)
            val downloadedBytes = if (file.exists()) file.length() else 0L
            Log.d("DownloadManager", "Already downloaded: $downloadedBytes bytes")

            // Build request with Range header for resume support
            val requestBuilder = Request.Builder()
                .url(downloadItem.url)
                .addHeader("User-Agent", "FileDownloadManager/1.0")

            // Add Range header if resuming
            if (downloadedBytes > 0) {
                requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
                Log.d("DownloadManager", "Resuming download from byte: $downloadedBytes")
            }

            val request = requestBuilder.build()

            // Execute request
            Log.d("DownloadManager", "Executing HTTP request...")
            var response = okHttpClient.newCall(request).execute()
            Log.d("DownloadManager", "Response code: ${response.code}")
            Log.d("DownloadManager", "Response message: ${response.message}")

            // Handle HTTP 416 (Range Not Satisfiable) - file may have changed on server
            if (response.code == 416) {
                Log.w("DownloadManager", "HTTP 416 (Range Not Satisfiable) - Server file may have changed. Restarting download from beginning.")
                // Delete the partial file and restart download from 0
                if (file.exists()) {
                    file.delete()
                    Log.d("DownloadManager", "Partial file deleted, restarting download")
                }

                // Make a new request without Range header
                val newRequest = Request.Builder()
                    .url(downloadItem.url)
                    .addHeader("User-Agent", "FileDownloadManager/1.0")
                    .build()

                response = okHttpClient.newCall(newRequest).execute()
                Log.d("DownloadManager", "Retry response code: ${response.code}")

                if (!response.isSuccessful) {
                    Log.e("DownloadManager", "Retry failed with HTTP error: ${response.code} - ${response.message}")
                    emit(DownloadProgress(
                        downloadedBytes = 0,
                        totalBytes = 0,
                        status = DownloadStatus.FAILED,
                        error = "HTTP ${response.code}: ${response.message}"
                    ))
                    return@flow
                }
            }

            // Check response code (200 = new download, 206 = partial/resume)
            if (!response.isSuccessful && response.code != 206) {
                Log.e("DownloadManager", "HTTP error: ${response.code} - ${response.message}")
                emit(DownloadProgress(
                    downloadedBytes = 0,
                    totalBytes = 0,
                    status = DownloadStatus.FAILED,
                    error = "HTTP ${response.code}: ${response.message}"
                ))
                return@flow
            }

            val body = response.body ?: run {
                Log.e("DownloadManager", "Empty response body")
                emit(DownloadProgress(
                    downloadedBytes = 0,
                    totalBytes = 0,
                    status = DownloadStatus.FAILED,
                    error = "Empty response body"
                ))
                return@flow
            }

            // Get content length
            val contentLength = body.contentLength()
            val totalBytes = if (response.code == 206) {
                // Partial download, add already downloaded bytes
                downloadedBytes + contentLength
            } else {
                // New download
                if (file.exists()) file.delete() // Delete existing file
                contentLength
            }

            Log.d("DownloadManager", "Content length: $contentLength")
            Log.d("DownloadManager", "Total bytes: $totalBytes")

            // Validate file size
            if (totalBytes > 0) {
                val sizeValidation = ContentValidator.validateFileSize(totalBytes)
                Log.d("DownloadManager", "Size validation: ${sizeValidation.isValid} - ${sizeValidation.message}")
                if (!sizeValidation.isValid) {
                    emit(DownloadProgress(
                        downloadedBytes = 0,
                        totalBytes = totalBytes,
                        status = DownloadStatus.FAILED,
                        error = sizeValidation.message
                    ))
                    return@flow
                }
            }

            // Open streams (append if resuming)
            Log.d("DownloadManager", "Opening file streams...")
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file, response.code == 206) // Append if 206

            val buffer = ByteArray(8192) // 8KB buffer
            var currentDownloadedBytes = downloadedBytes
            var bytesRead: Int

            // Emit initial progress
            Log.d("DownloadManager", "Starting download loop...")
            emit(DownloadProgress(
                downloadedBytes = currentDownloadedBytes,
                totalBytes = totalBytes,
                status = DownloadStatus.DOWNLOADING
            ))

            // Download loop
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                currentDownloadedBytes += bytesRead

                // Emit progress every 100KB
                if (currentDownloadedBytes % (100 * 1024) < 8192 || bytesRead == -1) {
                    emit(DownloadProgress(
                        downloadedBytes = currentDownloadedBytes,
                        totalBytes = totalBytes,
                        status = DownloadStatus.DOWNLOADING
                    ))
                }
            }

            // Close streams
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Log.d("DownloadManager", "Download completed successfully")

            // Emit completion
            emit(DownloadProgress(
                downloadedBytes = currentDownloadedBytes,
                totalBytes = totalBytes,
                status = DownloadStatus.COMPLETED
            ))

        } catch (e: IOException) {
            Log.e("DownloadManager", "IOException during download", e)
            // Get current file size if it exists (preserve progress)
            val file = File(getDownloadDirectory(), downloadItem.fileName)
            val currentSize = if (file.exists()) file.length() else 0L

            emit(DownloadProgress(
                downloadedBytes = currentSize,
                totalBytes = downloadItem.totalSize,
                status = DownloadStatus.FAILED,
                error = "Network error - Download paused. You can resume when network is restored."
            ))
        } catch (e: Exception) {
            Log.e("DownloadManager", "Exception during download", e)
            // Get current file size if it exists (preserve progress)
            val file = File(getDownloadDirectory(), downloadItem.fileName)
            val currentSize = if (file.exists()) file.length() else 0L

            emit(DownloadProgress(
                downloadedBytes = currentSize,
                totalBytes = downloadItem.totalSize,
                status = DownloadStatus.FAILED,
                error = "Error: ${e.message} - You can retry the download."
            ))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get download directory
     * Uses public Downloads folder so users can easily find their downloaded files
     * This requires WRITE_EXTERNAL_STORAGE permission on Android 9 and below
     * On Android 10+ with requestLegacyExternalStorage=true or Android 11+ with MANAGE_EXTERNAL_STORAGE
     */
    fun getDownloadDirectory(): File {
        // Always use public Downloads folder for user accessibility
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        return File(downloadsDir, "FileDownloadManager")
    }

    /**
     * Get file size from URL (HEAD request)
     * Used to show file size before download
     */
    suspend fun getFileSize(url: String): Long {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.body?.contentLength() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Check if URL is accessible
     */
    suspend fun isUrlAccessible(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

