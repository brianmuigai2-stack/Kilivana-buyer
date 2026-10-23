package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kilivana_buyer.model.Product
import com.example.kilivana_buyer.model.StockStatus
import com.example.kilivana_buyer.model.formatKes
import com.example.kilivana_buyer.ui.theme.*

@Composable
fun ProductDetailScreen(
    product: Product,
    isSaved: Boolean,
    back: () -> Unit,
    toggleSaved: () -> Unit,
    add: () -> Unit,
    buy: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(NeutralBackground)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box {
                ProductImage(product.imageRes, Modifier.fillMaxWidth())
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    shape = RoundedCornerShape(50), color = NeutralSurface.copy(alpha = .95f)
                ) {
                    IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = RoundedCornerShape(50), color = NeutralSurface.copy(alpha = .95f)
                ) {
                    IconButton(onClick = toggleSaved) {
                        Icon(
                            if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "Remove from saved items" else "Save item",
                            tint = if (isSaved) ErrorRed else TextPrimary
                        )
                    }
                }
            }
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StockBadge(product.stockStatus)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Star, null, tint = LowStockAccent, modifier = Modifier.size(18.dp))
                    Text("${product.rating}  ·  ${product.reviewCount} reviews", style = MaterialTheme.typography.labelLarge)
                }
                Text(product.name, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text("${formatKes(product.pricePerUnit)} / ${product.unit}", color = Green700, style = MaterialTheme.typography.titleLarge)
                Text("Minimum order: ${product.minimumOrder} ${product.unit}", color = TextSecondary)
                HorizontalDivider(color = NeutralDivider)
                Text(product.description, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                Surface(shape = RoundedCornerShape(16.dp), color = NeutralSurface) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Green600, modifier = Modifier.size(28.dp))
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text("Sold by", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text(product.sellerName, style = MaterialTheme.typography.titleMedium)
                            Text(product.sellerLocation, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Icon(Icons.Default.Star, null, tint = LowStockAccent, modifier = Modifier.size(18.dp))
                            Text(product.rating.toString(), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                DetailFact("Harvest", product.harvestNote ?: "Recently harvested")
                DetailFact("Delivery", "Available delivery options shown at checkout")
                Text("Ratings and seller details are from the current sample catalogue.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        Surface(color = NeutralSurface, shadowElevation = 8.dp) {
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = add, Modifier.weight(1f), enabled = product.stockStatus != StockStatus.OutOfStock) {
                    Text("Add to cart")
                }
                Button(onClick = buy, Modifier.weight(1f), enabled = product.stockStatus != StockStatus.OutOfStock) {
                    Text("Buy now")
                }
            }
        }
    }
}

@Composable
private fun DetailFact(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, Modifier.width(90.dp), style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@Composable
private fun StockBadge(status: StockStatus) {
    val label = when (status) {
        StockStatus.InStock -> "In stock"
        StockStatus.LowStock -> "Limited stock"
        StockStatus.OutOfStock -> "Out of stock"
    }
    val tint = when (status) {
        StockStatus.InStock -> Green700
        StockStatus.LowStock -> LowStockAccent
        StockStatus.OutOfStock -> ErrorRed
    }
    val background = when (status) {
        StockStatus.InStock -> Green50
        StockStatus.LowStock -> androidx.compose.ui.graphics.Color(0xFFFFF3E0)
        StockStatus.OutOfStock -> ErrorSurface
    }
    Surface(shape = RoundedCornerShape(50), color = background) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = tint, style = MaterialTheme.typography.labelMedium)
    }
}
