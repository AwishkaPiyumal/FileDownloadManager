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
import kotlinx.coroutines.Job
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

import com.piumal.filedownloadmanager.storage.StorageManager
import com.piumal.filedownloadmanager.storage.StorageManagerImpl
import dagger.hilt.android.qualifiers.ApplicationContext

// ... imports

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageManager: StorageManager
) {
// ...

    private companion object {
        private const val BUFFER_SIZE_BYTES = 64 * 1024
        private const val STORAGE_SAFETY_MARGIN_BYTES = 32L * 1024 * 1024
    }

    private val okHttpClient = createSecureDownloadHttpClient()

    data class DownloadProgress(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val status: DownloadStatus,
        val error: String? = null
    )

    fun downloadFile(downloadItem: DownloadItem): Flow<DownloadProgress> = flow {
        val safeFileName = FileNameSanitizer.sanitize(downloadItem.fileName)
        try {
            if (!ContentValidator.isSecureConnection(downloadItem.url)) {
                emit(DownloadProgress(0, 0, DownloadStatus.FAILED, "Only HTTPS URLs are supported"))
                return@flow
            }
            val validation = ContentValidator.validateDownloadUrl(downloadItem.url)
            if (!validation.isValid) {
                emit(DownloadProgress(0, 0, DownloadStatus.FAILED, validation.message))
                return@flow
            }

            val uriString = downloadItem.uri ?: downloadItem.filePath
            val isSaf = uriString.startsWith("content://")

            val file = if (!isSaf) File(uriString.ifBlank { DownloadStoragePaths.getDownloadFilePath(safeFileName) }) else null
            
            // Only perform directory checks and storage space checks for local files for now
            var downloadDir: File? = null
            if (!isSaf && file != null) {
                downloadDir = file.parentFile ?: DownloadStoragePaths.getDownloadDirectory()
                if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                    emit(DownloadProgress(0, 0, DownloadStatus.FAILED, "Failed to create download directory"))
                    return@flow
                }
            }

            var downloadedBytes = if (storageManager.exists(uriString)) storageManager.getLength(uriString) else 0L

            val requestBuilder = Request.Builder()
                .url(downloadItem.url)
                .addHeader("User-Agent", "FileDownloadManager/1.0")

            if (downloadedBytes > 0) {
                requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
            }

            val call = okHttpClient.newCall(requestBuilder.build())
            
            // Add cancellation support
            val job = currentCoroutineContext()[Job]
            job?.invokeOnCompletion { 
                call.cancel()
            }
            
            var response: Response = call.execute()

            // Handle HTTP 416 (Range Not Satisfiable)
            if (response.code == 416) {
                response.close()
                storageManager.delete(uriString)
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
                    storageManager.delete(uriString)
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

                if (remainingBytes > 0 && downloadDir != null && !hasEnoughStorage(downloadDir, remainingBytes)) {
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
                    val uriString = downloadItem.uri ?: downloadItem.filePath
                    val outputStream = storageManager.getOutputStream(uriString, isPartial) 
                        ?: throw IOException("Failed to open output stream")
                    
                    outputStream.use { stream ->
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (!currentCoroutineContext().isActive) break
                            
                            // Check for magic numbers in the very first chunk
                            if (isFirstBuffer) {
                                isFirstBuffer = false
                                if (ContentValidator.isMaliciousSignature(buffer)) {
                                    throw SecurityException("Malicious file signature detected.")
                                }
                            }

                            stream.write(buffer, 0, bytesRead)
                            currentDownloadedBytes += bytesRead
                            bytesSinceLastEmit += bytesRead

                            // Enforce file size limit during streaming
                            if (currentDownloadedBytes > ContentValidator.getMaxFileSize()) {
                                throw IOException("File size exceeded maximum limit (5 GB).")
                            }

                            if (bytesSinceLastEmit >= emitThreshold) {
                                emit(DownloadProgress(currentDownloadedBytes, totalBytes, DownloadStatus.DOWNLOADING))
                                bytesSinceLastEmit = 0L
                            }
                        }
                        stream.flush()
                    }
                }

                emit(DownloadProgress(currentDownloadedBytes, totalBytes, DownloadStatus.COMPLETED))
            }
        } catch (e: SecurityException) {
            val uriString = downloadItem.uri ?: downloadItem.filePath
            storageManager.delete(uriString)
            emit(DownloadProgress(0, downloadItem.totalSize, DownloadStatus.FAILED, e.message ?: "Security violation"))
        }
 catch (e: IOException) {
            val uriString = downloadItem.uri ?: downloadItem.filePath
            val currentSize = if (storageManager.exists(uriString)) storageManager.getLength(uriString) else 0L
            emit(DownloadProgress(currentSize, downloadItem.totalSize, DownloadStatus.FAILED, "Network error during download"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) {
                // Handle cancellation: delete partial file
                val uriString = downloadItem.uri ?: downloadItem.filePath
                storageManager.delete(uriString)
                emit(DownloadProgress(0, downloadItem.totalSize, DownloadStatus.CANCELLED))
            } else {
                val uriString = downloadItem.uri ?: downloadItem.filePath
                val currentSize = if (storageManager.exists(uriString)) storageManager.getLength(uriString) else 0L
                emit(DownloadProgress(currentSize, downloadItem.totalSize, DownloadStatus.FAILED, "Unexpected error during download"))
            }
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

/**
 * Decides whether a redirect from [requestUrl] to [locationHeader] should be blocked.
 *
 * Returns a rejection reason (suitable for throwing as an IOException message) if the redirect
 * must be blocked, or `null` if it's allowed. This is deliberately a pure function of plain
 * values - no OkHttp chain/response involved - so it can be unit tested directly (see
 * RedirectSecurityTest) without needing to stand up a real HTTPS MockWebServer, which is
 * meaningfully more setup and was never actually done here (the previous test targeted a
 * plain-HTTP MockWebServer, so the one HTTPS-downgrade case it claimed to cover was never
 * really exercised end to end).
 */
private val URI_SCHEME_PATTERN = Regex("^([a-zA-Z][a-zA-Z0-9+.\\-]*):")

internal fun resolveRedirectRejection(requestUrl: okhttp3.HttpUrl, locationHeader: String): String? {
    // A Location value with its own explicit scheme (e.g. "file:///etc/passwd",
    // "javascript:...", "content://...") is absolute and must never be resolved against
    // requestUrl. We detect that directly via RFC 3986 scheme syntax rather than relying on
    // HttpUrl.resolve(), which only understands http/https and simply returns null for
    // anything else - which would otherwise fall through to "can't resolve => allow" below and
    // silently let an unsupported-scheme redirect through instead of rejecting it.
    val explicitScheme = URI_SCHEME_PATTERN.find(locationHeader)?.groupValues?.get(1)

    if (explicitScheme != null &&
        !explicitScheme.equals("http", ignoreCase = true) &&
        !explicitScheme.equals("https", ignoreCase = true)
    ) {
        return "Redirect to unsupported scheme is not allowed"
    }

    val finalUrl = if (explicitScheme != null) {
        locationHeader
    } else {
        // No explicit scheme => a relative reference; resolve it against the request URL, which
        // is always http/https (that's the only thing HttpUrl can represent). A null result here
        // means the value couldn't be parsed as a relative reference at all, so fail closed.
        requestUrl.resolve(locationHeader)?.toString()
            ?: return "Redirect to unsupported scheme is not allowed"
    }

    if (requestUrl.isHttps && finalUrl.startsWith("http://", ignoreCase = true)) {
        return "HTTPS to HTTP redirect is not allowed"
    }

    return null
}

/**
 * Builds the OkHttpClient used for all downloads, including the redirect-scheme guard.
 *
 * This is a top-level `internal` function (rather than inline inside [DownloadManager]) so
 * tests can build and exercise the *exact* production client instead of keeping a
 * hand-maintained duplicate that can silently drift out of sync with the real thing - which is
 * what let the redirect-downgrade protection ship non-functional in the first place
 * (RedirectSecurityTest previously built its own separate client with an empty validation stub).
 */
internal fun createSecureDownloadHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .followRedirects(true)
    // Belt-and-suspenders: never silently follow a redirect that changes http<->https.
    // Combined with the network interceptor below, which additionally blocks redirects to
    // non-http(s) schemes (file://, content://, javascript:, ...).
    .followSslRedirects(false)
    .addNetworkInterceptor { chain ->
        // A NETWORK interceptor (not an application interceptor / addInterceptor) is required
        // here: addInterceptor() only ever sees the final response after OkHttp has already
        // followed any redirects internally, so response.isRedirect there would essentially
        // never be true. addNetworkInterceptor() runs once per physical request, including the
        // request to a redirect's target, before OkHttp decides to follow it.
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.isRedirect) {
            response.header("Location")?.let { location ->
                resolveRedirectRejection(request.url, location)?.let { reason ->
                    response.close()
                    throw IOException(reason)
                }
            }
        }

        response
    }
    .build()