package com.cashi.cashichallengev1

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.cashi.cashichallengev1.domain.model.Transaction
import com.cashi.cashichallengev1.presentation.PaymentIntent
import com.cashi.cashichallengev1.presentation.PaymentState
import com.cashi.cashichallengev1.presentation.PaymentViewModel
import kotlinx.coroutines.delay
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

// Premium Colors
val DarkBg = Color(0xFF0F172A)
val CardBg = Color(0xFF1E293B)
val EmeraldGreen = Color(0xFF10B981)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val ErrorColor = Color(0xFFEF4444)

@Composable
fun App() {
    KoinContext {
        MaterialTheme(
            colorScheme = darkColorScheme(
                background = DarkBg,
                surface = CardBg,
                primary = EmeraldGreen,
                onPrimary = Color.White,
                error = ErrorColor
            )
        ) {
            val viewModel: PaymentViewModel = koinInject()
            val state by viewModel.state.collectAsState()

            PaymentScreen(
                state = state,
                onIntent = { viewModel.handleIntent(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PaymentScreen(
    state: PaymentState,
    onIntent: (PaymentIntent) -> Unit
) {
    var currencyExpanded by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "EUR")

    LaunchedEffect(state.userMessage) {
        if (state.userMessage != null) {
            delay(5000)
            onIntent(PaymentIntent.ClearMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cashi Pay",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg
                )
            )
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .semantics { testTagsAsResourceId = true }
        ) {
            AnimatedVisibility(
                visible = state.userMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                state.userMessage?.let { msg ->
                    val isError = msg.contains("Error", ignoreCase = true) || msg.contains("failed", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isError) ErrorColor.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f))
                            .border(1.dp, if (isError) ErrorColor else EmeraldGreen, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = msg,
                            color = if (isError) Color(0xFFFCA5A5) else Color(0xFFA7F3D0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Send Money Instantly",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = state.recipientEmail,
                        onValueChange = { onIntent(PaymentIntent.EmailChanged(it)) },
                        label = { Text("Recipient Email", color = TextSecondary) },
                        placeholder = { Text("email@example.com", color = TextSecondary.copy(alpha = 0.5f)) },
                        isError = state.emailError != null,
                        supportingText = {
                            state.emailError?.let {
                                Text(text = it, color = ErrorColor)
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                            errorBorderColor = ErrorColor
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("email_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MoneyInput(
                            amountCents = state.amountCents,
                            onAmountChange = { onIntent(PaymentIntent.AmountChanged(it)) },
                            currency = state.currency,
                            errorText = state.amountError,
                            modifier = Modifier.weight(1.5f)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.currency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Currency", color = TextSecondary) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.clickable { currencyExpanded = true }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { currencyExpanded = true }
                                    .testTag("currency_selector")
                            )

                            DropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false },
                                modifier = Modifier.background(CardBg)
                            ) {
                                currencies.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr, color = TextPrimary) },
                                        onClick = {
                                            onIntent(PaymentIntent.CurrencyChanged(curr))
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { onIntent(PaymentIntent.SubmitPayment) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_button"),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Send Payment",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Transaction History",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (state.transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No transactions yet",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.transactions, key = { it.id }) { txn ->
                        TransactionItem(transaction = txn)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.recipientEmail,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(transaction.timestamp),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.currency == "USD") "$" else "€"}${transaction.amount}",
                    color = EmeraldGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Badge for status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EmeraldGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = transaction.status,
                        color = EmeraldGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Simple helper to make ISO timestamps more readable (e.g. 2026-07-16T12:00:00Z -> Jul 16, 12:00)
fun formatTimestamp(isoString: String): String {
    return try {
        val parts = isoString.split("T")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")
            if (dateParts.size == 3 && timeParts.size >= 2) {
                val monthStr = when (dateParts[1]) {
                    "01" -> "Jan"
                    "02" -> "Feb"
                    "03" -> "Mar"
                    "04" -> "Apr"
                    "05" -> "May"
                    "06" -> "Jun"
                    "07" -> "Jul"
                    "08" -> "Aug"
                    "09" -> "Sep"
                    "10" -> "Oct"
                    "11" -> "Nov"
                    "12" -> "Dec"
                    else -> dateParts[1]
                }
                val day = dateParts[2]
                val hour = timeParts[0]
                val min = timeParts[1]
                "$monthStr $day, $hour:$min"
            } else {
                isoString
            }
        } else {
            isoString
        }
    } catch (e: Exception) {
        isoString
    }
}

@Composable
fun MoneyInput(
    amountCents: Long,
    onAmountChange: (Long) -> Unit,
    currency: String,
    modifier: Modifier = Modifier,
    label: String = "Amount",
    errorText: String? = null,
    maxDigits: Int = 10,
    testTag: String = "amount_input"
) {
    val displayValue = remember(amountCents) { formatCents(amountCents) }

    OutlinedTextField(
        value = TextFieldValue(
            text = displayValue,
            selection = TextRange(displayValue.length)
        ),
        onValueChange = { newValue ->
            val digitsOnly = newValue.text.filter { it.isDigit() }
            val trimmed = digitsOnly.trimStart('0').ifEmpty { "0" }
            if (trimmed.length <= maxDigits) {
                onAmountChange(trimmed.toLong())
            }
        },
        label = { Text(label, color = TextSecondary) },
        isError = errorText != null,
        supportingText = {
            errorText?.let { Text(text = it, color = ErrorColor) }
        },
        leadingIcon = {
            Text(
                text = currency.toCurrencySymbol(),
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = EmeraldGreen,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
            errorBorderColor = ErrorColor
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier.testTag(testTag)
    )
}

private fun formatCents(cents: Long): String {
    val safeCents = cents.coerceAtLeast(0)
    val whole = safeCents / 100
    val fraction = safeCents % 100
    return "$whole.${fraction.toString().padStart(2, '0')}"
}

private fun String.toCurrencySymbol(): String = when (this) {
    "USD" -> "$"
    "EUR" -> "€"
    else -> this
}