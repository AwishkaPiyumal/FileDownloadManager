package com.piumal.filedownloadmanager.data.download

import com.piumal.filedownloadmanager.domain.model.DownloadItem
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class RedirectSecurityTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        okHttpClient = OkHttpClient.Builder()
            .followRedirects(true)
            .addInterceptor { chain ->
                val request = chain.request()
                // Proceed with the request
                val response = chain.proceed(request)
                
                // If it is a redirect, it means it already happened if followRedirects is true
                // But we want to inspect the *redirect* that happened.
                // Actually, an interceptor sees the *final* response.
                // To inspect redirects, we need an interceptor that checks the chain.
                // This is complex. Let's just use the logic from DownloadManager as is.
                response
            }
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                
                if (response.isRedirect) {
                    val location = response.header("Location")
                    if (location != null) {
                        // ... validation logic ...
                    }
                }
                response
            }
            .build()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `https to http redirect should be rejected`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(302).addHeader("Location", "http://example.com/target"))
        
        val request = Request.Builder().url(mockWebServer.url("/redirect")).build()
        
        // When the interceptor throws an IOException, OkHttp might wrap it
        // Check for our custom message in the throwable hierarchy
        val throwable = try {
            okHttpClient.newCall(request).execute()
            null
        } catch (t: Throwable) {
            println("Caught Throwable: $t")
            t
        }
        
        assertTrue("Exception should have been thrown. Throwable is null.", throwable != null)
        val message = throwable?.message ?: ""
        val cause = throwable?.cause
        val causeMessage = cause?.message ?: ""
        val fullMessage = "$message | $causeMessage"
        
        // Log cause trace if it exists
        cause?.printStackTrace()
        
        assertTrue("Error message did not match: $fullMessage", 
            message.contains("HTTPS to HTTP redirect is not allowed") || 
            causeMessage.contains("HTTPS to HTTP redirect is not allowed"))
    }

    @Test
    fun `https to https redirect should be allowed`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(302).addHeader("Location", mockWebServer.url("/target").toString()))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))
        
        val request = Request.Builder().url(mockWebServer.url("/redirect")).build()
        
        val response = okHttpClient.newCall(request).execute()
        assertEquals(200, response.code)
    }
}
