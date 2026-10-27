package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kilivana_buyer.data.DeliveryAddress
import com.example.kilivana_buyer.data.DeliveryQuote
import com.example.kilivana_buyer.model.CartItem
import com.example.kilivana_buyer.ui.theme.*

@Composable
fun CheckoutScreen(
    items: List<CartItem>,
    addresses: List<DeliveryAddress>,
    addressesLoading: Boolean,
    addressError: String?,
    quote: DeliveryQuote?,
    invalidateQuote: () -> Unit,
    requestQuote: (Long) -> Unit,
    manageAddresses: () -> Unit,
    place: (String, Boolean, String) -> Unit,
    back: () -> Unit,
    submitting: Boolean = false
) {
    var phone by remember { mutableStateOf("") }
    var selectedAddressId by remember(addresses) {
        mutableStateOf(addresses.firstOrNull()?.id)
    }
    var addressMenuExpanded by remember { mutableStateOf(false) }
    var payment by remember { mutableStateOf("M-Pesa") }
    val validPhone = phone.matches(Regex("^(?:07|01)\\d{8}$|^\\+254\\d{9}$"))
    val activeQuote = quote?.takeIf { it.addressId == selectedAddressId }

    Column(
        Modifier.fillMaxSize().background(NeutralBackground).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            androidx.compose.material3.IconButton(back) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Checkout", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }
        Text("Delivery and payment details", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)

        CheckoutSection("Phone number") {
            OutlinedTextField(
                phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Kenyan mobile number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true,
                isError = phone.isNotEmpty() && !validPhone,
                supportingText = { if (!validPhone) Text("Enter a valid Kenyan number") }
            )
        }

        CheckoutSection("Delivery address") {
            when {
                addressesLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                addresses.isEmpty() -> {
                    Text(addressError ?: "No saved delivery addresses yet.", color = if (addressError != null) ErrorRed else TextSecondary)
                    OutlinedButton(onClick = manageAddresses, modifier = Modifier.fillMaxWidth()) { Text("Add delivery address") }
                }
                else -> {
                    Box {
                        OutlinedButton(onClick = { addressMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(addresses.firstOrNull { it.id == selectedAddressId }?.label ?: "Select delivery address")
                        }
                        DropdownMenu(expanded = addressMenuExpanded, onDismissRequest = { addressMenuExpanded = false }) {
                            addresses.forEach { address ->
                                DropdownMenuItem(
                                    text = { Text(address.label) },
                                    onClick = {
                                        if (selectedAddressId != address.id) invalidateQuote()
                                        selectedAddressId = address.id
                                        addressMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (addresses.isNotEmpty()) {
                TextButton(onClick = manageAddresses, modifier = Modifier.align(Alignment.End)) { Text("Manage addresses") }
            }
            Button(
                onClick = { selectedAddressId?.let(requestQuote) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAddressId != null && !addressesLoading && !submitting && validPhone
            ) { Text(if (submitting && activeQuote == null) "Calculating delivery…" else "Calculate delivery") }
        }

        activeQuote?.let { deliveryQuote ->
            CheckoutSection("Order summary") {
                items.forEach { line ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${line.quantity} × ${line.product.name}", Modifier.weight(1f))
                    }
                }
                HorizontalDivider(color = NeutralDivider)
                SummaryLine("Items subtotal", "${deliveryQuote.currency} ${"%.2f".format(deliveryQuote.subtotal)}")
                SummaryLine("Delivery", "${deliveryQuote.currency} ${"%.2f".format(deliveryQuote.deliveryFee)}")
                SummaryLine("Total", "${deliveryQuote.currency} ${"%.2f".format(deliveryQuote.total)}", emphasize = true)
            }

            CheckoutSection("Payment method") {
                listOf("M-Pesa", "Cash on delivery").forEach { method ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(method, Modifier.weight(1f))
                        RadioButton(selected = payment == method, onClick = { payment = method })
                    }
                }
            }
            Button(
                onClick = { place(deliveryQuote.quoteId, payment == "M-Pesa", phone) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !submitting && validPhone
            ) {
                if (submitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(if (payment == "M-Pesa") "Create order and pay" else "Place order")
            }
        }

        if (items.isEmpty()) Text("Your cart is empty.", color = TextSecondary)
    }
}

@Composable
private fun CheckoutSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = NeutralSurface, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            content()
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, emphasize: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (emphasize) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium)
        Text(value, style = if (emphasize) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium, color = if (emphasize) Green700 else TextPrimary)
    }
}
