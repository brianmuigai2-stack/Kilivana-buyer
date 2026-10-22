package com.example.kilivana_buyer.data

import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kilivana_buyer.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()
    val itemCount get() = _items.value.sumOf { it.quantity }
    val subtotal get() = _items.value.sumOf { it.quantity * it.product.pricePerUnit }
    private val preferences = application.getSharedPreferences("kilivana_guest_cart", Application.MODE_PRIVATE)

    init {
        val productsById = FakeData.products.associateBy(Product::id)
        _items.value = preferences.getString("items", "").orEmpty()
            .split(',').mapNotNull { entry ->
                val pieces = entry.split(':')
                val product = pieces.getOrNull(0)?.let(productsById::get)
                val quantity = pieces.getOrNull(1)?.toIntOrNull()
                if (product != null && quantity != null && quantity >= product.minimumOrder) CartItem(product, quantity) else null
            }
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
    fun clear() = updateItems(emptyList())

    private fun updateItems(items: List<CartItem>) {
        _items.value = items
        preferences.edit().putString("items", items.joinToString(",") { "${it.product.id}:${it.quantity}" }).apply()
    }
}
class ProductsViewModel(private val repository: ProductRepository = FakeProductRepository()) : ViewModel() {
    val products = MutableStateFlow(repository.getProducts())
    val category = MutableStateFlow<String?>(null)
}
class OrdersViewModel(private val repository: OrderRepository = FakeOrderRepository()) : ViewModel() {
    private val _orders=MutableStateFlow(repository.getOrders())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    fun place(order:Order) { repository.addOrder(order); _orders.value=repository.getOrders() }
}
class AuthViewModel(private val repository: AuthRepository = FakeAuthRepository()) : ViewModel() {
    private val _loggedIn=MutableStateFlow(repository.isLoggedIn())
    val loggedIn: StateFlow<Boolean> = _loggedIn.asStateFlow()
    fun signIn(email: String, password: String): Boolean {
        val succeeded = repository.signIn(email, password)
        if (succeeded) _loggedIn.value = repository.isLoggedIn()
        return succeeded
    }
    fun signOut() { repository.signOut(); _loggedIn.value=false }
}
