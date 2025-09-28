package com.example.rite_medical_application

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class Coupon(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val discount: Double,
    val discountType: DiscountType,
    val minOrderAmount: Double = 0.0,
    val maxDiscount: Double = Double.MAX_VALUE,
    val isActive: Boolean = true
)

enum class DiscountType {
    PERCENTAGE, FIXED_AMOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponScreen(
    navController: NavController,
    cartTotal: Double = 0.0,
    onCouponApplied: (Coupon?) -> Unit = {}
) {
    // Sample coupons
    val availableCoupons = remember {
        listOf(
            Coupon(
                id = "1",
                code = "SAVE10",
                title = "Save 10%",
                description = "Get 10% off on orders above $20",
                discount = 10.0,
                discountType = DiscountType.PERCENTAGE,
                minOrderAmount = 20.0,
                maxDiscount = 50.0
            ),
            Coupon(
                id = "2",
                code = "FIRST5",
                title = "First Order Discount",
                description = "Get $5 off on your first order",
                discount = 5.0,
                discountType = DiscountType.FIXED_AMOUNT,
                minOrderAmount = 15.0
            ),
            Coupon(
                id = "3",
                code = "WELCOME15",
                title = "Welcome Offer",
                description = "Get 15% off on orders above $30",
                discount = 15.0,
                discountType = DiscountType.PERCENTAGE,
                minOrderAmount = 30.0,
                maxDiscount = 75.0
            ),
            Coupon(
                id = "4",
                code = "FREESHIP",
                title = "Free Shipping",
                description = "Free shipping on all orders",
                discount = 2.99,
                discountType = DiscountType.FIXED_AMOUNT,
                minOrderAmount = 0.0
            )
        )
    }
    
    var couponCode by remember { mutableStateOf("") }
    var selectedCoupon by remember { mutableStateOf<Coupon?>(null) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    fun applyCouponCode() {
        val coupon = availableCoupons.find { it.code.equals(couponCode.trim(), ignoreCase = true) }
        if (coupon != null) {
            if (cartTotal >= coupon.minOrderAmount) {
                selectedCoupon = coupon
                showError = false
            } else {
                showError = true
                errorMessage = "Minimum order amount is $${String.format("%.2f", coupon.minOrderAmount)}"
            }
        } else {
            showError = true
            errorMessage = "Invalid coupon code"
        }
    }
    
    fun calculateDiscount(coupon: Coupon): Double {
        return when (coupon.discountType) {
            DiscountType.PERCENTAGE -> {
                val discount = (cartTotal * coupon.discount) / 100
                minOf(discount, coupon.maxDiscount)
            }
            DiscountType.FIXED_AMOUNT -> coupon.discount
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply Coupon", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Coupon code input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Enter Coupon Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = couponCode,
                            onValueChange = { 
                                couponCode = it.uppercase()
                                showError = false
                            },
                            label = { Text("Coupon Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            isError = showError
                        )
                        Button(
                            onClick = { applyCouponCode() },
                            enabled = couponCode.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF007AFF)
                            )
                        ) {
                            Text("Apply")
                        }
                    }
                    
                    if (showError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Available coupons
            Text(
                text = "Available Coupons",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            availableCoupons.forEach { coupon ->
                CouponCard(
                    coupon = coupon,
                    cartTotal = cartTotal,
                    isSelected = selectedCoupon?.id == coupon.id,
                    onSelect = { 
                        if (cartTotal >= coupon.minOrderAmount) {
                            selectedCoupon = if (selectedCoupon?.id == coupon.id) null else coupon
                            showError = false
                        } else {
                            showError = true
                            errorMessage = "Minimum order amount is $${String.format("%.2f", coupon.minOrderAmount)}"
                        }
                    },
                    calculateDiscount = ::calculateDiscount
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Apply button
            Button(
                onClick = {
                    onCouponApplied(selectedCoupon)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = if (selectedCoupon != null) "Apply Coupon" else "Continue Without Coupon",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CouponCard(
    coupon: Coupon,
    cartTotal: Double,
    isSelected: Boolean,
    onSelect: () -> Unit,
    calculateDiscount: (Coupon) -> Double
) {
    val isEligible = cartTotal >= coupon.minOrderAmount
    val discount = if (isEligible) calculateDiscount(coupon) else 0.0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isEligible) onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> Color(0xFFE3F2FD)
                isEligible -> Color.White
                else -> Color(0xFFF5F5F5)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coupon icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isEligible) Color(0xFF007AFF) else Color.Gray,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = "Coupon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Coupon details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = coupon.code,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isEligible) Color.Black else Color.Gray
                )
                Text(
                    text = coupon.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isEligible) Color(0xFF007AFF) else Color.Gray
                )
                Text(
                    text = coupon.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                if (isEligible && discount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You save: $${String.format("%.2f", discount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                } else if (!isEligible) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Min order: $${String.format("%.2f", coupon.minOrderAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFF007AFF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CouponScreenPreview() {
    CouponScreen(
        navController = rememberNavController(),
        cartTotal = 25.0
    )
}
