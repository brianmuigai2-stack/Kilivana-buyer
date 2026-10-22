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

@Composable fun OrderDetailScreen(order:Order,reorder:()->Unit) { Column(Modifier.fillMaxSize().verticalScrollCompat().padding(16.dp)) { Text("Order ${order.id}",style=MaterialTheme.typography.headlineMedium); listOf("Placed","Confirmed","Out for delivery","Delivered").forEachIndexed { index,step -> Text("${if(index==0 || order.status.ordinal>=index) "●" else "○"}  $step",Modifier.padding(vertical=8.dp)) }; order.items.forEach { Text("${it.quantity} × ${it.product.name}   ${formatKes(it.quantity*it.product.pricePerUnit)}") }; Text("Delivery address: ${order.deliveryAddress}"); Text("Payment: M-Pesa"); Button(reorder){Text("Reorder")} } }
