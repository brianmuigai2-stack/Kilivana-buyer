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

@Composable fun CartScreen(items: List<CartItem>, vm: CartViewModel, checkout: () -> Unit) {
    Column(Modifier.fillMaxSize().background(NeutralBackground).padding(16.dp)) { Text("Cart", style = MaterialTheme.typography.headlineMedium); if (items.isEmpty()) Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Your cart is empty"); Button(onClick = checkout) { Text("Start shopping") } } } else { LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(items, key = { it.product.id }) { line -> Card(border = BorderStroke(1.dp, Green100), colors = CardDefaults.cardColors(containerColor = NeutralSurface)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { ProductImage(line.product.imageRes, Modifier.size(76.dp)); Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(line.product.name, style = MaterialTheme.typography.titleMedium); Text("Sold by ${line.product.sellerName}"); Text("${formatKes(line.product.pricePerUnit)} / ${line.product.unit}", color = Green600); Row(verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = { vm.decrement(line.product.id) }) { Text("−") }; Text(line.quantity.toString()); TextButton(onClick = { vm.increment(line.product.id) }) { Text("+") }; TextButton(onClick = { vm.remove(line.product.id) }) { Text("Remove") } } } } } } }; OutlinedTextField("", {}, Modifier.fillMaxWidth(), label = { Text("Promo code") }, singleLine = true); Spacer(Modifier.height(12.dp)); Text("Items subtotal  ${formatKes(vm.subtotal)}"); Text("Delivery fee calculated at checkout"); Text("Total before delivery  ${formatKes(vm.subtotal)}", style = MaterialTheme.typography.titleLarge); Button(onClick = checkout, Modifier.fillMaxWidth().padding(top = 12.dp), enabled = items.isNotEmpty()) { Text("Proceed to checkout") } } }
}
