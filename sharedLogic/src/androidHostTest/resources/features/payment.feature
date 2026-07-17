Feature: Send Payment Logic
  As a Cashi App user,
  I want to send payments to recipients,
  So that I can transfer funds successfully.

  Scenario: Send payment with valid details
    Given the recipient email is "valid@cashi.com"
    And the payment amount is 100.00
    And the currency is "USD"
    When the user submits the payment
    Then the payment should be successfully processed
    And the transaction status should be "SUCCESS"

  Scenario: Send payment with invalid email format
    Given the recipient email is "invalid_email"
    And the payment amount is 50.00
    And the currency is "EUR"
    When the user submits the payment
    Then the system should show an email validation error

  Scenario: Send payment with zero amount
    Given the recipient email is "valid@cashi.com"
    And the payment amount is 0.00
    And the currency is "USD"
    When the user submits the payment
    Then the system should show an amount validation error

  Scenario: Send payment with server error
    Given the recipient email is "server_error@cashi.com"
    And the payment amount is 25.00
    And the currency is "USD"
    When the user submits the payment
    Then the system should show a server connection error
