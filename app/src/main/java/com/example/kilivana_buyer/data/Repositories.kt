package com.example.kilivana_buyer.data

import com.example.kilivana_buyer.BuildConfig
import com.example.kilivana_buyer.model.*
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getCategories(): List<String>
}
interface OrderRepository {
    suspend fun getOrders(buyerId: String, accessToken: String?, products: List<Product>): List<Order>
    suspend fun getOrder(id: String, accessToken: String?, products: List<Product>): Order
    suspend fun getTimeline(id: String, accessToken: String?): List<OrderEvent>
}
interface CheckoutRepository {
    suspend fun getAddresses(accessToken: String?): List<DeliveryAddress>
    suspend fun saveAddress(
        addressId: Long?, userId: Long, label: String, addressText: String,
        latitude: Double, longitude: Double, accessToken: String?
    ): DeliveryAddress
    suspend fun deleteAddress(addressId: Long, accessToken: String?)
    suspend fun checkout(items: List<CartItem>, accessToken: String?)
    suspend fun requestDeliveryQuote(addressId: Long, accessToken: String?): DeliveryQuote
    suspend fun createOrder(quoteId: String, phoneNumber: String, accessToken: String?): CheckoutOrder
}
interface PaymentRepository {
    suspend fun initiateBuyerStkPush(orderId: Long, phoneNumber: String, accessToken: String?): StkPushResult
    suspend fun getStatus(paymentId: Long, accessToken: String?): PaymentStatus
}
interface AuthRepository {
    fun isLoggedIn(): Boolean
    fun currentBuyerId(): String?
    fun currentName(): String?
    fun currentEmail(): String?
    fun currentPhone(): String?
    fun accessToken(): String?
    suspend fun signIn(email: String, password: String): String?
    suspend fun register(name: String, email: String, phone: String, password: String, username: String, region: String): String?
    suspend fun requestEmailVerification(email: String): EmailVerificationChallenge
    suspend fun verifyEmail(challengeId: String, email: String, code: String)
    suspend fun refreshSession(): Boolean
    suspend fun forgotPassword(email: String): String?
    suspend fun signOut()
}

data class EmailVerificationChallenge(
    val challengeId: String,
    val expiresInSeconds: Int,
    val resendAfterSeconds: Int
)

data class CheckoutOrder(val id: Long, val message: String)
data class DeliveryAddress(
    val id: Long,
    val userId: Long,
    val addressText: String,
    val latitude: Double,
    val longitude: Double,
    val label: String
)
data class DeliveryQuote(
    val quoteId: String,
    val addressId: Long,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val currency: String,
    val expiresAt: String
)
data class StkPushResult(val paymentId: Long, val status: String, val customerMessage: String?)
data class PaymentStatus(val status: String, val amount: Double, val reference: String?)
data class CheckoutSubmission(
    val orderId: Long? = null,
    val paymentId: Long? = null,
    val paymentMessage: String? = null,
    val error: String? = null
)

class ApiAuthRepository(context: Context, private val baseUrl: String = BuildConfig.API_BASE_URL) : AuthRepository {
    private val preferences = context.getSharedPreferences("kilivana_auth", Context.MODE_PRIVATE)
    override fun isLoggedIn() = !accessToken().isNullOrBlank() && !currentBuyerId().isNullOrBlank()
    override fun currentBuyerId() = preferences.getString("buyer_id", null)
    override fun currentName() = preferences.getString("name", null)
    override fun currentEmail() = preferences.getString("email", null)
    override fun currentPhone() = preferences.getString("phone", null)
    override fun accessToken() = preferences.getString("access_token", null)
    private fun refreshToken() = preferences.getString("refresh_token", null)

    override suspend fun signIn(email: String, password: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = request("/api/v1/auth/login", "POST", JSONObject().put("email", email).put("password", password))
            saveSession(response.getJSONObject("data"))
            loadProfile()
            null
        } catch (error: Exception) { clearSession(); error.message ?: "Sign-in failed." }
    }

    override suspend fun register(name: String, email: String, phone: String, password: String, username: String, region: String): String? = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().put("name", name).put("email", email).put("phone", phone)
                .put("password", password).put("role", "BUYER").put("username", username).put("region", region)
            request("/api/v1/auth/register", "POST", payload)
            null
        } catch (error: Exception) { error.message ?: "Account registration failed." }
    }

    override suspend fun requestEmailVerification(email: String): EmailVerificationChallenge = withContext(Dispatchers.IO) {
        val response = request(
            "/api/v1/auth/email-verification/request",
            "POST",
            JSONObject().put("email", email)
        ).getJSONObject("data")
        EmailVerificationChallenge(
            challengeId = response.optString("challengeId").takeIf(String::isNotBlank)
                ?: error("Verification response has no challenge ID."),
            expiresInSeconds = response.optInt("expiresInSeconds"),
            resendAfterSeconds = response.optInt("resendAfterSeconds")
        )
    }

    override suspend fun verifyEmail(challengeId: String, email: String, code: String) = withContext(Dispatchers.IO) {
        val response = request(
            "/api/v1/auth/email-verification/verify",
            "POST",
            JSONObject().put("challengeId", challengeId).put("email", email).put("code", code)
        )
        if (!response.optBoolean("success", true)) {
            error(response.optString("message").ifBlank { "Email verification failed." })
        }
    }

    override suspend fun refreshSession(): Boolean = withContext(Dispatchers.IO) {
        try {
            loadProfile()
            true
        } catch (_: Exception) {
            val refreshToken = refreshToken() ?: run { clearSession(); return@withContext false }
            try {
                val response = request("/api/v1/auth/refresh", "POST", JSONObject().put("refreshToken", refreshToken))
                saveSession(response.getJSONObject("data"))
                loadProfile()
                true
            } catch (_: Exception) { clearSession(); false }
        }
    }

    override suspend fun forgotPassword(email: String): String? = withContext(Dispatchers.IO) {
        try {
            val query = "email=${java.net.URLEncoder.encode(email, Charsets.UTF_8.name())}&application=BUYER"
            val response = request("/api/v1/auth/forgot-password?$query", "POST")
            response.optString("message").ifBlank { "If that email is registered, reset instructions have been sent." }
        } catch (error: Exception) { error.message ?: "Could not request password reset." }
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        val token = accessToken()
        clearSession()
        token?.let { runCatching { request("/api/v1/auth/logout", "POST", token = it) } }
        Unit
    }

    private fun loadProfile() {
        val token = accessToken() ?: error("Sign-in did not return an access token.")
        val user = request("/api/v1/auth/me", "GET", token = token).getJSONObject("data")
        if (user.optString("role").uppercase() != "BUYER") {
            clearSession()
            error("This account is not a buyer account.")
        }
        preferences.edit().putString("buyer_id", user.optLong("id").toString())
            .putString("name", user.optString("name").takeIf(String::isNotBlank))
            .putString("email", user.optString("email").takeIf(String::isNotBlank))
            .putString("phone", user.optString("phone").ifBlank { user.optString("phoneNumber") }.takeIf(String::isNotBlank))
            .apply()
    }

    private fun saveSession(data: JSONObject) {
        val access = data.optString("accessToken").takeIf(String::isNotBlank) ?: error("Authentication response has no access token.")
        val refresh = data.optString("refreshToken")
        val user = data.optJSONObject("user")
        val editor = preferences.edit().putString("access_token", access).putString("refresh_token", refresh)
        user?.let { editor.putString("buyer_id", it.optLong("id").takeIf { id -> id > 0 }?.toString()) }
        editor.apply()
    }

    private fun clearSession() { preferences.edit().clear().apply() }

    private fun request(path: String, method: String, payload: JSONObject? = null, token: String? = null): JSONObject {
        val connection = URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("ngrok-skip-browser-warning", "1")
            token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            payload?.let {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer -> writer.write(it.toString()) }
            }
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(body) }.getOrDefault(JSONObject())
            if (status !in 200..299) error(json.optString("message").ifBlank { "Authentication request failed (HTTP $status)." })
            return json
        } finally { connection.disconnect() }
    }
}

class ApiProductRepository(private val baseUrl: String = BuildConfig.API_BASE_URL) : ProductRepository {
    override suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        val endpoint = baseUrl.trimEnd('/') + "/api/v1/products"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("ngrok-skip-browser-warning", "1")
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) error("Products request failed (HTTP $status): ${errorMessage(body)}")
            val data = JSONObject(body).optJSONArray("data") ?: error("Products response has no data array")
            val products = (0 until data.length()).map { index -> data.getJSONObject(index).toProduct() }
            coroutineScope {
                products.map { product -> async(Dispatchers.IO) {
                    if (product.imageUrl != null) product
                    else product.copy(imageUrl = getPrimaryImageUrl(product.id))
                } }.awaitAll()
            }
        } finally { connection.disconnect() }
    }

    override suspend fun getCategories(): List<String> = withContext(Dispatchers.IO) {
        val connection = URL(baseUrl.trimEnd('/') + "/api/v1/categories/active").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("ngrok-skip-browser-warning", "1")
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) error("Categories request failed (HTTP $status)")
            val data = JSONObject(body).optJSONArray("data") ?: error("Categories response has no data array")
            (0 until data.length()).mapNotNull { data.optJSONObject(it)?.optString("name")?.takeIf(String::isNotBlank) }
        } finally { connection.disconnect() }
    }

    private fun getPrimaryImageUrl(productId: String): String? {
        val connection = URL("${baseUrl.trimEnd('/')}/api/v1/products/$productId/images").openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("ngrok-skip-browser-warning", "1")
            if (connection.responseCode !in 200..299) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val images = JSONObject(body).optJSONArray("data") ?: return null
            val primary = (0 until images.length()).mapNotNull { images.optJSONObject(it) }
                .firstOrNull { it.optBoolean("isPrimary") } ?: images.optJSONObject(0)
            primary?.optString("url")?.let(::resolveImageUrl)
        } catch (_: Exception) { null }
        finally { connection.disconnect() }
    }

    private fun resolveImageUrl(path: String?): String? {
        val value = path?.trim()?.takeIf(String::isNotBlank) ?: return null
        return runCatching { URL(URL(baseUrl.trimEnd('/') + "/"), value).toString() }.getOrNull()
    }

    private fun JSONObject.toProduct(): Product {
        val image = optJSONArray("images")?.let { images ->
            (0 until images.length()).asSequence().mapNotNull { images.optJSONObject(it) }
                .firstOrNull { it.optBoolean("isPrimary") } ?: images.optJSONObject(0)
        }
        val stock = optInt("stockQty", 0) - optInt("reservedQty", 0)
        val category = optString("categoryName", "Produce")
        return Product(
            id = optLong("id").toString(), name = optString("name", "Product"), category = category,
            pricePerUnit = optDouble("price", 0.0).toInt(), unit = optString("unit", "unit"),
            description = optString("description", ""),
            sellerName = optString("sellerName").takeIf(String::isNotBlank),
            sellerLocation = optString("sellerCounty").takeIf(String::isNotBlank),
            stockStatus = when { stock <= 0 -> StockStatus.OutOfStock; stock <= optInt("lowStockThreshold", 5) -> StockStatus.LowStock; else -> StockStatus.InStock },
            minimumOrder = optInt("minimumOrderQty", 1).coerceAtLeast(1),
            harvestNote = optString("harvestDate").takeIf(String::isNotBlank), imageUrl = resolveImageUrl(image?.optString("url"))
        )
    }

    private fun errorMessage(body: String): String = runCatching { JSONObject(body).optString("message") }.getOrDefault("").ifBlank { "Unable to load catalogue" }
}
class ApiOrderRepository(private val baseUrl: String = BuildConfig.API_BASE_URL) : OrderRepository {
    override suspend fun getOrders(buyerId: String, accessToken: String?, products: List<Product>): List<Order> = withContext(Dispatchers.IO) {
        val data = request("/api/v1/orders/buyer/${buyerId.encodePath()}", accessToken).optJSONArray("data")
            ?: error("Orders response has no data array")
        (0 until data.length()).map { data.getJSONObject(it).toOrder(products) }
    }

    override suspend fun getOrder(id: String, accessToken: String?, products: List<Product>): Order = withContext(Dispatchers.IO) {
        request("/api/v1/orders/${id.encodePath()}", accessToken).getJSONObject("data").toOrder(products)
    }

    override suspend fun getTimeline(id: String, accessToken: String?): List<OrderEvent> = withContext(Dispatchers.IO) {
        val data = request("/api/v1/orders/${id.encodePath()}/timeline", accessToken).optJSONArray("data")
            ?: error("Order timeline response has no data array")
        (0 until data.length()).map { index ->
            val event = data.getJSONObject(index)
            OrderEvent(event.optString("status"), event.optString("note").takeIf(String::isNotBlank), event.optString("createdAt"))
        }
    }

    private fun request(path: String, accessToken: String?): JSONObject {
        val connection = URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("ngrok-skip-browser-warning", "1")
            accessToken?.takeIf(String::isNotBlank)?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching { JSONObject(body).optString("message") }.getOrDefault("")
                error(message.ifBlank { "Order request failed (HTTP $status)" })
            }
            return JSONObject(body)
        } finally { connection.disconnect() }
    }

    private fun JSONObject.toOrder(products: List<Product>): Order {
        val itemData = optJSONArray("items")
        val orderItems = if (itemData == null) emptyList() else (0 until itemData.length()).mapNotNull { index ->
            val item = itemData.getJSONObject(index)
            val productId = item.optLong("productId", -1).toString()
            val product = products.firstOrNull { it.id == productId } ?: return@mapNotNull null
            CartItem(product, item.optInt("quantity", 1).coerceAtLeast(1))
        }
        val rawStatus = optString("status").uppercase()
        val status = when {
            rawStatus.contains("CANCEL") -> OrderStatus.Cancelled
            rawStatus.contains("DELIVER") || rawStatus.contains("RECEIVED") -> OrderStatus.Delivered
            rawStatus.contains("TRANSIT") || rawStatus.contains("SHIPPED") || rawStatus.contains("DELIVERY") -> OrderStatus.OutForDelivery
            rawStatus.contains("CONFIRM") || rawStatus.contains("PROCESS") -> OrderStatus.Confirmed
            else -> OrderStatus.Pending
        }
        val id = optLong("id").toString()
        return Order(
            id = id, code = optString("code", id), date = optString("createdAt"), status = status, items = orderItems,
            subtotal = optDouble("subtotal", 0.0).toInt(), deliveryFee = optDouble("deliveryFee", 0.0).toInt(),
            total = optDouble("total", 0.0).toInt(), deliveryAddress = optString("deliveryAddress", "")
        )
    }

    private fun String.encodePath() = java.net.URLEncoder.encode(this, Charsets.UTF_8.name())
}

class ApiCheckoutRepository(private val baseUrl: String = BuildConfig.API_BASE_URL) : CheckoutRepository {
    override suspend fun getAddresses(accessToken: String?): List<DeliveryAddress> = withContext(Dispatchers.IO) {
        val token = accessToken?.takeIf(String::isNotBlank) ?: error("Sign in to load your saved addresses.")
        val response = request("/api/v1/users/addresses", "GET", token)
        val data = response.optJSONArray("data")
            ?: response.optJSONObject("data")?.optJSONArray("content")
            ?: error("Address response has no data list.")
        (0 until data.length()).mapNotNull { index ->
            val address = data.optJSONObject(index) ?: return@mapNotNull null
            val id = address.optLong("id").takeIf { it > 0 } ?: return@mapNotNull null
            DeliveryAddress(
                id = id,
                userId = address.optLong("userId"),
                addressText = address.optString("addressText"),
                latitude = address.optDouble("latitude", 0.0),
                longitude = address.optDouble("longitude", 0.0),
                label = address.optString("label").ifBlank { "Address $id" }
            )
        }
    }

    override suspend fun saveAddress(
        addressId: Long?, userId: Long, label: String, addressText: String,
        latitude: Double, longitude: Double, accessToken: String?
    ): DeliveryAddress = withContext(Dispatchers.IO) {
        val token = accessToken?.takeIf(String::isNotBlank) ?: error("Sign in to save an address.")
        val payload = JSONObject()
            .put("addressText", addressText)
            .put("latitude", latitude)
            .put("longitude", longitude)
            .put("label", label)
            .put("userId", userId)
        val path = if (addressId == null) "/api/v1/users/addresses" else "/api/v1/users/addresses/$addressId"
        val response = request(path, if (addressId == null) "POST" else "PUT", token, payload)
        val data = response.optJSONObject("data") ?: error("Address response has no data.")
        DeliveryAddress(
            id = data.optLong("id").takeIf { it > 0 } ?: error("Address response has no ID."),
            userId = data.optLong("userId", userId),
            addressText = data.optString("addressText", addressText),
            latitude = data.optDouble("latitude", latitude),
            longitude = data.optDouble("longitude", longitude),
            label = data.optString("label", label)
        )
    }

    override suspend fun deleteAddress(addressId: Long, accessToken: String?) = withContext(Dispatchers.IO) {
        val token = accessToken?.takeIf(String::isNotBlank) ?: error("Sign in to delete an address.")
        request("/api/v1/users/addresses/$addressId", "DELETE", token)
        Unit
    }

    override suspend fun checkout(items: List<CartItem>, accessToken: String?) = withContext(Dispatchers.IO) {
        require(!accessToken.isNullOrBlank()) { "Sign in to continue checkout." }
        require(items.isNotEmpty()) { "Your cart is empty." }
        items.forEach { line ->
            val productId = line.product.id.toLongOrNull() ?: error("Invalid product ID: ${line.product.id}")
            val item = JSONObject().put("productId", productId).put("quantity", line.quantity)
            request("/api/v1/cart/items", "POST", accessToken, item)
        }
        request("/api/v1/checkout", "POST", accessToken)
        Unit
    }

    override suspend fun requestDeliveryQuote(addressId: Long, accessToken: String?): DeliveryQuote = withContext(Dispatchers.IO) {
        val token = accessToken?.takeIf(String::isNotBlank) ?: error("Sign in to request a delivery quote.")
        val response = request(
            "/api/v1/checkout/delivery-quote",
            "POST",
            token,
            JSONObject().put("addressId", addressId)
        )
        val data = response.optJSONObject("data") ?: error("Delivery quote response has no data.")
        DeliveryQuote(
            quoteId = data.optString("quoteId").takeIf(String::isNotBlank) ?: error("Delivery quote has no ID."),
            addressId = data.optLong("addressId", addressId),
            subtotal = data.optDouble("subtotal", 0.0),
            deliveryFee = data.optDouble("deliveryFee", 0.0),
            total = data.optDouble("total", 0.0),
            currency = data.optString("currency").ifBlank { "KES" },
            expiresAt = data.optString("expiresAt")
        )
    }

    override suspend fun createOrder(quoteId: String, phoneNumber: String, accessToken: String?): CheckoutOrder = withContext(Dispatchers.IO) {
        val token = accessToken?.takeIf(String::isNotBlank) ?: error("Sign in to create an order.")
        val response = request(
            "/api/v1/checkout/order",
            "POST",
            token,
            JSONObject().put("quoteId", quoteId).put("phoneNumber", normalizeKenyanPhone(phoneNumber))
        )
        val data = response.opt("data")
        val orderId = when (data) {
            is Number -> data.toLong()
            is JSONObject -> when {
                data.has("id") -> data.optLong("id")
                data.has("orderId") -> data.optLong("orderId")
                else -> response.optLong("orderId")
            }
            else -> response.optLong("orderId")
        }.takeIf { it > 0 } ?: error("Order response has no order ID.")
        CheckoutOrder(orderId, response.optString("message").ifBlank { "Order created." })
    }

    private fun request(path: String, method: String, token: String, payload: JSONObject? = null): JSONObject {
        val connection = URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("ngrok-skip-browser-warning", "1")
            connection.setRequestProperty("Authorization", "Bearer $token")
            if (payload != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.bufferedWriter().use { it.write(payload.toString()) }
            }
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            val response = runCatching { JSONObject(body) }.getOrDefault(JSONObject())
            if (status !in 200..299) {
                val message = response.optString("message").ifBlank { "Checkout request failed (HTTP $status)" }
                error(message)
            }
            return response
        } finally { connection.disconnect() }
    }
}

class ApiPaymentRepository(private val baseUrl: String = BuildConfig.API_BASE_URL) : PaymentRepository {
    override suspend fun initiateBuyerStkPush(
        orderId: Long,
        phoneNumber: String,
        accessToken: String?
    ): StkPushResult = withContext(Dispatchers.IO) {
        val token = accessToken?.takeIf(String::isNotBlank) ?: error("Sign in to start M-PESA payment.")
        val phone = normalizeKenyanPhone(phoneNumber)
        val data = request(
            "/api/v1/payments/stk-push/buyer",
            "POST",
            token,
            JSONObject().put("orderId", orderId).put("phoneNumber", phone)
        ).getJSONObject("data")
        StkPushResult(
            paymentId = data.optLong("paymentId").takeIf { it > 0 } ?: error("Payment response has no payment ID."),
            status = data.optString("status", "PENDING"),
            customerMessage = data.optJSONObject("darajaResponse")?.optString("customerMessage")?.takeIf(String::isNotBlank)
        )
    }

    override suspend fun getStatus(paymentId: Long, accessToken: String?): PaymentStatus = withContext(Dispatchers.IO) {
        val token = accessToken?.takeIf(String::isNotBlank) ?: error("Sign in to check payment status.")
        val data = request("/api/v1/payments/$paymentId/status", "GET", token).getJSONObject("data")
        PaymentStatus(
            status = data.optString("status").ifBlank { "UNKNOWN" },
            amount = data.optDouble("amount", 0.0),
            reference = data.optString("reference").takeIf(String::isNotBlank)
        )
    }

    private fun request(path: String, method: String, token: String, payload: JSONObject? = null): JSONObject {
        val connection = URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("ngrok-skip-browser-warning", "1")
            connection.setRequestProperty("Authorization", "Bearer $token")
            if (payload != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(payload.toString()) }
            }
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            val response = runCatching { JSONObject(body) }.getOrDefault(JSONObject())
            if (status !in 200..299) {
                error(response.optString("message").ifBlank { "Payment request failed (HTTP $status)." })
            }
            return response
        } finally { connection.disconnect() }
    }
}

private fun normalizeKenyanPhone(phoneNumber: String): String {
    val digits = phoneNumber.filter(Char::isDigit)
    val normalized = if (digits.startsWith("0")) "254${digits.drop(1)}" else digits
    require(normalized.matches(Regex("^254(?:7|1)\\d{8}$"))) { "Enter a valid Kenyan M-PESA number." }
    return normalized
}
