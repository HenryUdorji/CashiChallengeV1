package com.cashi.cashichallengev1.data.repository

import com.cashi.cashichallengev1.domain.model.Transaction
import com.cashi.cashichallengev1.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class PlatformFirestore actual constructor() : TransactionRepository {
    companion object {
        private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    }

    actual override fun observeTransactions(): Flow<List<Transaction>> = _transactions.asStateFlow()

    actual override suspend fun saveTransaction(transaction: Transaction) {
        _transactions.value = listOf(transaction) + _transactions.value
    }
}
