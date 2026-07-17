package com.cashi.cashichallengev1.server

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.HttpStatusCode
import com.cashi.cashichallengev1.domain.model.PaymentRequest
import com.cashi.cashichallengev1.domain.model.Transaction
import com.cashi.cashichallengev1.validator.PaymentValidator
import com.cashi.cashichallengev1.validator.ValidationResult
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val status: String,
    val message: String
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Cashi Challenge V1 Ktor Mock Server is running!")
        }
        
        post("/payments") {
            try {
                val request = call.receive<PaymentRequest>()
                println("Received payment request: email=${request.recipientEmail}, amount=${request.amount}, currency=${request.currency}")
                
                // Perform validation using the shared PaymentValidator
                when (val validation = PaymentValidator.validate(request.recipientEmail, request.amount, request.currency)) {
                    is ValidationResult.Invalid -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("ERROR", validation.message)
                        )
                    }
                    is ValidationResult.Valid -> {
                        // Process the mock transaction
                        val transactionId = "TXN_" + UUID.randomUUID().toString().replace("-", "").take(9).uppercase()
                        val transaction = Transaction(
                            id = transactionId,
                            recipientEmail = request.recipientEmail,
                            amount = request.amount,
                            currency = request.currency.uppercase(),
                            timestamp = Instant.now().toString(),
                            status = "SUCCESS"
                        )
                        call.respond(HttpStatusCode.OK, transaction)
                    }
                }
            } catch (e: Exception) {
                application.log.error("Failed to process payment request", e)
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("ERROR", "Invalid request body format: ${e.localizedMessage}")
                )
            }
        }
    }
}
