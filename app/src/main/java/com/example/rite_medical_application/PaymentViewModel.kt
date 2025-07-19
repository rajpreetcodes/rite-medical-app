package com.example.rite_medical_application

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Data class for payment methods
data class PaymentMethod(
    val id: String,
    val name: String,
    val details: String,
    val icon: ImageVector
)

class PaymentViewModel : ViewModel() {
    
    // Available payment methods
    private val _availablePaymentMethods = MutableStateFlow(
        listOf(
            PaymentMethod(
                id = "mastercard",
                name = "Mastercard",
                details = "**** **** **** 1234",
                icon = Icons.Default.CreditCard
            ),
            PaymentMethod(
                id = "googlepay",
                name = "Google Pay",
                details = "user@gmail.com",
                icon = Icons.Default.Wallet
            ),
            PaymentMethod(
                id = "paytm",
                name = "Paytm",
                details = "+1 234-567-8900",
                icon = Icons.Default.Payment
            ),
            PaymentMethod(
                id = "upi",
                name = "UPI",
                details = "user@bankname",
                icon = Icons.Default.AccountBalance
            ),
            PaymentMethod(
                id = "cod",
                name = "Cash on Delivery",
                details = "Pay when you receive",
                icon = Icons.Default.LocalAtm
            )
        )
    )
    val availablePaymentMethods: StateFlow<List<PaymentMethod>> = _availablePaymentMethods.asStateFlow()
    
    // Currently selected payment method - default to first method (Mastercard)
    private val _selectedPaymentMethod = MutableStateFlow(_availablePaymentMethods.value.first())
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()
    
    // Function to select a payment method
    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }
    
    // Helper function to get payment method by ID
    fun getPaymentMethodById(id: String): PaymentMethod? {
        return _availablePaymentMethods.value.find { it.id == id }
    }
} 