package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.kilivana_buyer.data.CartViewModel
import com.example.kilivana_buyer.model.*
import com.example.kilivana_buyer.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@Composable fun Modifier.horizontalScrollCompat(): Modifier = this.horizontalScroll(rememberScrollState())
@Composable fun Modifier.verticalScrollCompat(): Modifier = this.verticalScroll(rememberScrollState())
@Composable fun SectionHeader(title: String, action: String, onClick: () -> Unit = {}) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge); if (action.isNotEmpty()) TextButton(onClick) { Text(action) } } }
@Composable fun ProductGrid(products: List<Product>, open: (Product) -> Unit, add: suspend (Product) -> Unit) { Column(Modifier.padding(horizontal = 12.dp)) { products.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { ProductTile(it, Modifier.weight(1f), open, add) }; if (row.size == 1) Spacer(Modifier.weight(1f)) } } } }
@Composable fun ProductTile(product: Product, modifier: Modifier = Modifier, open: (Product) -> Unit, add: suspend (Product) -> Unit) {
    val scope = rememberCoroutineScope()
    Card(modifier.padding(bottom = 12.dp).clickable { open(product) }, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, NeutralDivider.copy(alpha = .65f)), colors = CardDefaults.cardColors(containerColor = NeutralSurface)) {
        Column {
            ProductImage(Modifier.fillMaxWidth(), product.name, product.imageUrl)
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                when (product.stockStatus) {
                    StockStatus.LowStock -> Text("Limited stock", color = LowStockAccent, style = MaterialTheme.typography.labelSmall)
                    StockStatus.OutOfStock -> Text("Currently unavailable", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
                    StockStatus.InStock -> Text(product.category, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
                Text(product.name, maxLines = 2, style = MaterialTheme.typography.titleMedium)
                Text("${formatKes(product.pricePerUnit)} / ${product.unit}", color = Green700, style = MaterialTheme.typography.titleSmall)
                product.sellerName?.let { Text(it, maxLines = 1, style = MaterialTheme.typography.labelSmall, color = TextSecondary) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    FilledTonalIconButton(
                        onClick = { scope.launch { add(product) } },
                        modifier = Modifier.size(40.dp),
                        enabled = product.stockStatus != StockStatus.OutOfStock
                    ) { Icon(Icons.Default.AddShoppingCart, "Add ${product.name} to cart") }
                }
            }
        }
    }
}
@Composable fun ProductImage(modifier: Modifier = Modifier, contentDescription: String? = null, imageUrl: String? = null) {
    val context = LocalContext.current
    var loading by remember(imageUrl) { mutableStateOf(!imageUrl.isNullOrBlank()) }
    val request = remember(context, imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .addHeader("ngrok-skip-browser-warning", "1")
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
    val imageModifier = modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))
    Box(imageModifier.background(NeutralSurface), contentAlignment = Alignment.Center) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onLoading = { loading = true },
                onSuccess = { loading = false },
                onError = { loading = false }
            )
        }
        if (loading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}
