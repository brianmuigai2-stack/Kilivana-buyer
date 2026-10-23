package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kilivana_buyer.data.CartViewModel
import com.example.kilivana_buyer.model.CartItem
import com.example.kilivana_buyer.model.formatKes
import com.example.kilivana_buyer.ui.theme.*

@Composable
fun CartScreen(items: List<CartItem>, vm: CartViewModel, checkout: () -> Unit) {
    Column(Modifier.fillMaxSize().background(NeutralBackground).padding(16.dp)) {
        Text("Cart", style = MaterialTheme.typography.headlineMedium)
        if (items.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(Modifier.size(72.dp), shape = RoundedCornerShape(24.dp), color = Green50) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = Green600, modifier = Modifier.size(34.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Your cart is waiting", style = MaterialTheme.typography.titleLarge)
                    Text("Add fresh produce and it will show up here.", color = TextSecondary)
                    Button(onClick = checkout) { Text("Start shopping") }
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(items, key = { it.product.id }) { line ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Green100),
                        colors = CardDefaults.cardColors(containerColor = NeutralSurface)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductImage(line.product.imageRes, Modifier.size(88.dp))
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(line.product.name, style = MaterialTheme.typography.titleMedium)
                                Text("Sold by ${line.product.sellerName}", style = MaterialTheme.typography.bodySmall)
                                Text("${formatKes(line.product.pricePerUnit)} / ${line.product.unit}", color = Green600)
                                Row(
                                    Modifier.fillMaxWidth().padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        OutlinedIconButton(
                                            onClick = { vm.decrement(line.product.id) },
                                            modifier = Modifier.size(40.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = line.quantity > line.product.minimumOrder
                                        ) { Icon(Icons.Default.Remove, contentDescription = "Decrease quantity") }
                                        Text(
                                            line.quantity.toString(),
                                            Modifier.widthIn(min = 42.dp).padding(horizontal = 8.dp),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        OutlinedIconButton(
                                            onClick = { vm.increment(line.product.id) },
                                            modifier = Modifier.size(40.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        ) { Icon(Icons.Default.Add, contentDescription = "Increase quantity") }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    TextButton(onClick = { vm.remove(line.product.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null)
                                        Text("Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(shape = RoundedCornerShape(18.dp), color = NeutralSurface, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Items subtotal", color = TextSecondary)
                        Text(formatKes(vm.subtotal))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery", color = TextSecondary)
                        Text("Shown at checkout", style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider(color = NeutralDivider)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total before delivery", style = MaterialTheme.typography.titleMedium)
                        Text(formatKes(vm.subtotal), style = MaterialTheme.typography.titleMedium, color = Green700)
                    }
                }
            }
            Button(onClick = checkout, Modifier.fillMaxWidth().padding(top = 12.dp), enabled = items.isNotEmpty()) {
                Text("Proceed to checkout")
            }
        }
    }
}
