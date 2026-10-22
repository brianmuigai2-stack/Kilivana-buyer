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


@Composable fun SearchScreen(products:List<Product>,open:(Product)->Unit,back:()->Unit) { var query by remember { mutableStateOf("") }; Column(Modifier.fillMaxSize().padding(16.dp)) { Row(verticalAlignment=Alignment.CenterVertically) { IconButton(back){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")}; OutlinedTextField(query,{query=it},Modifier.weight(1f),placeholder={Text("Search produce")},singleLine=true) }; Text("Recent searches",style=MaterialTheme.typography.titleMedium); Text("Tomatoes · Sukuma wiki · Maize"); LazyColumn { items(products.filter { it.name.contains(query,true) || it.category.contains(query,true) }) { Text(it.name,Modifier.fillMaxWidth().clickable { open(it) }.padding(14.dp)) } } } }
