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
import com.example.kilivana_buyer.data.PaymentStatus
import com.example.kilivana_buyer.model.*
import com.example.kilivana_buyer.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable fun OrderConfirmationScreen(id:String,track:()->Unit){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(Icons.Default.CheckCircle,"Order placed",tint=Green600,modifier=Modifier.size(64.dp));Text("Order placed",style=MaterialTheme.typography.headlineMedium);Text("Order number $id");Button(track){Text("Track order")}}}

@Composable
fun PaymentStatusScreen(
	paymentId: Long,
	orderId: Long,
	accessToken: String?,
	loadStatus: suspend (Long, String?) -> PaymentStatus,
	continueShopping: () -> Unit
) {
	var payment by remember(paymentId) { mutableStateOf<PaymentStatus?>(null) }
	var error by remember(paymentId) { mutableStateOf<String?>(null) }
	var loading by remember(paymentId) { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

	suspend fun refreshStatus() {
		loading = true
		error = null
		try {
			payment = loadStatus(paymentId, accessToken)
		} catch (requestError: Exception) {
			error = requestError.message ?: "Could not load payment status."
		} finally {
			loading = false
		}
	}

	LaunchedEffect(paymentId) { refreshStatus() }

	Column(
		Modifier.fillMaxSize().background(NeutralBackground).padding(24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Green700, modifier = Modifier.size(52.dp))
		Spacer(Modifier.height(16.dp))
		Text("M-PESA payment", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
		Text("Order $orderId · Payment $paymentId", color = TextSecondary)
		Spacer(Modifier.height(20.dp))
		when {
			loading -> CircularProgressIndicator()
			error != null -> Text(error.orEmpty(), color = ErrorRed)
			payment != null -> {
				Text(payment!!.status.replace('_', ' '), style = MaterialTheme.typography.titleLarge, color = Green700)
				Text("Amount: ${formatKes(payment!!.amount.toInt())}", color = TextSecondary)
				payment!!.reference?.let { Text("Reference: $it", color = TextSecondary) }
				if (payment!!.status.equals("PENDING", ignoreCase = true)) {
					Text("Complete the prompt on your phone. Payment status updates after M-PESA responds.", color = TextSecondary)
				}
			}
		}
		Spacer(Modifier.height(20.dp))
		if (!payment?.status.equals("COMPLETED", ignoreCase = true) && !payment?.status.equals("SUCCESS", ignoreCase = true)) {
			OutlinedButton(onClick = { scope.launch { refreshStatus() } }, enabled = !loading) {
				Text("Check payment status")
			}
		}
		Button(onClick = continueShopping) { Text("View orders") }
	}
}
