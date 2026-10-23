package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kilivana_buyer.ui.theme.*

@Composable
fun AuthScreen(signup: Boolean, submit: (String, String) -> Unit, switch: () -> Unit, back: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val validEmail = email.contains("@") && email.substringAfter('@').contains('.')
    val validPhone = phone.matches(Regex("^(?:07|01)\\d{8}$|^\\+254\\d{9}$"))

    Column(
        Modifier.fillMaxSize().background(NeutralBackground).verticalScroll(rememberScrollState()).imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Kilivana", style = MaterialTheme.typography.titleLarge, color = Green700)
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(if (signup) "Join Kilivana" else "Welcome back", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Text(
                if (signup) "Create an account to manage orders and delivery details." else "Sign in to continue to your account.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(Modifier.height(18.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = NeutralSurface, shadowElevation = 1.dp) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (signup) {
                        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full name") }, singleLine = true)
                        OutlinedTextField(
                            phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Phone number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true,
                            isError = phone.isNotEmpty() && !validPhone,
                            supportingText = { if (phone.isNotEmpty() && !validPhone) Text("Enter a valid Kenyan number") }
                        )
                    }
                    OutlinedTextField(
                        email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true,
                        isError = email.isNotEmpty() && !validEmail,
                        supportingText = { if (email.isNotEmpty() && !validEmail) Text("Enter a valid email address") }
                    )
                    OutlinedTextField(
                        password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = password.isNotEmpty() && password.length < 8,
                        supportingText = { if (password.isNotEmpty() && password.length < 8) Text("Use at least 8 characters") },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = if (showPassword) "Hide password" else "Show password")
                            }
                        }
                    )
                    Button(
                        onClick = { submit(email, password) },
                        Modifier.fillMaxWidth(),
                        enabled = validEmail && password.length >= 8 && (!signup || (name.isNotBlank() && validPhone))
                    ) { Text(if (signup) "Create account" else "Sign in") }
                    TextButton(onClick = switch, Modifier.fillMaxWidth()) {
                        Text(if (signup) "Already have an account? Sign in" else "New to Kilivana? Create account")
                    }
                    Text(
                        "Account services are not connected yet. Your details will not be submitted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
