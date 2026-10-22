package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kilivana_buyer.model.Product
import com.example.kilivana_buyer.model.StockStatus
import com.example.kilivana_buyer.model.formatKes
import com.example.kilivana_buyer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    products: List<Product>,
    initialCategory: String?,
    open: (Product) -> Unit,
    add: suspend (Product) -> Unit,
    back: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember(initialCategory) { mutableStateOf(initialCategory) }
    var filter by remember { mutableStateOf(false) }
    var stockOnly by remember { mutableStateOf(false) }
    var priceLimit by remember { mutableFloatStateOf((products.maxOfOrNull { it.pricePerUnit } ?: 1).toFloat()) }
    var sort by remember { mutableStateOf("Relevance") }
    var sortMenu by remember { mutableStateOf(false) }
    val categories = remember(products) { products.map(Product::category).distinct() }
    val maxPrice = (products.maxOfOrNull { it.pricePerUnit } ?: 1).toFloat().coerceAtLeast(1f)
    val shown = products.asSequence()
        .filter { category == null || it.category == category }
        .filter { it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) || it.sellerName.contains(query, ignoreCase = true) }
        .filter { it.pricePerUnit <= priceLimit }
        .filter { !stockOnly || it.stockStatus != StockStatus.OutOfStock }
        .toList().let { filtered ->
            when (sort) {
                "Price: low to high" -> filtered.sortedBy(Product::pricePerUnit)
                "Price: high to low" -> filtered.sortedByDescending(Product::pricePerUnit)
                "Top rated" -> filtered.sortedByDescending(Product::rating)
                else -> filtered
            }
        }

    Column(Modifier.fillMaxSize().background(NeutralBackground)) {
        Text("Shop products", Modifier.padding(start = 16.dp, top = 16.dp), style = MaterialTheme.typography.headlineMedium)
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search products and sellers") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search products") },
                trailingIcon = { if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text("Clear") } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            IconButton(onClick = { filter = true }) { Icon(Icons.Default.Tune, contentDescription = "Filter products") }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = category == null, onClick = { category = null }, label = { Text("All") })
            categories.forEach { name -> FilterChip(selected = category == name, onClick = { category = name }, label = { Text(name) }) }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${shown.size} products", Modifier.weight(1f), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Box {
                TextButton(onClick = { sortMenu = true }) { Text("Sort: $sort") }
                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                    listOf("Relevance", "Price: low to high", "Price: high to low", "Top rated").forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { sort = option; sortMenu = false })
                    }
                }
            }
        }
        if (shown.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No products found")
                    TextButton(onClick = { query = ""; category = null; stockOnly = false; priceLimit = maxPrice }) { Text("Clear filters") }
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f)) { items(shown.chunked(2)) { row -> ProductGrid(row, open, add) } }
        }
    }

    if (filter) ModalBottomSheet(onDismissRequest = { filter = false }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Filter products", style = MaterialTheme.typography.titleLarge)
            Text("Maximum price: ${formatKes(priceLimit.toInt())}")
            Slider(value = priceLimit, onValueChange = { priceLimit = it }, valueRange = 1f..maxPrice)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Available products only", Modifier.weight(1f))
                Switch(checked = stockOnly, onCheckedChange = { stockOnly = it })
            }
            Text("Category")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = category == null, onClick = { category = null }, label = { Text("All") })
                categories.forEach { name -> FilterChip(selected = category == name, onClick = { category = name }, label = { Text(name) }) }
            }
            Button(onClick = { filter = false }, Modifier.fillMaxWidth()) { Text("Show ${shown.size} products") }
        }
    }
}
