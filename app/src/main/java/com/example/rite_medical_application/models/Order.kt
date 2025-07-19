package com.example.rite_medical_application.models

data class OrderItem(
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String = ""
)

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "pending",
    val paymentMethod: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryAddress: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = ""
) 