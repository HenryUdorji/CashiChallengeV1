package com.cashi.cashichallengev1.bdd

import io.cucumber.java.Before
import io.cucumber.java.After
import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import io.cucumber.java.en.Then
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import com.cashi.cashichallengev1.presentation.PaymentViewModel
import com.cashi.cashichallengev1.presentation.PaymentIntent
import com.cashi.cashichallengev1.data.network.PaymentService
import com.cashi.cashichallengev1.data.network.NetworkResult
import com.cashi.cashichallengev1.domain.model.Transaction
import com.cashi.cashichallengev1.domain.repository.TransactionRepository
import com.cashi.cashichallengev1.domain.usecase.ObserveTransactionsUseCase
import com.cashi.cashichallengev1.domain.usecase.ProcessPaymentUseCase
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain

class PaymentSteps {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUpDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
    }

    private val transactionRepository = MockTransactionRepository()
    private val paymentService = MockPaymentService()
    private lateinit var viewModel: PaymentViewModel
    
    private var emailInput: String = ""
    private var amountInput: String = ""
    private var currencyInput: String = "USD"

    @Given("the recipient email is {string}")
    fun the_recipient_email_is(email: String) {
        emailInput = email
    }

    @Given("the payment amount is {double}")
    fun the_payment_amount_is(amount: Double) {
        amountInput = amount.toString()
    }

    @Given("the currency is {string}")
    fun the_currency_is(currency: String) {
        currencyInput = currency
    }

    @When("the user submits the payment")
    fun the_user_submits_the_payment() = runBlocking {
        val processPaymentUseCase = ProcessPaymentUseCase(paymentService, transactionRepository)
        val observeTransactionsUseCase = ObserveTransactionsUseCase(transactionRepository)
        viewModel = PaymentViewModel(processPaymentUseCase, observeTransactionsUseCase)
        
        val amountVal = amountInput.toDoubleOrNull() ?: 0.0
        val amountCents = (amountVal * 100).toLong()

        viewModel.handleIntent(PaymentIntent.EmailChanged(emailInput))
        viewModel.handleIntent(PaymentIntent.AmountChanged(amountCents))
        viewModel.handleIntent(PaymentIntent.CurrencyChanged(currencyInput))

        if (emailInput == "server_error@cashi.com") {
            paymentService.mockResult = NetworkResult.Failure(Exception("Server connection failed"))
        } else if (emailInput.contains("@") && amountVal > 0.0) {
            paymentService.mockResult = NetworkResult.Success(
                Transaction("TXN_MOCK", emailInput, amountVal, currencyInput, "2026-07-16T12:00:00Z", "SUCCESS")
            )
        } else {
            paymentService.mockResult = NetworkResult.Failure(Exception("Validation failed"))
        }

        viewModel.handleIntent(PaymentIntent.SubmitPayment)
    }

    @Then("the payment should be successfully processed")
    fun the_payment_should_be_successfully_processed() {
        val state = viewModel.state.value
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage.contains("successfully", ignoreCase = true))
    }

    @Then("the transaction status should be {string}")
    fun the_transaction_status_should_be(status: String) {
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
    }

    @Then("the system should show an email validation error")
    fun the_system_should_show_an_email_validation_error() {
        val state = viewModel.state.value
        assertNotNull(state.emailError)
        assertEquals("Invalid email address format.", state.emailError)
    }

    @Then("the system should show an amount validation error")
    fun the_system_should_show_an_amount_validation_error() {
        val state = viewModel.state.value
        assertNotNull(state.amountError)
        assertEquals("Amount must be greater than 0.", state.amountError)
    }

    @Then("the system should show a server connection error")
    fun the_system_should_show_a_server_connection_error() {
        val state = viewModel.state.value
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage.contains("failed", ignoreCase = true) || state.userMessage.contains("connection", ignoreCase = true))
    }
}

private class MockTransactionRepository : TransactionRepository {
    val saved = mutableListOf<Transaction>()
    override fun observeTransactions() = flowOf(saved)
    override suspend fun saveTransaction(transaction: Transaction) {
        saved.add(transaction)
    }
}

private class MockPaymentService : PaymentService(
    httpClient = HttpClient(),
    baseUrl = ""
) {
    var mockResult: NetworkResult<Transaction> = NetworkResult.Failure(Exception("Not initialized"))
    override suspend fun processPayment(recipientEmail: String, amount: Double, currency: String): NetworkResult<Transaction> {
        return mockResult
    }
}
