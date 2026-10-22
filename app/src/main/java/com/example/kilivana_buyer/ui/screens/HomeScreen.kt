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

@Composable fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) { delay(1500); onFinished() }
    Surface(Modifier.fillMaxSize(), color = Green600) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("Kilivana", style = MaterialTheme.typography.headlineLarge, color = NeutralSurface); Text("Farm produce, delivered.", color = Green50) } }
}

@Composable fun HomeScreen(cartCount: Int, products: List<Product>, search: () -> Unit, category: (String) -> Unit, seeAll: () -> Unit, openProduct: (Product) -> Unit, openCart: () -> Unit, add: suspend (Product) -> Unit) {
    val categories = listOf("Vegetables" to Icons.Default.Eco, "Fruits" to Icons.Default.LunchDining, "Cereals and Grains" to Icons.Default.Grass, "Tubers" to Icons.Default.Kitchen, "Legumes" to Icons.Default.Spa, "Dairy" to Icons.Default.WaterDrop, "Poultry and Eggs" to Icons.Default.Egg)
    LazyColumn(Modifier.fillMaxSize().background(NeutralBackground), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { Row(Modifier.fillMaxWidth().background(Green600).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Kilivana", style = MaterialTheme.typography.titleMedium, color = NeutralSurface); Text("Set delivery location", style = MaterialTheme.typography.labelMedium, color = Green50) }; IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = NeutralSurface) }; IconButton(onClick = openCart) { BadgedBox(badge = { if (cartCount > 0) Badge { Text(cartCount.toString()) } }) { Icon(Icons.Default.ShoppingCart, "Cart", tint = NeutralSurface) } } } }
        item { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { OutlinedTextField("", {}, Modifier.weight(1f).clickable(onClick = search), enabled = false, placeholder = { Text("Search produce") }, leadingIcon = { Icon(Icons.Default.Search, "Search") }, shape = RoundedCornerShape(12.dp), singleLine = true); Spacer(Modifier.width(8.dp)); FilledTonalIconButton(onClick = search) { Icon(Icons.Default.Tune, "Filters") } } }
        item { Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))) { Image(painterResource(R.drawable.banner_bulk_orders), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop); Surface(Modifier.align(Alignment.CenterStart).padding(start = 12.dp), shape = RoundedCornerShape(12.dp), color = NeutralSurface.copy(alpha = 0.94f)) { Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) { Text("Fresh from local farms", style = MaterialTheme.typography.titleLarge, color = TextPrimary); Text("Browse produce from Kilivana sellers", color = TextSecondary); Spacer(Modifier.height(8.dp)); FilledTonalButton(onClick = seeAll) { Text("Shop products") } } } } }
        item { SectionHeader("Shop by category", "") }
        item { Column(Modifier.padding(horizontal = 12.dp)) { categories.chunked(4).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { (name, icon) -> Column(Modifier.weight(1f).clickable { category(name) }.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Surface(Modifier.size(60.dp), shape = RoundedCornerShape(14.dp), color = Green50) { Box(contentAlignment = Alignment.Center) { Icon(icon, name, tint = Green700, modifier = Modifier.size(30.dp)) } }; Text(name, style = MaterialTheme.typography.labelMedium, color = TextPrimary, maxLines = 2) } }; repeat(4-row.size) { Spacer(Modifier.weight(1f)) } } } } }
        item { SectionHeader("Recommended for you", "See all", seeAll) }
        item { ProductGrid(products.take(4), openProduct, add) }
        item { SectionHeader("Fresh this week", "See all", seeAll) }
        item { ProductGrid(products.drop(4).take(4), openProduct, add) }
        item { SectionHeader("Top sellers", "") }
        item { Row(Modifier.horizontalScrollCompat(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { listOf("Green Valley Farm · Kiambu", "Mavuno Fresh Farm · Thika", "Rift Harvest · Nakuru").forEach { Text(it, Modifier.padding(12.dp), style = MaterialTheme.typography.labelLarge) } } }
    }
}
