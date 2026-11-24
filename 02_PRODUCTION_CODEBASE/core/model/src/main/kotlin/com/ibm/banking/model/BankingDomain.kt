package com.ibm.banking.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class AccountId(val value: String) {
    init {
        require(value.isNotBlank()) { "AccountId cannot be blank" }
    }
}

@JvmInline
@Serializable
value class TransactionId(val value: String) {
    init {
        require(value.isNotBlank()) { "TransactionId cannot be blank" }
    }
}

@JvmInline
@Serializable
value class AmountCents(val cents: Long) {
    init {
        require(cents >= 0) { "Amount cannot be negative" }
    }

    fun toDisplayString(currency: String = "USD"): String {
        val dollars = cents / 100
        val remainder = cents % 100
        return String.format("%s %d.%02d", currency, dollars, remainder)
    }
}

enum class TransactionStatus {
    PENDING,
    AUTHORIZED,
    COMPLETED,
    DECLINED,
    EXPIRED
}

@Serializable
data class BankingTransaction(
    val id: TransactionId,
    val sourceAccount: AccountId,
    val amount: AmountCents,
    val status: TransactionStatus,
    val description: String,
    val timestamp: Long
)

@Serializable
data class AtmWithdrawalSession(
    val sessionId: String,
    val accountId: AccountId,
    val requestedAmount: AmountCents,
    val oneTimeOtp: String,
    val qrChallengeCode: String,
    val expiryTimestamp: Long,
    val terminalId: String? = null
) {
    val isExpired: Boolean get() = System.currentTimeMillis() > expiryTimestamp
}

@Serializable
data class AuthTokenBundle(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val issuedAtEpochMs: Long = System.currentTimeMillis()
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= (issuedAtEpochMs + (expiresInSeconds * 1000L) - BUFFER_MS)

    companion object {
        private const val BUFFER_MS = 60_000L // 60s safety buffer before hard expiry
    }
}
