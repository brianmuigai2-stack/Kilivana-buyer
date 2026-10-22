package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kilivana_buyer.ui.theme.Green100
import com.example.kilivana_buyer.ui.theme.Green600
import com.example.kilivana_buyer.ui.theme.NeutralBackground
import com.example.kilivana_buyer.ui.theme.NeutralSurface
import com.example.kilivana_buyer.ui.theme.TextPrimary
import com.example.kilivana_buyer.ui.theme.TextSecondary

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    cartCount: Int = 0,
    onSearchClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onSeeAllProducts: () -> Unit = {},
    onProductClick: (Product) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize().background(NeutralBackground)) {
        TopAppBar(
            title = { Text("Kilivana", fontWeight = FontWeight.Bold, color = TextPrimary) },
            actions = {
                BadgedBox(badge = { if (cartCount > 0) Badge { Text(cartCount.toString()) } }) {
                    IconButton(onClick = {}) { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = TextPrimary) }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NeutralSurface)
        )
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(listOf(Green600, Green100)))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Good morning", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Fresh from the farm.", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Discover quality farm produce from trusted sellers.", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeutralSurface)
                    .clickable { onSearchClick() }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                    Spacer(Modifier.width(8.dp))
                    Text("Search products, crops...", color = TextSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Browse categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("View all", color = Green600, modifier = Modifier.clickable { onSeeAllProducts() })
            }
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(productCategories.filter { it != "All" }) { cat ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(NeutralSurface)
                            .clickable { onCategoryClick(cat) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(cat, color = TextPrimary)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Popular near you", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("See all", color = Green600, modifier = Modifier.clickable { onSeeAllProducts() })
            }
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sampleProducts.take(4)) { p ->
                    Card(modifier = Modifier.width(170.dp).clickable { onProductClick(p) }, colors = CardDefaults.cardColors(containerColor = NeutralSurface), shape = RoundedCornerShape(12.dp)) {
                        Column {
                            AsyncImage(model = p.imageUrl, contentDescription = p.name, modifier = Modifier.fillMaxWidth().height(110.dp))
                            Column(Modifier.padding(8.dp)) {
                                Text(p.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1)
                                Text("KES ${p.price}/${p.unit}", color = Green600, fontWeight = FontWeight.Bold)
                                p.sellerName?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1) }
                            }
                        }
                    }
                }
            }
        }
    }
}
