package com.cashi.cashichallengev1.data.repository

import com.cashi.cashichallengev1.domain.model.Transaction
import com.cashi.cashichallengev1.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

expect class PlatformFirestore() : TransactionRepository {
    override fun observeTransactions(): Flow<List<Transaction>>
    override suspend fun saveTransaction(transaction: Transaction)
}
