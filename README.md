# Cashi Payment KMP Mobile App - Developer Guide

This is my implementation of the Cashi Mobile App Challenge V1. It is built as a **Kotlin Multiplatform (KMP)** application containing an Android launcher, a shared Compose UI library, a shared business logic library, and a native **Kotlin Ktor Server** subproject.


---

## 🎬 Application Demonstration
Below is a recording showing the payment flows, MVI view state transitions, and real-time Firestore collection synchronizations:

![Cashi Payment Flow Demo](demo/cashi_demo.gif)

---

## 🏗️ Architectural Overview & Design Decisions

This project follows clean engineering principles to ensure the codebase remains maintainable, modular, and highly testable:

```
                          ┌──────────────────────────────┐
                          │    :androidApp (Android)     │
                          └──────────────┬───────────────┘
                                         │ depends on
                          ┌──────────────▼───────────────┐
                          │ :sharedUI (Compose MP)       │
                          └──────────────┬───────────────┘
                                         │ depends on
                          ┌──────────────▼───────────────┐
                          │  :sharedLogic (KMP Logic)    │◄──────┐
                          └──────────────────────────────┘       │
                                                                 │ depends on (JVM target)
                          ┌──────────────────────────────┐       │
                          │   :server (Ktor Backend)     ├───────┘
                          └──────────────────────────────┘
```

1. **Gradle Multi-Module Layout**:
    - `:sharedLogic`: Houses the core library written in pure Kotlin, structured according to **Clean Architecture** patterns:
        - **Domain Layer**: Contains shared models ([Transaction](file:///Users/Ifechukwu/Work/Kmp/Cashi%20ChallengeV1/sharedLogic/src/commonMain/kotlin/com/cashi/cashichallengev1/domain/model/Transaction.kt), [PaymentRequest](file:///Users/Ifechukwu/Work/Kmp/Cashi%20ChallengeV1/sharedLogic/src/commonMain/kotlin/com/cashi/cashichallengev1/domain/model/PaymentRequest.kt)), [TransactionRepository](file:///Users/Ifechukwu/Work/Kmp/Cashi%20ChallengeV1/sharedLogic/src/commonMain/kotlin/com/cashi/cashichallengev1/domain/repository/TransactionRepository.kt) interface, and Use Cases (`ProcessPaymentUseCase`, `ObserveTransactionsUseCase`) holding core business logic.
        - **Data Layer**: Contains Ktor client service [PaymentService](file:///Users/Ifechukwu/Work/Kmp/Cashi%20ChallengeV1/sharedLogic/src/commonMain/kotlin/com/cashi/cashichallengev1/data/network/PaymentService.kt) and expect/actual `PlatformFirestore` data wrapper sources.
        - **Presentation Layer**: Contains the unidirectional MVI state machine (`PaymentViewModel`, `PaymentState`, `PaymentIntent`).
    - `:sharedUI`: Houses Compose Multiplatform UI components (like the form, lists, and theme styling).
    - `:androidApp`: Launches the Android app, registers the `Application` class, and configures the DI container.
    - `:server`: A separate Kotlin JVM module hosting a Ktor Netty HTTP server on port `8080`.

2. **Full-Stack Kotlin Code Sharing**:
    - Configured `:sharedLogic` to support a `jvm()` compiler target.
    - The Ktor `:server` depends on `:sharedLogic` directly. This enables **direct code sharing** of the domain models and validation rules (`PaymentValidator`). If validation rules change, they remain in sync across client and server at compile-time.

3. **MVI (Model-View-Intent) Presentation**:
    - `PaymentViewModel` exposes a single, immutable `StateFlow<PaymentState>` to the Compose views.
    - View actions are modeled as explicit `PaymentIntent` objects (e.g. `SubmitPayment`, `EmailChanged`). This guarantees unidirectional data flow (UDF), making states predictable and easy to unit test.

4. **KMP expect/actual Wrapper for Native SDKs**:
    - Rather than forcing third-party KMP database libraries, I declared `expect class PlatformFirestore` implementing the `TransactionRepository` interface in `commonMain` data layer.
    - In `androidMain`, I implemented `actual class PlatformFirestore` calling the official Android Firebase Firestore SDK.
    - In `jvmMain` and `iosMain`, I implemented mock in-memory database fallback wrappers, allowing the server and other targets to compile and execute instantly without target dependency errors.

5. **Koin Dependency Injection**:
    - Common services, use cases, and target-specific platforms are injected using Koin.
    - The Koin configuration accepts a runtime `baseUrl`, letting the Android emulator (`10.0.2.2`), iOS emulator (`localhost`), and unit tests configure network endpoints dynamically.

---

## 🚀 Setting Up & Running the Application

### 1. Start the Ktor Server Backend
The Ktor server runs on your local machine on port `8080`. Run the Gradle runner:
```bash
./gradlew :server:run
```
You can verify it is running by hitting `http://localhost:8080/` in your browser. It should show:
`Cashi Challenge V1 Ktor Mock Server is running!`

### 2. Running on Emulators vs. Physical Devices (Network Configuration)

Depending on your target device, you must configure the Ktor server `baseUrl` in [CashiApplication.kt](file:///Users/Ifechukwu/Work/Kmp/Cashi%20ChallengeV1/androidApp/src/main/kotlin/com/cashi/cashichallengev1/CashiApplication.kt):

#### A. Running on the Android Emulator
* **IP Configuration**: Use `http://10.0.2.2:8080` (the emulator's special loopback alias pointing to your laptop's `localhost`).
* **CashiApplication.kt Configuration**:
  ```kotlin
  modules(
      commonModule(baseUrl = "http://10.0.2.2:8080"),
      androidModule
  )
  ```
* Run the app from Android Studio, or compile and install via CLI:
  ```bash
  ./gradlew :androidApp:installDebug
  ```

#### B. Running on a Physical Android Device (Shared local Wi-Fi)
If you want to run the app wirelessly:
1. Connect both your computer and your physical Android phone to the **same Wi-Fi network**.
2. Find your computer's local IP address:
    * **macOS**: Run `ipconfig getifaddr en0` in the terminal.
    * **Windows**: Run `ipconfig` in the Command Prompt.
      *(e.g., `192.168.1.15`)*
3. **CashiApplication.kt Configuration**: Use `http://<YOUR_COMPUTER_IP>:8080`:
  ```kotlin
  modules(
    commonModule(baseUrl = "http://192.168.1.15:8080"), // Replace with your IP
    androidModule
)
  ```

*(Note: `AndroidManifest.xml` has `usesCleartextTraffic="true"` configured, allowing HTTP cleartext connections to local dev IP ranges).*

### 3. Building the Production Release APK
To compile and package the optimized, minified production release APK:
1. Ensure your signing credentials are configured inside [`local.properties`](file:///Users/Ifechukwu/Work/Kmp/Cashi%20ChallengeV1/local.properties) (see properties layout below).
2. Execute the Gradle assembly task:
   ```bash
   ./gradlew :androidApp:assembleRelease
   ```
3. The generated release APK will be output to:
   📂 `androidApp/build/outputs/apk/release/androidApp-release.apk`
   *(Or `androidApp-release-unsigned.apk` if signing properties were omitted).*

#### Keystore configurations layout inside `local.properties`:
```properties
signing.storeFile=path/to/keystore.jks
signing.storePassword=your_keystore_password
signing.keyAlias=your_key_alias
signing.keyPassword=your_key_password
```
*(Note: R8 minification and Proguard rules defined in `androidApp/proguard-rules.pro` will be applied automatically to strip unused dependencies and obfuscate class members for production security).*

---

## 🧪 Running the Tests

### 1. JVM Unit Tests (Shared Logic & Validation)
Runs JUnit tests for validations and network mapping on the local JVM:
```bash
./gradlew :sharedLogic:testAndroidHostTest
```

### 2. Cucumber Behavior-Driven Development (BDD)
Gherkin syntax is used to define behavioral rules under `sharedLogic/src/androidHostTest/resources/features/payment.feature` and run step definitions in `PaymentSteps.kt`. Run the tests via:
```bash
./gradlew :sharedLogic:testAndroidHostTest --tests "com.cashi.cashichallengev1.bdd.RunBddTest"
```

### 3. JMeter Performance testing
The JMeter script is configured in `jmeter/payments_performance.jmx`.
- **Target**: `http://localhost:8080/payments`
- **Load Profile**: 5 concurrent threads (users), 10 iterations each (total 50 requests).
- **Run Headless**:
  ```bash
  jmeter -n -t jmeter/payments_performance.jmx -l jmeter/results.jtl
  ```

### 4. Appium Mobile UI Automation
The Python automation script is in `testing/payment_ui_test.py`. It uses UI test tags mapped on Compose inputs (`email_input`, `amount_input`, `submit_button`).
- **Prerequisites Setup**:
    1. Install Appium Server globally:
       ```bash
       npm install -g appium
       ```
    2. Install the Android automation driver:
       ```bash
       appium driver install uiautomator2
       ```
    3. Install the Python automation test dependencies:
       ```bash
       pip install Appium-Python-Client selenium
       ```
- **Execution Steps**:
    1. Compile and install the app on your running Android Emulator:
       ```bash
       ./gradlew :androidApp:installDebug
       ```
    2. Open a separate terminal window and start the Appium server:
       ```bash
       appium
       ```
    3. Run the python automation test script:
       ```bash
       python testing/payment_ui_test.py
       ```