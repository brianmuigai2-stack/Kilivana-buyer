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

@Composable fun AccountScreen(loggedIn:Boolean,orders:List<Order>,login:()->Unit,logout:()->Unit) { Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { Text("Account",style=MaterialTheme.typography.headlineMedium); if(!loggedIn) { Text("Sign in to manage your orders and delivery details."); Button(onClick=login){Text("Sign in")}; TextButton(onClick=logout){Text("Log out")} } else { Surface(Modifier.size(60.dp),shape=CircleShape,color=Green100){Box(contentAlignment=Alignment.Center){Text("KB",style=MaterialTheme.typography.titleLarge)}}; Text("Kilivana buyer"); Text("Orders   ${orders.size}     Pending   ${orders.count{it.status==OrderStatus.Pending}}     Delivered   ${orders.count{it.status==OrderStatus.Delivered}}"); listOf("My orders","Saved items","Addresses","Payment methods","Notifications","Help and support","About").forEach { Text(it,Modifier.fillMaxWidth().clickable{}.padding(vertical=12.dp)) }; TextButton(onClick=logout){Text("Log out")} } } }
