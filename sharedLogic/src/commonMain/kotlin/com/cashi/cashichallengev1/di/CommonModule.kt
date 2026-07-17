package com.cashi.cashichallengev1.di

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import com.cashi.cashichallengev1.data.network.PaymentService
import com.cashi.cashichallengev1.data.repository.PlatformFirestore
import com.cashi.cashichallengev1.domain.repository.TransactionRepository
import com.cashi.cashichallengev1.domain.usecase.ObserveTransactionsUseCase
import com.cashi.cashichallengev1.domain.usecase.ProcessPaymentUseCase
import com.cashi.cashichallengev1.presentation.PaymentViewModel

fun commonModule(baseUrl: String) = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.DEFAULT
            }
        }
    }

    single { PaymentService(httpClient = get(), baseUrl = baseUrl) }

    single<TransactionRepository> { PlatformFirestore() }

    single { ProcessPaymentUseCase(paymentService = get(), transactionRepository = get()) }
    single { ObserveTransactionsUseCase(repository = get()) }

    factory { 
        PaymentViewModel(
            processPaymentUseCase = get(),
            observeTransactionsUseCase = get()
        ) 
    }
}
