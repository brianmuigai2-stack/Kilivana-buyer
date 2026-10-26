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

@Composable fun OrderDetailScreen(order: Order, reorder: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScrollCompat().padding(16.dp)) {
        Text("Order ${order.code}", style = MaterialTheme.typography.headlineMedium)
        Text(order.date, color = TextSecondary)
        order.timeline.forEach { event ->
            Text(event.status.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
            if (!event.note.isNullOrBlank()) Text(event.note, color = TextSecondary)
            if (event.createdAt.isNotBlank()) Text(event.createdAt, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        if (order.timeline.isEmpty()) Text("Status: ${order.status}", modifier = Modifier.padding(vertical = 12.dp))
        order.items.forEach { Text("${it.quantity} × ${it.product.name}   ${formatKes(it.quantity * it.product.pricePerUnit)}") }
        Text("Items subtotal: ${formatKes(order.subtotal)}", modifier = Modifier.padding(top = 12.dp))
        Text("Delivery: ${formatKes(order.deliveryFee)}")
        Text("Total: ${formatKes(order.total)}", style = MaterialTheme.typography.titleMedium)
        Text("Delivery address: ${order.deliveryAddress}", modifier = Modifier.padding(vertical = 8.dp))
        Button(reorder) { Text("Reorder") }
    }
}
