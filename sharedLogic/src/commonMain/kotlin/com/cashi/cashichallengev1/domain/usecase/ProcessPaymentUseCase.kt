package com.cashi.cashichallengev1.domain.usecase

import com.cashi.cashichallengev1.domain.model.Transaction
import com.cashi.cashichallengev1.domain.repository.TransactionRepository
import com.cashi.cashichallengev1.data.network.PaymentService
import com.cashi.cashichallengev1.data.network.NetworkResult
import com.cashi.cashichallengev1.validator.PaymentValidator
import com.cashi.cashichallengev1.validator.ValidationResult

class ProcessPaymentUseCase(
    private val paymentService: PaymentService,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(email: String, amount: Double, currency: String): NetworkResult<Transaction> {
        val validation = PaymentValidator.validate(email, amount, currency)
        if (validation is ValidationResult.Invalid) {
            return NetworkResult.Failure(Exception("Validation failed"))
        }

        return when (val result = paymentService.processPayment(email, amount, currency)) {
            is NetworkResult.Success -> {
                transactionRepository.saveTransaction(result.data)
                result
            }
            is NetworkResult.Failure -> result
        }
    }
}
