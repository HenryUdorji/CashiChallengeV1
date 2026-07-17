package com.cashi.cashichallengev1.data.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.call.*
import io.ktor.http.*
import com.cashi.cashichallengev1.domain.model.PaymentRequest
import com.cashi.cashichallengev1.domain.model.Transaction
import kotlinx.serialization.Serializable

sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Failure(val error: Throwable) : NetworkResult<Nothing>
}

@Serializable
private data class ErrorBody(
    val status: String,
    val message: String
)

open class PaymentService(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    open suspend fun processPayment(recipientEmail: String, amount: Double, currency: String): NetworkResult<Transaction> {
        return try {
            val response = httpClient.post("$baseUrl/payments") {
                contentType(ContentType.Application.Json)
                setBody(PaymentRequest(recipientEmail, amount, currency))
            }
            if (response.status == HttpStatusCode.OK) {
                NetworkResult.Success(response.body<Transaction>())
            } else {
                val errorMsg = try {
                    response.body<ErrorBody>().message
                } catch (e: Exception) {
                    "Request failed with status ${response.status}"
                }
                NetworkResult.Failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            NetworkResult.Failure(e)
        }
    }
}
