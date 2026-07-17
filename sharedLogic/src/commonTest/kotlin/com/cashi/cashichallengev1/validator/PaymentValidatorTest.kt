package com.cashi.cashichallengev1.validator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PaymentValidatorTest {

    @Test
    fun testValidPaymentRequest() {
        val result = PaymentValidator.validate("recipient@cashi.com", 150.00, "USD")
        assertEquals(ValidationResult.Valid, result)
        
        val resultEur = PaymentValidator.validate("another@cashi.com", 0.01, "eur")
        assertEquals(ValidationResult.Valid, resultEur)
    }

    @Test
    fun testInvalidEmails() {
        val invalidEmails = listOf(
            "",
            "   ",
            "plainaddress",
            "#@%^%#$@#$@#.com",
            "@example.com",
            "Joe Smith <email@example.com>",
            "email.example.com",
            "email@example@example.com"
        )
        for (email in invalidEmails) {
            val result = PaymentValidator.validate(email, 100.0, "USD")
            assertTrue(result is ValidationResult.Invalid, "Email should be invalid: $email")
            assertEquals("Invalid recipient email address format.", result.message)
        }
    }

    @Test
    fun testInvalidAmounts() {
        val invalidAmounts = listOf(0.0, -0.01, -100.00)
        for (amount in invalidAmounts) {
            val result = PaymentValidator.validate("test@cashi.com", amount, "USD")
            assertTrue(result is ValidationResult.Invalid, "Amount should be invalid: $amount")
            assertEquals("Amount must be a positive number greater than 0.", result.message)
        }
    }

    @Test
    fun testInvalidCurrencies() {
        val invalidCurrencies = listOf("", "GBP", "CAD", "JPY", "usd1")
        for (currency in invalidCurrencies) {
            val result = PaymentValidator.validate("test@cashi.com", 10.0, currency)
            assertTrue(result is ValidationResult.Invalid, "Currency should be invalid: $currency")
            assertEquals("Unsupported currency. Supported currencies are USD and EUR.", result.message)
        }
    }
}
