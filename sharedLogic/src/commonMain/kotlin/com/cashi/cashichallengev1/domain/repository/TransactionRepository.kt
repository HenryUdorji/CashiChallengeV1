package com.cashi.cashichallengev1.domain.repository

import com.cashi.cashichallengev1.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    suspend fun saveTransaction(transaction: Transaction)
}
