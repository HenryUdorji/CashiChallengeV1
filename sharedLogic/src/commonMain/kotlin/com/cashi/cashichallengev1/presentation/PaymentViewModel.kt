package com.cashi.cashichallengev1.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashi.cashichallengev1.domain.model.Transaction
import com.cashi.cashichallengev1.data.network.NetworkResult
import com.cashi.cashichallengev1.domain.usecase.ObserveTransactionsUseCase
import com.cashi.cashichallengev1.domain.usecase.ProcessPaymentUseCase
import com.cashi.cashichallengev1.validator.PaymentValidator
import com.cashi.cashichallengev1.validator.ValidationResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PaymentState(
    val recipientEmail: String = "",
    val amountCents: Long = 0L,
    val currency: String = "USD",
    val emailError: String? = null,
    val amountError: String? = null,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val transactions: List<Transaction> = emptyList()
)

sealed interface PaymentIntent {
    data class EmailChanged(val email: String) : PaymentIntent
    data class AmountChanged(val amountCents: Long) : PaymentIntent
    data class CurrencyChanged(val currency: String) : PaymentIntent
    object SubmitPayment : PaymentIntent
    object ClearMessage : PaymentIntent
}

class PaymentViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            observeTransactionsUseCase()
                .catch { e ->
                    _state.update { it.copy(userMessage = "Failed to load transactions: ${e.message}") }
                }
                .collect { list ->
                    _state.update { it.copy(transactions = list) }
                }
        }
    }

    fun handleIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.EmailChanged -> {
                _state.update { 
                    it.copy(
                        recipientEmail = intent.email,
                        emailError = if (PaymentValidator.validateEmail(intent.email) || intent.email.isEmpty()) null else "Invalid email address format."
                    )
                }
            }
            is PaymentIntent.AmountChanged -> {
                _state.update {
                    val amountVal = intent.amountCents / 100.0
                    it.copy(
                        amountCents = intent.amountCents,
                        amountError = if (amountVal > 0.0) null else "Amount must be greater than 0."
                    )
                }
            }
            is PaymentIntent.CurrencyChanged -> {
                _state.update {
                    it.copy(
                        currency = intent.currency
                    )
                }
            }
            is PaymentIntent.SubmitPayment -> {
                submitPayment()
            }
            is PaymentIntent.ClearMessage -> {
                _state.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun submitPayment() {
        val currentState = _state.value
        val email = currentState.recipientEmail
        val amountCents = currentState.amountCents
        val currency = currentState.currency
        val amount = amountCents / 100.0

        val validation = PaymentValidator.validate(email, amount, currency)
        if (validation is ValidationResult.Invalid) {
            _state.update {
                it.copy(
                    emailError = if (!PaymentValidator.validateEmail(email)) "Invalid email address format." else null,
                    amountError = if (amount <= 0.0) "Amount must be greater than 0." else null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, userMessage = null) }

            when (val result = processPaymentUseCase(email, amount, currency)) {
                is NetworkResult.Success -> {
                    _state.update {
                        it.copy(
                            recipientEmail = "",
                            amountCents = 0L,
                            isLoading = false,
                            userMessage = "Payment of $amount $currency sent successfully!"
                        )
                    }
                }

                is NetworkResult.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            userMessage = "Payment failed: ${result.error.message}"
                        )
                    }
                }
            }
        }
    }
}
