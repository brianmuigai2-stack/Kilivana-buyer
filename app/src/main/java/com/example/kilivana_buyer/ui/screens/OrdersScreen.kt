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

@Composable fun OrdersScreen(orders: List<Order>, open:(Order)->Unit) { Column(Modifier.fillMaxSize().padding(16.dp)) { Text("My orders", style=MaterialTheme.typography.headlineMedium); Row { listOf("All", "Active", "Delivered", "Cancelled").forEach { AssistChip(onClick={},label={Text(it)}) } }; if(orders.isEmpty()) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("Your orders will appear here")}; LazyColumn { items(orders) { order -> Card(Modifier.fillMaxWidth().padding(vertical=6.dp).clickable { open(order) }, border=BorderStroke(1.dp,Green100), colors=CardDefaults.cardColors(containerColor=NeutralSurface)) { Column(Modifier.padding(16.dp)) { Text(order.id,style=MaterialTheme.typography.titleMedium); Text("${order.date} · ${order.status}"); Text("${order.items.sumOf { it.quantity }} items · ${formatKes(order.total)}") } } } } } }
