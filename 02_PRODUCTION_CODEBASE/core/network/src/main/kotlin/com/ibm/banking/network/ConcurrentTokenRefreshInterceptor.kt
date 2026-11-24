package com.ibm.banking.network

import com.ibm.banking.model.AuthTokenBundle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

interface TokenProvider {
    fun getTokens(): AuthTokenBundle?
    suspend fun refreshTokens(oldRefreshToken: String): Result<AuthTokenBundle>
    fun onSessionExpired()
}

/**
 * Thread-safe OkHttp Interceptor that handles concurrent 401 Unauthorized errors.
 * Uses Kotlin Mutex to prevent the "Thundering Herd" problem when access tokens expire.
 */
class ConcurrentTokenRefreshInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    private val refreshMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentTokens = tokenProvider.getTokens()

        // 1. Attach authorization header if available and not explicitly skipped
        val initialRequest = if (currentTokens != null && !originalRequest.headers.names().contains("No-Auth")) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer ${currentTokens.accessToken}")
                .build()
        } else {
            originalRequest
        }

        val initialResponse = chain.proceed(initialRequest)

        // 2. If response is NOT 401, proceed normally
        if (initialResponse.code != 401 || currentTokens == null) {
            return initialResponse
        }

        // 3. Close the 401 response body before attempting retry
        initialResponse.close()

        // 4. Synchronize concurrent token refresh
        val updatedTokens = runBlocking {
            refreshMutex.withLock {
                val latestTokens = tokenProvider.getTokens()
                
                // If another thread already refreshed the token while this thread was waiting on the mutex,
                // check if the access token has already changed
                if (latestTokens != null && latestTokens.accessToken != currentTokens.accessToken && !latestTokens.isExpired) {
                    return@withLock latestTokens
                }

                // If token is still stale, execute the actual network refresh
                val refreshResult = tokenProvider.refreshTokens(currentTokens.refreshToken)
                if (refreshResult.isSuccess) {
                    refreshResult.getOrThrow()
                } else {
                    tokenProvider.onSessionExpired()
                    null
                }
            }
        }

        // 5. Retry request with new token or return terminal failure
        return if (updatedTokens != null) {
            val retryRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer ${updatedTokens.accessToken}")
                .build()
            chain.proceed(retryRequest)
        } else {
            // Re-execute original to produce appropriate error response upstream
            chain.proceed(initialRequest)
        }
    }
}
