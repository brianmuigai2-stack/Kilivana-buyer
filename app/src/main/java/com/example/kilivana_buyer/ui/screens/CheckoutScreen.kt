package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kilivana_buyer.model.CartItem
import com.example.kilivana_buyer.model.formatKes
import com.example.kilivana_buyer.ui.theme.*

@Composable
fun CheckoutScreen(items: List<CartItem>, place: (String) -> Unit, back: () -> Unit, submitting: Boolean = false) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var town by remember { mutableStateOf("Nairobi") }
    var street by remember { mutableStateOf("") }
    var payment by remember { mutableStateOf("M-Pesa") }
    var deliveryWindow by remember { mutableStateOf("Morning · 8 am–12 pm") }
    var submitted by remember { mutableStateOf(false) }
    val validPhone = phone.matches(Regex("^(?:07|01)\\d{8}$|^\\+254\\d{9}$"))
    val total = items.sumOf { it.quantity * it.product.pricePerUnit }

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

        CheckoutSection("Contact") {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full name") }, singleLine = true, isError = submitted && name.isBlank())
            OutlinedTextField(
                phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Phone number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true,
                isError = (phone.isNotEmpty() && !validPhone) || (submitted && !validPhone),
                supportingText = { if ((phone.isNotEmpty() || submitted) && !validPhone) Text("Enter a valid Kenyan number") }
            )
        }

        CheckoutSection("Delivery address") {
            OutlinedTextField(town, { town = it }, Modifier.fillMaxWidth(), label = { Text("Town or area") }, singleLine = true, isError = submitted && town.isBlank())
            OutlinedTextField(street, { street = it }, Modifier.fillMaxWidth(), label = { Text("Street, estate, or landmark") }, minLines = 2, isError = submitted && street.isBlank())
            Text("Preferred delivery window", style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Morning · 8 am–12 pm", "Afternoon · 12 pm–5 pm", "Evening · 5 pm–8 pm").forEach { slot ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(slot, Modifier.weight(1f))
                        RadioButton(selected = deliveryWindow == slot, onClick = { deliveryWindow = slot })
                    }
                }
            }
        }

        CheckoutSection("Payment method") {
            listOf("M-Pesa", "Cash on delivery").forEach { method ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(method, Modifier.weight(1f))
                    RadioButton(selected = payment == method, onClick = { payment = method })
                }
            }
            if (payment == "M-Pesa") {
                OutlinedTextField(
                    phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("M-Pesa phone number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true
                )
            }
        }

        CheckoutSection("Order summary") {
            items.forEach { line ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${line.quantity} × ${line.product.name}", Modifier.weight(1f))
                    Text(formatKes(line.quantity * line.product.pricePerUnit))
                }
            }
            HorizontalDivider(color = NeutralDivider)
            SummaryLine("Items subtotal", formatKes(total))
            SummaryLine("Delivery", "Calculated when service is available")
            SummaryLine("Total before delivery", formatKes(total), emphasize = true)
        }

        Surface(shape = RoundedCornerShape(14.dp), color = Green50) {
            Row(Modifier.padding(14.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Green700, modifier = Modifier.padding(end = 10.dp))
                Text(
                    "The checkout API processes your signed-in account cart and does not accept the delivery or payment fields entered on this screen.",
                    color = Green700,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (submitted) Text("Please check your name, Kenyan phone number, and delivery address.", color = ErrorRed)
        Button(
            onClick = {
                val detailsValid = name.isNotBlank() && validPhone && town.isNotBlank() && street.isNotBlank()
                submitted = !detailsValid
                if (detailsValid) place("$street, $town")
            },
            Modifier.fillMaxWidth(),
            enabled = items.isNotEmpty() && !submitting
        ) {
            if (submitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Place order")
        }
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
