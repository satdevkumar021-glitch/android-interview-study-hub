package com.ibm.banking.feature.atm

import com.ibm.banking.model.AccountId
import com.ibm.banking.model.AmountCents
import com.ibm.banking.model.AtmWithdrawalSession

/**
 * Model-View-Intent (MVI) Contract for Card-Free ATM Cash Withdrawal.
 */
sealed interface AtmIntent {
    data class SelectAccount(val accountId: AccountId) : AtmIntent
    data class EnterAmount(val amountCents: AmountCents) : AtmIntent
    object AuthenticateWithBiometrics : AtmIntent
    data class ConfirmWithdrawal(val terminalQrPayload: String) : AtmIntent
    object CancelSession : AtmIntent
}

data class AtmUiState(
    val selectedAccount: AccountId? = null,
    val withdrawalAmount: AmountCents = AmountCents(0L),
    val isLoading: Boolean = false,
    val activeSession: AtmWithdrawalSession? = null,
    val errorMessage: String? = null
) {
    val canProceedToBiometrics: Boolean
        get() = selectedAccount != null && withdrawalAmount.cents > 0L && !isLoading
}

sealed interface AtmSideEffect {
    data class ShowToast(val message: String) : AtmSideEffect
    object TriggerBiometricPrompt : AtmSideEffect
    data class NavigateToQrScanner(val sessionId: String) : AtmSideEffect
    data class DispenseCashSuccess(val sessionId: String, val amount: AmountCents) : AtmSideEffect
}
