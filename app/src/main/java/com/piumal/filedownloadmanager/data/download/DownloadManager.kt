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
     *
     * @param downloadItem Download item to download
     * @return Flow of download progress
     */
    fun downloadFile(downloadItem: DownloadItem): Flow<DownloadProgress> = flow {
        try {
            // Validate URL again before download
            val validation = ContentValidator.validateDownloadUrl(downloadItem.url)
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
            if (!downloadDir.exists()) {
                val created = downloadDir.mkdirs()
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

            // Check if file already exists
            if (file.exists()) {
                file.delete()
            }

            // Build request
            val request = Request.Builder()
                .url(downloadItem.url)
                .addHeader("User-Agent", "FileDownloadManager/1.0")
                .build()

            // Execute request
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                emit(DownloadProgress(
                    downloadedBytes = 0,
                    totalBytes = 0,
                    status = DownloadStatus.FAILED,
                    error = "HTTP ${response.code}: ${response.message}"
                ))
                return@flow
            }

            val body = response.body ?: run {
                emit(DownloadProgress(
                    downloadedBytes = 0,
                    totalBytes = 0,
                    status = DownloadStatus.FAILED,
                    error = "Empty response body"
                ))
                return@flow
            }

            val contentLength = body.contentLength()

            // Validate file size
            if (contentLength > 0) {
                val sizeValidation = ContentValidator.validateFileSize(contentLength)
                if (!sizeValidation.isValid) {
                    emit(DownloadProgress(
                        downloadedBytes = 0,
                        totalBytes = contentLength,
                        status = DownloadStatus.FAILED,
                        error = sizeValidation.message
                    ))
                    return@flow
                }
            }

            // Start downloading
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(8192) // 8KB buffer
            var downloadedBytes = 0L
            var bytesRead: Int

            // Emit initial progress
            emit(DownloadProgress(
                downloadedBytes = 0,
                totalBytes = contentLength,
                status = DownloadStatus.DOWNLOADING
            ))

            // Download loop
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead

                // Emit progress every 100KB
                if (downloadedBytes % (100 * 1024) == 0L || bytesRead == -1) {
                    emit(DownloadProgress(
                        downloadedBytes = downloadedBytes,
                        totalBytes = contentLength,
                        status = DownloadStatus.DOWNLOADING
                    ))
                }
            }

            // Close streams
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Emit completion
            emit(DownloadProgress(
                downloadedBytes = downloadedBytes,
                totalBytes = contentLength,
                status = DownloadStatus.COMPLETED
            ))

        } catch (e: IOException) {
            Log.e("DownloadManager", "IOException during download", e)
            emit(DownloadProgress(
                downloadedBytes = 0,
                totalBytes = 0,
                status = DownloadStatus.FAILED,
                error = "Network error: ${e.message}"
            ))
        } catch (e: Exception) {
            Log.e("DownloadManager", "Exception during download", e)
            emit(DownloadProgress(
                downloadedBytes = 0,
                totalBytes = 0,
                status = DownloadStatus.FAILED,
                error = "Error: ${e.message}"
            ))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get download directory
     * Uses app-specific external storage for Android 10+ (doesn't require permissions)
     * Uses public Downloads folder for older Android versions
     */
    private fun getDownloadDirectory(): File {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+): Use app-specific external storage (no permission needed)
            val appDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            File(appDownloadsDir, "FileDownloadManager")
        } else {
            // Android 9 and below: Use public Downloads folder (requires permission)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            File(downloadsDir, "FileDownloadManager")
        }
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

