import unittest
from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# Host address of the local running Appium Server
APPIUM_SERVER_URL = "http://localhost:4723"

class CashiAppiumUiTests(unittest.TestCase):
    def setUp(self):
        # Configure capabilities for the Android application running in emulator
        options = UiAutomator2Options()
        options.platform_name = "Android"
        options.device_name = "Android Emulator"
        options.automation_name = "UiAutomator2"
        options.app_package = "com.cashi.cashichallengev1"
        options.app_activity = "com.cashi.cashichallengev1.MainActivity"
        options.no_reset = False
        options.ensure_webviews_have_pages = True

        # Initialize Appium driver session
        self.driver = webdriver.Remote(APPIUM_SERVER_URL, options=options)
        self.wait = WebDriverWait(self.driver, 10)

    def tearDown(self):
        # Terminate driver session
        if self.driver:
            self.driver.quit()

    def test_send_payment_flow(self):
        """
        Tests the end-to-end payment flow:
        1. Finds and fills the Recipient Email input field (using its test tag).
        2. Finds and fills the Amount input field (using its test tag).
        3. Clicks the "Send Payment" Button.
        4. Asserts that the payment succeeds (verified via the success banner text).
        5. Asserts that the transaction is appended to the Transaction History list.
        """
        # 1. Fill Recipient Email field
        email_field = self.wait.until(
            EC.presence_of_element_located((AppiumBy.XPATH, "//*[contains(@resource-id, 'email_input')]"))
        )
        email_field.click()
        email_field.send_keys("automation_recipient@cashi.com")

        # 2. Fill Amount field
        amount_field = self.wait.until(
            EC.presence_of_element_located((AppiumBy.XPATH, "//*[contains(@resource-id, 'amount_input')]"))
        )
        amount_field.click()
        amount_field.send_keys("75.50")

        # 3. Click "Send Payment" Button
        send_button = self.wait.until(
            EC.element_to_be_clickable((AppiumBy.XPATH, "//*[contains(@resource-id, 'submit_button')]"))
        )
        send_button.click()

        # 4. Verify transaction success message pops up
        success_message = self.wait.until(
            EC.presence_of_element_located((AppiumBy.XPATH, "//*[contains(@text, 'sent successfully!')]"))
        )
        self.assertTrue(success_message.is_displayed())

        # 5. Verify the transaction appears in the scrollable Transaction History list
        transaction_item = self.wait.until(
            EC.presence_of_element_located((AppiumBy.XPATH, "//*[contains(@text, 'automation_recipient@cashi.com')]"))
        )
        self.assertTrue(transaction_item.is_displayed())

if __name__ == "__main__":
    unittest.main()
