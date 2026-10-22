package com.example.kilivana_buyer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kilivana_buyer.ui.theme.Green600

@Composable
fun AuthenticationRequiredScreen(
    feature: String,
    signIn: () -> Unit,
    createAccount: () -> Unit,
    goBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("You're almost there", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            if (feature == "checkout") {
                "Sign in or create an account to complete your purchase and manage your orders."
            } else {
                "Sign in or create an account to access $feature. You can keep browsing as a guest."
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = signIn, Modifier.fillMaxWidth()) { Text("Sign in") }
        OutlinedButton(onClick = createAccount, Modifier.fillMaxWidth()) { Text("Create account") }
        TextButton(onClick = goBack) {
            Text(if (feature == "checkout") "Return to cart" else "Continue shopping", color = Green600)
        }
    }
}
