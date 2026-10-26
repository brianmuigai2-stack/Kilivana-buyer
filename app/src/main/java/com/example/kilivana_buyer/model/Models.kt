package com.example.kilivana_buyer.model

data class Product(
    val id: String, val name: String, val category: String, val pricePerUnit: Int,
    val unit: String, val description: String, val sellerName: String?,
    val sellerLocation: String?, val stockStatus: StockStatus, val minimumOrder: Int,
    val harvestNote: String? = null,
    val imageUrl: String? = null
)
enum class StockStatus { InStock, LowStock, OutOfStock }
data class CartItem(val product: Product, val quantity: Int)
enum class OrderStatus { Pending, Confirmed, OutForDelivery, Delivered, Cancelled }
data class Order(
    val id: String, val date: String, val status: OrderStatus, val items: List<CartItem>,
    val subtotal: Int, val deliveryFee: Int, val total: Int, val deliveryAddress: String,
    val code: String = id, val timeline: List<OrderEvent> = emptyList()
)
data class OrderEvent(val status: String, val note: String?, val createdAt: String)
fun formatKes(amount: Int): String = "KES " + String.format(java.util.Locale.US, "%,d", amount)
