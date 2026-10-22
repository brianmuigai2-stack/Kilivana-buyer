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

@Composable fun AuthScreen(signup:Boolean,submit:(String,String)->Unit,switch:()->Unit){var email by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().imePadding().verticalScrollCompat().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Kilivana",style=MaterialTheme.typography.headlineLarge,color=Green600);Text(if(signup) "Create account" else "Welcome back",style=MaterialTheme.typography.headlineMedium);if(signup){OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Full name")});OutlinedTextField(phone,{phone=it},Modifier.fillMaxWidth(),label={Text("Phone number")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone))};OutlinedTextField(email,{email=it},Modifier.fillMaxWidth(),label={Text("Email")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),isError=email.isNotEmpty()&&!email.contains("@"));if(email.isNotEmpty()&&!email.contains("@"))Text("Enter a valid email address",color=MaterialTheme.colorScheme.error);OutlinedTextField(password,{password=it},Modifier.fillMaxWidth(),label={Text("Password")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),isError=password.isNotEmpty()&&password.length<8);if(password.isNotEmpty()&&password.length<8)Text("Use at least 8 characters",color=MaterialTheme.colorScheme.error);Button(onClick={submit(email,password)},Modifier.fillMaxWidth(),enabled=email.contains("@")&&password.length>=8&&(!signup||(name.isNotBlank()&&phone.matches(Regex("^(?:07|01)\\d{8}$|^\\+254\\d{9}$"))))){Text(if(signup) "Create account" else "Sign in")};TextButton(switch){Text(if(signup) "Already have an account? Sign in" else "New to Kilivana? Create account")} } }
