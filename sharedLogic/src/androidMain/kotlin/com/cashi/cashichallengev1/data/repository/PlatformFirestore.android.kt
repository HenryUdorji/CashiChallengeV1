package com.cashi.cashichallengev1.data.repository

import android.util.Log
import com.cashi.cashichallengev1.domain.model.Transaction
import com.cashi.cashichallengev1.domain.repository.TransactionRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class PlatformFirestore actual constructor() : TransactionRepository, KoinComponent {
    
    private val firestore: FirebaseFirestore? by inject()
    
    private companion object {
        const val TAG = "PlatformFirestore"
        const val COLLECTION_PAYMENTS = "payments"

        const val FIELD_ID = "id"
        const val FIELD_RECIPIENT_EMAIL = "recipientEmail"
        const val FIELD_AMOUNT = "amount"
        const val FIELD_CURRENCY = "currency"
        const val FIELD_TIMESTAMP = "timestamp"
        const val FIELD_STATUS = "status"
    }

    private val fallbackTransactions = MutableStateFlow<List<Transaction>>(emptyList())

    private fun paymentCollection(fs: FirebaseFirestore) = fs.collection(COLLECTION_PAYMENTS)

    actual override fun observeTransactions(): Flow<List<Transaction>>  {
        val fs = firestore
        if (fs == null) {
            Log.w(TAG, "observeTransactions: Firestore is null, falling back to local list memory state.")
            return fallbackTransactions
        }

        return callbackFlow {
            val registration = paymentCollection(fs)
                .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        Log.w(TAG, "Firestore listener failed, showing cached items", exception)
                        trySend(fallbackTransactions.value)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val transactions = snapshots.documents.mapNotNull { doc ->
                            runCatching {
                                Transaction(
                                    id = doc.getString(FIELD_ID) ?: doc.id,
                                    recipientEmail = doc.getString(FIELD_RECIPIENT_EMAIL) ?: "",
                                    amount = doc.getDouble(FIELD_AMOUNT) ?: 0.0,
                                    currency = doc.getString(FIELD_CURRENCY) ?: "USD",
                                    timestamp = doc.getString(FIELD_TIMESTAMP) ?: "",
                                    status = doc.getString(FIELD_STATUS) ?: "SUCCESS",
                                )
                            }.onFailure {
                                Log.w(TAG, "Skipping malformed document ${doc.id}", it)
                            }.getOrNull()
                        }
                        fallbackTransactions.value = transactions
                        trySend(transactions)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    actual override suspend fun saveTransaction(transaction: Transaction) {
        fallbackTransactions.update { current ->
            listOf(transaction) + current.filter { it.id != transaction.id }
        }

        val fs = firestore
        if (fs == null) {
            Log.w(TAG, "saveTransaction: Firestore is null. Skipping Firestore upload and keeping transaction locally.")
            return
        }

        val data = mapOf(
            FIELD_ID to transaction.id,
            FIELD_RECIPIENT_EMAIL to transaction.recipientEmail,
            FIELD_AMOUNT to transaction.amount,
            FIELD_CURRENCY to transaction.currency,
            FIELD_TIMESTAMP to transaction.timestamp,
            FIELD_STATUS to transaction.status,
        )

        try {
            paymentCollection(fs).document(transaction.id).set(data).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write transaction ${transaction.id} to Firestore", e)
            throw e
        }
    }
}
