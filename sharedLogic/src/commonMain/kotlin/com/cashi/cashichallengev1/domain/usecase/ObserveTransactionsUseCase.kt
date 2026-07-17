package com.cashi.cashichallengev1.domain.usecase

import com.cashi.cashichallengev1.domain.model.Transaction
import com.cashi.cashichallengev1.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.observeTransactions()
    }
}
