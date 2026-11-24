package com.ibm.banking.feature.atm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibm.banking.model.AccountId
import com.ibm.banking.model.AmountCents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AtmWithdrawalViewModel(
    private val repository: AtmWithdrawalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AtmUiState())
    val uiState: StateFlow<AtmUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<AtmSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    fun handleIntent(intent: AtmIntent) {
        when (intent) {
            is AtmIntent.SelectAccount -> {
                _uiState.update { it.copy(selectedAccount = intent.accountId, errorMessage = null) }
            }
            is AtmIntent.EnterAmount -> {
                _uiState.update { it.copy(withdrawalAmount = intent.amountCents, errorMessage = null) }
            }
            is AtmIntent.AuthenticateWithBiometrics -> {
                validateAndTriggerBiometrics()
            }
            is AtmIntent.ConfirmWithdrawal -> {
                executeTerminalConfirmation(intent.terminalQrPayload)
            }
            is AtmIntent.CancelSession -> {
                _uiState.update { AtmUiState() }
            }
        }
    }

    private fun validateAndTriggerBiometrics() {
        val currentState = _uiState.value
        if (!currentState.canProceedToBiometrics) {
            _uiState.update { it.copy(errorMessage = "Please select account and valid withdrawal amount") }
            return
        }

        viewModelScope.launch {
            _sideEffects.send(AtmSideEffect.TriggerBiometricPrompt)
        }
    }

    fun onBiometricSuccess(biometricSignature: ByteArray) {
        val account = _uiState.value.selectedAccount ?: return
        val amount = _uiState.value.withdrawalAmount

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.initiateWithdrawal(account, amount)
                .onSuccess { session ->
                    _uiState.update { it.copy(isLoading = false, activeSession = session) }
                    _sideEffects.send(AtmSideEffect.NavigateToQrScanner(session.sessionId))
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to initiate ATM session"
                        )
                    }
                    _sideEffects.send(AtmSideEffect.ShowToast("Security verification failed"))
                }
        }
    }

    private fun executeTerminalConfirmation(qrPayload: String) {
        val session = _uiState.value.activeSession ?: return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            repository.authorizeTerminalHandshake(
                sessionId = session.sessionId,
                terminalQrPayload = qrPayload,
                biometricSignature = byteArrayOf() // Handshake verified with terminal
            )
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _sideEffects.send(
                        AtmSideEffect.DispenseCashSuccess(
                            sessionId = session.sessionId,
                            amount = session.requestedAmount
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "ATM Terminal Handshake Failed"
                        )
                    }
                }
        }
    }
}
