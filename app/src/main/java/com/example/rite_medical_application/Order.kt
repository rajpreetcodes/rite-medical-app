package com.example.rite_medical_application

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Order(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("customerEmail") val customerEmail: String,
    @SerializedName("customerPhone") val customerPhone: String,
    @SerializedName("orderDate") val orderDate: Date,
    @SerializedName("orderStatus") val orderStatus: String,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("shippingAddress") val shippingAddress: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("items") val items: List<OrderItem>
)

data class OrderItem(
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: Double
) 