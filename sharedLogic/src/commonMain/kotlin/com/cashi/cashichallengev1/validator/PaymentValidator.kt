package com.cashi.cashichallengev1.validator

object PaymentValidator {
    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()

    fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email)
    }

    fun validateAmount(amount: Double): Boolean {
        return amount > 0.0
    }

    fun validateCurrency(currency: String): Boolean {
        val upper = currency.uppercase()
        return upper == "USD" || upper == "EUR"
    }

    fun validate(email: String, amount: Double, currency: String): ValidationResult {
        if (!validateEmail(email)) {
            return ValidationResult.Invalid("Invalid recipient email address format.")
        }
        if (!validateAmount(amount)) {
            return ValidationResult.Invalid("Amount must be a positive number greater than 0.")
        }
        if (!validateCurrency(currency)) {
            return ValidationResult.Invalid("Unsupported currency. Supported currencies are USD and EUR.")
        }
        return ValidationResult.Valid
    }
}

sealed interface ValidationResult {
    object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}
