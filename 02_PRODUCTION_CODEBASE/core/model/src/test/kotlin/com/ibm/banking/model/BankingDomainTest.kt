package com.ibm.banking.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BankingDomainTest {

    @Test
    fun amountCents_formatsDollarsAndCentsCorrectly() {
        val amount = AmountCents(12550L) // $125.50
        assertEquals("USD 125.50", amount.toDisplayString("USD"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun amountCents_negativeValue_throwsException() {
        AmountCents(-500L)
    }

    @Test
    fun atmSession_validSession_isNotExpired() {
        val session = AtmWithdrawalSession(
            sessionId = "ATM_SES_001",
            accountId = AccountId("ACC_987654"),
            requestedAmount = AmountCents(20000L),
            oneTimeOtp = "482910",
            qrChallengeCode = "CHALLENGE_QR_XYZ",
            expiryTimestamp = System.currentTimeMillis() + 300_000L // 5 mins in future
        )
        assertFalse(session.isExpired)
    }

    @Test
    fun authTokenBundle_nearExpiry_reportsExpiredWithinSafetyBuffer() {
        val token = AuthTokenBundle(
            accessToken = "eyJhbGciOi...",
            refreshToken = "dGhpcy1pcy...",
            expiresInSeconds = 50L, // 50 seconds lifetime, but buffer is 60s
            issuedAtEpochMs = System.currentTimeMillis()
        )
        // With 60s buffer, a token with 50s total expiry is considered expired immediately to trigger proactive refresh
        assertTrue(token.isExpired)
    }
}
