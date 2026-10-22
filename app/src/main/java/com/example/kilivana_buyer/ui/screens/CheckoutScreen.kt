package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kilivana_buyer.R
import com.example.kilivana_buyer.data.CartViewModel
import com.example.kilivana_buyer.model.*
import com.example.kilivana_buyer.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable fun CheckoutScreen(items: List<CartItem>, place: (String) -> Unit) {
    var name by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }; var town by remember { mutableStateOf("Nairobi") }; var street by remember { mutableStateOf("") }; var payment by remember { mutableStateOf("M-Pesa") }
    Column(Modifier.fillMaxSize().imePadding().verticalScrollCompat().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Delivery details", style = MaterialTheme.typography.headlineMedium); listOf("Full name" to name, "Phone number" to phone, "Town" to town, "Street or landmark" to street).forEach { (label, value) -> OutlinedTextField(value, { updated -> when(label) { "Full name" -> name=updated; "Phone number" -> phone=updated; "Town" -> town=updated; else -> street=updated } }, Modifier.fillMaxWidth(), label={ Text(label) }, keyboardOptions = if(label=="Phone number") KeyboardOptions(keyboardType=KeyboardType.Phone) else KeyboardOptions.Default) }; Text("Delivery window"); listOf("Morning · 8 am–12 pm", "Afternoon · 12 pm–5 pm", "Evening · 5 pm–8 pm").forEach { Text("◯  $it") }; Text("Payment method"); listOf("M-Pesa", "Cash on delivery").forEach { Row(verticalAlignment=Alignment.CenterVertically) { RadioButton(payment==it,{payment=it}); Text(it) } }; if(payment=="M-Pesa") OutlinedTextField(phone,{phone=it},Modifier.fillMaxWidth(),label={Text("M-Pesa phone number")}); Text("Items subtotal  ${formatKes(items.sumOf { it.quantity*it.product.pricePerUnit })}"); Text("Delivery fee calculated at checkout"); Button(onClick={ if(name.isNotBlank() && phone.matches(Regex("^(?:07|01)\\d{8}$|^\\+254\\d{9}$")) && street.isNotBlank()) place("$street, $town") },Modifier.fillMaxWidth()) { Text("Place order") } }
}
@Composable fun Modifier.verticalScrollCompat(): Modifier = this.then(verticalScroll(rememberScrollState()))
