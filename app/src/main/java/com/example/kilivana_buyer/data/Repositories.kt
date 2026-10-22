package com.example.kilivana_buyer.data

import com.example.kilivana_buyer.model.Order

interface ProductRepository { fun getProducts(): List<com.example.kilivana_buyer.model.Product> }
interface OrderRepository { fun getOrders(): List<Order>; fun addOrder(order: Order) }
interface AuthRepository { fun isLoggedIn(): Boolean; fun signIn(email: String, password: String): Boolean; fun signOut() }
class FakeProductRepository : ProductRepository { override fun getProducts() = FakeData.products }
class FakeOrderRepository : OrderRepository {
    private val orders = mutableListOf<Order>()
    override fun getOrders() = orders.toList()
    override fun addOrder(order: Order) { orders.add(0, order) }
}
class FakeAuthRepository : AuthRepository {
    private var loggedIn = false
    override fun isLoggedIn() = loggedIn
    override fun signIn(email: String, password: String): Boolean = false
    override fun signOut() { loggedIn = false }
}
