package com.example.kilivana_buyer.data

import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kilivana_buyer.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()
    val itemCount get() = _items.value.sumOf { it.quantity }
    val subtotal get() = _items.value.sumOf { it.quantity * it.product.pricePerUnit }
    private val preferences = application.getSharedPreferences("kilivana_guest_cart", Application.MODE_PRIVATE)
    private val checkoutRepository = ApiCheckoutRepository()
    private val paymentRepository = ApiPaymentRepository()
    private var cartRestored = false
    private var checkoutLocked = false
    val checkoutInProgress = MutableStateFlow(false)
    val addresses = MutableStateFlow<List<DeliveryAddress>>(emptyList())
    val addressesLoading = MutableStateFlow(false)
    val addressError = MutableStateFlow<String?>(null)
    val deliveryQuote = MutableStateFlow<DeliveryQuote?>(null)

    suspend fun loadAddresses(accessToken: String?) {
        addressesLoading.value = true
        addressError.value = null
        try { addresses.value = checkoutRepository.getAddresses(accessToken) }
        catch (error: Exception) { addressError.value = error.message ?: "Could not load saved addresses." }
        finally { addressesLoading.value = false }
    }

    suspend fun saveAddress(
        addressId: Long?, userId: String?, label: String, addressText: String,
        latitude: Double, longitude: Double, accessToken: String?
    ): String? {
        val ownerId = userId?.toLongOrNull() ?: return "Your profile is missing a user ID. Please sign in again."
        return try {
            val saved = checkoutRepository.saveAddress(addressId, ownerId, label, addressText, latitude, longitude, accessToken)
            addresses.value = addresses.value.filterNot { it.id == saved.id } + saved
            deliveryQuote.value = null
            null
        } catch (error: Exception) { error.message ?: "Could not save address." }
    }

    suspend fun deleteAddress(addressId: Long, accessToken: String?): String? {
        return try {
            checkoutRepository.deleteAddress(addressId, accessToken)
            addresses.value = addresses.value.filterNot { it.id == addressId }
            deliveryQuote.value = null
            null
        } catch (error: Exception) { error.message ?: "Could not delete address." }
    }

    suspend fun requestDeliveryQuote(addressId: Long, accessToken: String?): String? {
        checkoutInProgress.value = true
        deliveryQuote.value = null
        return try {
            if (!checkoutLocked) {
                checkoutRepository.checkout(_items.value, accessToken)
                checkoutLocked = true
            }
            deliveryQuote.value = checkoutRepository.requestDeliveryQuote(addressId, accessToken)
            null
        } catch (error: Exception) {
            error.message ?: "Could not calculate delivery. Please try again."
        } finally { checkoutInProgress.value = false }
    }

    fun clearDeliveryQuote() { deliveryQuote.value = null }

    fun restoreItems(products: List<Product>) {
        if (cartRestored || products.isEmpty()) return
        cartRestored = true
        val productsById = products.associateBy(Product::id)
        val restoredItems = preferences.getString("items", "").orEmpty()
            .split(',').mapNotNull { entry ->
                val pieces = entry.split(':')
                val product = pieces.getOrNull(0)?.let(productsById::get)
                val quantity = pieces.getOrNull(1)?.toIntOrNull()
                if (product != null && quantity != null && quantity >= product.minimumOrder) CartItem(product, quantity) else null
            }
        updateItems(restoredItems)
    }

    fun add(product: Product, quantity: Int = 1) {
        val current = _items.value
        val match = current.firstOrNull { it.product.id == product.id }
        updateItems(if (match == null) current + CartItem(product, quantity) else current.map {
            if (it.product.id == product.id) it.copy(quantity = it.quantity + quantity) else it
        })
    }

    fun increment(id: String) = updateItems(_items.value.map { if (it.product.id == id) it.copy(quantity = it.quantity + 1) else it })
    fun decrement(id: String) = updateItems(_items.value.map {
        if (it.product.id == id && it.quantity > it.product.minimumOrder) it.copy(quantity = it.quantity - 1) else it
    })
    fun remove(id: String) = updateItems(_items.value.filterNot { it.product.id == id })
    fun clear() {
        checkoutLocked = false
        deliveryQuote.value = null
        updateItems(emptyList())
    }

    suspend fun submitCheckout(accessToken: String?, quoteId: String, useMpesa: Boolean, phoneNumber: String): CheckoutSubmission {
        checkoutInProgress.value = true
        return try {
            val order = checkoutRepository.createOrder(quoteId, phoneNumber, accessToken)
            if (!useMpesa) CheckoutSubmission(orderId = order.id)
            else try {
                val payment = paymentRepository.initiateBuyerStkPush(order.id, phoneNumber, accessToken)
                CheckoutSubmission(order.id, payment.paymentId, payment.customerMessage)
            } catch (error: Exception) {
                CheckoutSubmission(orderId = order.id, error = error.message ?: "Could not start M-PESA payment.")
            }
        } catch (error: Exception) {
            CheckoutSubmission(error = error.message ?: "Checkout failed. Please try again.")
        } finally { checkoutInProgress.value = false }
    }

    suspend fun getPaymentStatus(paymentId: Long, accessToken: String?) =
        paymentRepository.getStatus(paymentId, accessToken)

    private fun updateItems(items: List<CartItem>) {
        _items.value = items
        preferences.edit().putString("items", items.joinToString(",") { "${it.product.id}:${it.quantity}" }).apply()
    }
}
class ProductsViewModel(private val repository: ProductRepository = ApiProductRepository()) : ViewModel() {
    val products = MutableStateFlow(emptyList<Product>())
    val categories = MutableStateFlow(emptyList<String>())
    val loading = MutableStateFlow(true)
    val loadError = MutableStateFlow<String?>(null)
    val category = MutableStateFlow<String?>(null)
    val savedProductIds = MutableStateFlow<Set<String>>(emptySet())
    init { refresh() }
    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            loadError.value = null
            try { products.value = repository.getProducts() }
            catch (error: Exception) { loadError.value = error.message ?: "Could not load products" }
            try { categories.value = repository.getCategories() }
            catch (_: Exception) { categories.value = products.value.map(Product::category).distinct() }
            finally { loading.value = false }
        }
    }
    fun toggleSaved(id: String) {
        savedProductIds.value = if (id in savedProductIds.value) savedProductIds.value - id else savedProductIds.value + id
    }
}
class OrdersViewModel(private val repository: OrderRepository = ApiOrderRepository()) : ViewModel() {
    private val _orders=MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()
    val loading = MutableStateFlow(false)
    val loadError = MutableStateFlow<String?>(null)

    fun loadForBuyer(buyerId: String, accessToken: String?, products: List<Product>) {
        viewModelScope.launch {
            loading.value = true
            loadError.value = null
            try { _orders.value = repository.getOrders(buyerId, accessToken, products) }
            catch (error: Exception) { loadError.value = error.message ?: "Could not load orders" }
            finally { loading.value = false }
        }
    }

    fun loadOrder(id: String, accessToken: String?, products: List<Product>) {
        viewModelScope.launch {
            loadError.value = null
            try {
                val order = repository.getOrder(id, accessToken, products)
                _selectedOrder.value = order.copy(timeline = repository.getTimeline(id, accessToken))
            } catch (error: Exception) { loadError.value = error.message ?: "Could not load order" }
        }
    }
}
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AuthRepository = ApiAuthRepository(application)
    private val _loggedIn=MutableStateFlow(repository.isLoggedIn())
    val loggedIn: StateFlow<Boolean> = _loggedIn.asStateFlow()
    val buyerId: String? get() = repository.currentBuyerId()
    val name: String? get() = repository.currentName()
    val email: String? get() = repository.currentEmail()
    val phone: String? get() = repository.currentPhone()
    val accessToken: String? get() = repository.accessToken()
    var pendingPassword: String = ""
    init { viewModelScope.launch { _loggedIn.value = repository.refreshSession() } }
    suspend fun signIn(email: String, password: String): String? {
        val error = repository.signIn(email, password)
        _loggedIn.value = repository.isLoggedIn()
        return error
    }
    suspend fun signInWithPassword(email: String, password: String): String? {
        val error = repository.signIn(email, password)
        _loggedIn.value = repository.isLoggedIn()
        return error
    }
    suspend fun register(name: String, email: String, phone: String, password: String, username: String, region: String): String? {
        return repository.register(name, email, phone, password, username, region)
    }
    suspend fun requestEmailVerification(email: String) = repository.requestEmailVerification(email)
    suspend fun verifyEmail(challengeId: String, email: String, code: String) =
        repository.verifyEmail(challengeId, email, code)
    suspend fun forgotPassword(email: String) = repository.forgotPassword(email)
    fun signOut() { _loggedIn.value = false; viewModelScope.launch { repository.signOut() } }
}
