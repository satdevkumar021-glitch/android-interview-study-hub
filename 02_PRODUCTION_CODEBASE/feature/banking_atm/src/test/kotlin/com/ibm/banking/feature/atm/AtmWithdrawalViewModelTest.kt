package com.ibm.banking.feature.atm

import app.cash.turbine.test
import com.ibm.banking.model.AccountId
import com.ibm.banking.model.AmountCents
import com.ibm.banking.model.AtmWithdrawalSession
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AtmWithdrawalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: AtmWithdrawalRepository = mockk(relaxed = true)
    private lateinit var viewModel: AtmWithdrawalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AtmWithdrawalViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun handleIntent_selectAccountAndAmount_updatesStateCorrectly() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(null, initial.selectedAccount)
            assertEquals(0L, initial.withdrawalAmount.cents)

            viewModel.handleIntent(AtmIntent.SelectAccount(AccountId("ACC_123")))
            val accountSelected = awaitItem()
            assertEquals(AccountId("ACC_123"), accountSelected.selectedAccount)

            viewModel.handleIntent(AtmIntent.EnterAmount(AmountCents(10000L)))
            val amountEntered = awaitItem()
            assertEquals(10000L, amountEntered.withdrawalAmount.cents)
            assertTrue(amountEntered.canProceedToBiometrics)
        }
    }

    @Test
    fun onBiometricSuccess_successFromRepository_emitsNavigateEffectAndUpdatesActiveSession() = runTest {
        val account = AccountId("ACC_123")
        val amount = AmountCents(20000L)
        val mockSession = AtmWithdrawalSession(
            sessionId = "ATM_SES_999",
            accountId = account,
            requestedAmount = amount,
            oneTimeOtp = "123456",
            qrChallengeCode = "QR_CODE_PAYLOAD",
            expiryTimestamp = System.currentTimeMillis() + 60000L
        )

        coEvery { repository.initiateWithdrawal(account, amount) } returns Result.success(mockSession)

        viewModel.handleIntent(AtmIntent.SelectAccount(account))
        viewModel.handleIntent(AtmIntent.EnterAmount(amount))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sideEffects.test {
            viewModel.onBiometricSuccess(byteArrayOf(1, 2, 3))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is AtmSideEffect.NavigateToQrScanner)
            assertEquals("ATM_SES_999", (effect as AtmSideEffect.NavigateToQrScanner).sessionId)
        }

        assertEquals(mockSession, viewModel.uiState.value.activeSession)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
