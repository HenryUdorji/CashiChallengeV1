package com.cashi.cashichallengev1.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val recipientEmail: String,
    val amount: Double,
    val currency: String,
    val timestamp: String,
    val status: String
)
