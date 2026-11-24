package com.ibm.banking.network

import com.ibm.banking.model.AuthTokenBundle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ConcurrentTokenRefreshInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private val tokenProvider: TokenProvider = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun intercept_concurrent401s_triggersOnlyOneTokenRefresh() = runBlocking {
        val refreshCallCount = AtomicInteger(0)
        var currentToken = AuthTokenBundle("STALE_TOKEN", "REFRESH_XYZ", 3600)
        val refreshedToken = AuthTokenBundle("FRESH_TOKEN", "REFRESH_XYZ", 3600)

        every { tokenProvider.getTokens() } answers { currentToken }

        coEvery { tokenProvider.refreshTokens("REFRESH_XYZ") } coAnswers {
            refreshCallCount.incrementAndGet()
            currentToken = refreshedToken
            Result.success(refreshedToken)
        }

        val interceptor = ConcurrentTokenRefreshInterceptor(tokenProvider)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        // Enqueue responses:
        // Request 1 fails 401, Request 2 fails 401
        // Then retry for Request 1 succeeds (200), retry for Request 2 succeeds (200)
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK_1"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK_2"))

        val url = mockWebServer.url("/api/account").toString()

        // Launch 2 concurrent requests
        val deferred1 = async(Dispatchers.IO) {
            client.newCall(Request.Builder().url(url).build()).execute()
        }
        val deferred2 = async(Dispatchers.IO) {
            client.newCall(Request.Builder().url(url).build()).execute()
        }

        val responses = awaitAll(deferred1, deferred2)

        responses.forEach { response ->
            assertEquals(200, response.code)
            response.close()
        }

        // Verify only 1 token refresh was executed even though 2 requests hit 401 simultaneously!
        assertEquals(1, refreshCallCount.get())
        coVerify(exactly = 1) { tokenProvider.refreshTokens("REFRESH_XYZ") }
    }
}
