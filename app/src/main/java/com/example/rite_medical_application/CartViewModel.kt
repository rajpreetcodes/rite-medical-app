package com.example.rite_medical_application

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Date

data class CartItem(val product: Product, val quantity: Int)

sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    object Success : OrderState()
    data class Error(val message: String) : OrderState()
}

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()
    
    private val _lastOrderId = MutableStateFlow<String?>(null)
    val lastOrderId: StateFlow<String?> = _lastOrderId.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val orderRepository = OrderRepository.getInstance()

    fun addToCart(product: Product) {
        //Logic to add product or update quantity if it already exists
        val currentList = _cartItems.value.toMutableList()
        val existingItem = currentList.find { it.product.name == product.name }

        if (existingItem != null) {
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            val itemIndex = currentList.indexOf(existingItem)
            currentList[itemIndex] = updatedItem
        } else {
            currentList.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = currentList
    }

    fun removeFromCart(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        val existingItem = currentList.find { it.product.name == product.name }

        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                val updatedItem = existingItem.copy(quantity = existingItem.quantity - 1)
                val itemIndex = currentList.indexOf(existingItem)
                currentList[itemIndex] = updatedItem
            } else {
                currentList.remove(existingItem)
            }
            _cartItems.value = currentList
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getTotalPrice(): Double {
        return _cartItems.value.sumOf { it.product.price * it.quantity }
    }

    fun getTotalItems(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    fun increaseQuantity(item: CartItem) {
        val currentList = _cartItems.value.toMutableList()
        val existingItem = currentList.find { it.product.name == item.product.name }
        if (existingItem != null) {
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1) //Increase quantity
            val itemIndex = currentList.indexOf(existingItem)
            currentList[itemIndex] = updatedItem
            _cartItems.value = currentList
        }
    }

    fun decreaseQuantity(item: CartItem) {
        val currentList = _cartItems.value.toMutableList()
        val existingItem = currentList.find { it.product.name == item.product.name }

        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                val updatedItem = existingItem.copy(quantity = existingItem.quantity - 1) //Decrease quantity
                val itemIndex = currentList.indexOf(existingItem)
                currentList[itemIndex] = updatedItem
            } else {
                currentList.remove(existingItem) //Remove item if quantity is 1
            }
            _cartItems.value = currentList
        }
    }
    
    fun placeOrder(paymentMethod: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _orderState.value = OrderState.Error("User not authenticated")
            return
        }
        
        if (_cartItems.value.isEmpty()) {
            _orderState.value = OrderState.Error("Cart is empty")
            return
        }
        
        viewModelScope.launch {
            try {
                _orderState.value = OrderState.Loading
                
                // Generate unique order ID
                val orderId = "ORDER_${UUID.randomUUID().toString().uppercase().take(8)}"
                
                // Convert cart items to order items
                val orderItems = _cartItems.value.map { cartItem ->
                    OrderItem(
                        productId = cartItem.product.id ?: "",
                        productName = cartItem.product.name,
                        quantity = cartItem.quantity,
                        price = cartItem.product.price
                    )
                }
                
                // Create order object for Firestore
                val firestoreOrder = hashMapOf(
                    "orderId" to orderId,
                    "userId" to currentUser.uid,
                    "items" to orderItems.map { item ->
                        hashMapOf(
                            "productId" to item.productId,
                            "productName" to item.productName,
                            "quantity" to item.quantity,
                            "price" to item.price
                        )
                    },
                    "totalAmount" to getTotalPrice(),
                    "status" to "CONFIRMED",
                    "paymentMethod" to paymentMethod,
                    "timestamp" to System.currentTimeMillis(),
                    "deliveryAddress" to "Default Address", // TODO: Get from user profile
                    "customerName" to (currentUser.displayName ?: "Customer"),
                    "customerPhone" to (currentUser.phoneNumber ?: ""),
                    "customerEmail" to (currentUser.email ?: "")
                )
                
                // Create order object for n8n webhook
                val webhookOrder = Order(
                    orderId = orderId,
                    userId = currentUser.uid,
                    customerName = currentUser.displayName ?: "Customer",
                    customerEmail = currentUser.email ?: "",
                    customerPhone = currentUser.phoneNumber ?: "",
                    orderDate = Date(),
                    orderStatus = "CONFIRMED",
                    totalAmount = getTotalPrice(),
                    shippingAddress = "Default Address", // TODO: Get from user profile
                    paymentMethod = paymentMethod,
                    items = orderItems
                )
                
                // Save order to Firestore
                firestore.collection("orders")
                    .document(orderId)
                    .set(firestoreOrder)
                    .addOnSuccessListener {
                        Log.d("CartViewModel", "Order placed successfully: $orderId")
                        
                        // Send order notification to n8n webhook
                        viewModelScope.launch {
                            try {
                                val notificationSent = orderRepository.sendOrderNotification(listOf(webhookOrder))
                                if (notificationSent) {
                                    Log.d("CartViewModel", "Order notification sent successfully")
                                } else {
                                    Log.e("CartViewModel", "Failed to send order notification")
                                }
                                
                                val orderProcessed = orderRepository.processOrder(listOf(webhookOrder))
                                if (orderProcessed) {
                                    Log.d("CartViewModel", "Order processing triggered successfully")
                                } else {
                                    Log.e("CartViewModel", "Failed to trigger order processing")
                                }
                                
                                val inventoryUpdated = orderRepository.updateInventory(listOf(webhookOrder))
                                if (inventoryUpdated) {
                                    Log.d("CartViewModel", "Inventory update triggered successfully")
                                } else {
                                    Log.e("CartViewModel", "Failed to trigger inventory update")
                                }
                            } catch (e: Exception) {
                                Log.e("CartViewModel", "Error sending webhook notifications", e)
                            }
                        }
                        
                        _orderState.value = OrderState.Success
                        _lastOrderId.value = orderId
                        clearCart() // Clear cart after successful order
                    }
                    .addOnFailureListener { exception ->
                        Log.e("CartViewModel", "Error placing order", exception)
                        _orderState.value = OrderState.Error(
                            exception.message ?: "Failed to place order"
                        )
                    }
                    
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error creating order", e)
                _orderState.value = OrderState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun resetOrderState() {
        _orderState.value = OrderState.Idle
    }
} 