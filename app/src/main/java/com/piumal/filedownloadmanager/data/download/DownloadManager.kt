package com.piumal.filedownloadmanager.data.download

import android.content.Context
import android.os.Environment
import android.util.Log
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.domain.util.ContentValidator
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
        try {
            Log.d("DownloadManager", "Starting download: ${downloadItem.fileName}")

            val validation = ContentValidator.validateDownloadUrl(downloadItem.url)
            if (!validation.isValid) {
                emit(DownloadProgress(0, 0, DownloadStatus.FAILED, validation.message))
                return@flow
            }

            val downloadDir = getDownloadDirectory()
            if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                emit(DownloadProgress(0, 0, DownloadStatus.FAILED, "Failed to create download directory"))
                return@flow
            }

            val file = File(downloadDir, downloadItem.fileName)
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
                } else {
                    contentLength
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

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var bytesSinceLastEmit = 0L
                val emitThreshold = 100 * 1024 // 100 KB

                body.byteStream().use { inputStream ->
                    FileOutputStream(file, isPartial).use { outputStream ->
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (!currentCoroutineContext().isActive) break

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
        } catch (e: IOException) {
            val file = File(getDownloadDirectory(), downloadItem.fileName)
            val currentSize = if (file.exists()) file.length() else 0L
            emit(DownloadProgress(currentSize, downloadItem.totalSize, DownloadStatus.FAILED, "Network error: ${e.message}"))
        } catch (e: Exception) {
            val file = File(getDownloadDirectory(), downloadItem.fileName)
            val currentSize = if (file.exists()) file.length() else 0L
            emit(DownloadProgress(currentSize, downloadItem.totalSize, DownloadStatus.FAILED, "Error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getDownloadDirectory(): File {
        // App-specific external directory guarantees write access across Android 5.0 to 14+ without legacy storage flags
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads").apply { mkdirs() }
    }

    suspend fun getFileSize(url: String): Long = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).head().build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.contentLength() ?: 0L else 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun isUrlAccessible(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).head().build()
            okHttpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}