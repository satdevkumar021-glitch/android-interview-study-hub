package com.ibm.banking.feature.atm

import com.ibm.banking.model.AccountId
import com.ibm.banking.model.AmountCents
import com.ibm.banking.model.AtmWithdrawalSession

interface AtmWithdrawalRepository {
    suspend fun initiateWithdrawal(
        accountId: AccountId,
        amount: AmountCents
    ): Result<AtmWithdrawalSession>

    suspend fun authorizeTerminalHandshake(
        sessionId: String,
        terminalQrPayload: String,
        biometricSignature: ByteArray
    ): Result<Boolean>
}
