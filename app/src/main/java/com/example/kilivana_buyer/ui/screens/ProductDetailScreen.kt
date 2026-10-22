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

@Composable fun ProductDetailScreen(product:Product,back:()->Unit,add:()->Unit,buy:()->Unit) { Column(Modifier.fillMaxSize().background(NeutralBackground).verticalScrollCompat()) { Box { ProductImage(product.imageRes,Modifier.fillMaxWidth()); IconButton(back,Modifier.align(Alignment.TopStart)){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")}; IconButton({},Modifier.align(Alignment.TopEnd)){Icon(Icons.Default.FavoriteBorder,"Save item")} }; Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){repeat(4){ Surface(Modifier.padding(end=6.dp).size(50.dp),shape=RoundedCornerShape(8.dp),color=Green50){ProductImage(product.imageRes)} } }; Column(Modifier.padding(16.dp)) { Text(product.name,style=MaterialTheme.typography.headlineMedium); Text("★ ${product.rating} (${product.reviewCount} reviews)"); Text("${formatKes(product.pricePerUnit)} / ${product.unit}",color=Green600,style=MaterialTheme.typography.titleLarge); Text(if(product.stockStatus==StockStatus.InStock) "In stock" else "Low stock"); Text("Sold by ${product.sellerName} · ${product.sellerLocation}",style=MaterialTheme.typography.titleMedium); Text(product.description); Text("Category  ${product.category}"); Text("Harvested  ${product.harvestNote ?: "Recently"}"); Text("Minimum order  ${product.minimumOrder} ${product.unit}"); Text("Delivery window  Select at checkout") }; Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(add,Modifier.weight(1f)){Text("Add to cart")}; Button(buy,Modifier.weight(1f)){Text("Buy now")} } } }
