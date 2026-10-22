package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kilivana_buyer.ui.theme.Green600
import com.example.kilivana_buyer.ui.theme.NeutralBackground
import com.example.kilivana_buyer.ui.theme.NeutralSurface
import com.example.kilivana_buyer.ui.theme.TextPrimary
import com.example.kilivana_buyer.ui.theme.TextSecondary

data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val description: String = "",
    val imageUrl: String = "",
    val unit: String = "kg",
    val sellerName: String? = null,
    val availability: String? = null,
    val category: String = "All"
)

// Clearly separated sample/preview data for development only.
val sampleProducts = listOf(
    Product(1, "Fresh Tomatoes", 120, "Juicy red tomatoes", "https://picsum.photos/seed/tomato/400", "kg", "Green Valley Farm", "In stock", "Vegetables"),
    Product(2, "Ripe Bananas", 80, "Sweet bananas", "https://picsum.photos/seed/banana/400", "bunch", "Mama Njeri Farm", "In stock", "Fruits"),
    Product(3, "Maize (Corn)", 3500, "Dried maize grain", "https://picsum.photos/seed/maize/400", "90kg bag", "Rift Valley Grains", "Limited", "Cereals & Grains"),
    Product(4, "Irish Potatoes", 90, "Fresh potatoes", "https://picsum.photos/seed/potato/400", "kg", "Nyandarua Farms", "In stock", "Tubers"),
    Product(5, "Green Beans", 150, "Fresh green beans", "https://picsum.photos/seed/beans/400", "kg", "Highland Veggies", "In stock", "Legumes"),
    Product(6, "Avocados", 40, "Hass avocados", "https://picsum.photos/seed/avocado/400", "piece", "Murang'a Orchards", "In stock", "Fruits"),
    Product(7, "Onions", 100, "Red onions", "https://picsum.photos/seed/onion/400", "kg", "Kajiado Traders", "In stock", "Vegetables"),
    Product(8, "Sweet Potatoes", 70, "Orange fleshed", "https://picsum.photos/seed/spotato/400", "kg", "Western Farms", "In stock", "Tubers")
)

val productCategories = listOf("All", "Vegetables", "Fruits", "Cereals & Grains", "Tubers", "Legumes")

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    modifier: Modifier = Modifier,
    cartCount: Int = 0,
    onAddToCart: (Product) -> Unit = {},
    onProductClick: (Product) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val filtered = sampleProducts.filter { p ->
        (selectedCategory == "All" || p.category == selectedCategory) &&
            (query.isBlank() || p.name.contains(query, ignoreCase = true))
    }

    Scaffold(
        modifier = modifier,
        containerColor = NeutralBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kilivana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Deliver to Nairobi", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
                    }
                    BadgedBox(badge = { if (cartCount > 0) Badge { Text(cartCount.toString()) } }) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search products, crops...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeutralSurface,
                        unfocusedContainerColor = NeutralSurface,
                        focusedBorderColor = Green600,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeutralSurface)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = TextPrimary)
                }
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productCategories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green600,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Text(
                "Featured products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products found", color = TextSecondary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { product ->
                        ProductCard(product = product, onClick = { onProductClick(product) }, onAddToCart = { onAddToCart(product) })
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit = {},
    onAddToCart: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(NeutralBackground),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "KES ${product.price}/${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
                product.sellerName?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                product.availability?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onAddToCart, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Add to cart", tint = Green600)
                    }
                }
            }
        }
    }
}
