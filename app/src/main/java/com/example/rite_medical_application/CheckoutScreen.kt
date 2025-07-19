package com.example.rite_medical_application

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    paymentViewModel: PaymentViewModel
) {
    // Observe cart items
    val cartItems by cartViewModel.cartItems.collectAsState()
    
    // Observe order state
    val orderState by cartViewModel.orderState.collectAsState()
    val lastOrderId by cartViewModel.lastOrderId.collectAsState()

    // Observe selected payment method
    val selectedPaymentMethod by paymentViewModel.selectedPaymentMethod.collectAsState()
    
    // Check if user is authenticated
    val auth = FirebaseAuth.getInstance()
    val isAuthenticated = remember { auth.currentUser != null }
    
    // Show login dialog if not authenticated
    var showLoginDialog by remember { mutableStateOf(false) }
    
    // Handle order state changes
    LaunchedEffect(orderState) {
        when (orderState) {
            is OrderState.Success -> {
                // Navigate to order confirmation screen
                navController.navigate("order_confirmation/${lastOrderId}") {
                    popUpTo("checkout") { inclusive = true }
                }
                cartViewModel.resetOrderState()
            }
            is OrderState.Error -> {
                if ((orderState as OrderState.Error).message == "User not authenticated") {
                    showLoginDialog = true
                }
            }
            else -> { /* Handle other states if needed */ }
        }
    }

    // Simple calculations – can be moved to ViewModel later
    val deliveryFee = 2.99
    val discount = 0.0
    val itemTotal = cartItems.sumOf { it.product.price * it.quantity }
    val originalTotal = itemTotal + deliveryFee
    val finalTotal = originalTotal - discount

    // Dummy recommendation products
    val recommendedProducts = remember {
        listOf(
            Product(id = "R001", name = "Hand Sanitizer", price = 8.99, imageUrl = "https://images.unsplash.com/photo-1584362917165-526a968579e8?w=400", stock = 50),
            Product(id = "R002", name = "Face Masks (Pack of 10)", price = 12.99, imageUrl = "https://images.unsplash.com/photo-1603791440384-56cd371ee9a7?w=400", stock = 30),
            Product(id = "R003", name = "Vitamin D Tablets", price = 15.99, imageUrl = "https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=400", stock = 25),
            Product(id = "R004", name = "First Aid Kit", price = 24.99, imageUrl = "https://images.unsplash.com/photo-1603503364272-9ba9bbf5a1c1?w=400", stock = 15)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Sticky Bottom Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side - Payment & Total
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate("payment_selection") }
                    ) {
                        Text(
                            text = selectedPaymentMethod.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = selectedPaymentMethod.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "$${String.format("%.2f", finalTotal)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF007AFF)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Right Side - Place Order Button
                    Button(
                        onClick = {
                            if (cartItems.isNotEmpty()) {
                                if (isAuthenticated) {
                                    cartViewModel.placeOrder(selectedPaymentMethod.name)
                                } else {
                                    showLoginDialog = true
                                }
                            }
                        },
                        enabled = cartItems.isNotEmpty() && orderState !is OrderState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (cartItems.isNotEmpty() && orderState !is OrderState.Loading) Color(0xFF007AFF) else Color.Gray
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        if (orderState is OrderState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Place Order",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Place Order",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* 1️⃣ Delivery Details */
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Address Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFF007AFF)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Delivery at Home",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "123 Main Street, Apt 4B, New York, NY 10001",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Change Address",
                                tint = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        // ETA Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "ETA",
                                tint = Color(0xFF007AFF)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Delivery in 25-30 mins",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            /* 2️⃣ Cart Items */
            if (cartItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Your cart is empty", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            Text("Add some products to get started!", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(cartItems) { cartItem ->
                    CartItemCard(cartItem = cartItem, cartViewModel = cartViewModel)
                }
            }

            /* 3️⃣ Frequently Bought Together */
            if (cartItems.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Frequently Bought Together",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(recommendedProducts) { product ->
                                RecommendationCard(
                                    product = product,
                                    onAddToCart = { cartViewModel.addToCart(product) }
                                )
                            }
                        }
                    }
                }
            }

            /* 4️⃣ Coupon Section */
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = "Coupon",
                            tint = Color(0xFF007AFF)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Apply Coupon",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Apply Coupon",
                            tint = Color.Gray
                        )
                    }
                }
            }

            /* 5️⃣ Bill Details */
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bill Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        // Item Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Total")
                            Text("$${String.format("%.2f", itemTotal)}")
                        }
                        Spacer(Modifier.height(8.dp))
                        // Delivery Fee
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Fee")
                            Text("$${String.format("%.2f", deliveryFee)}")
                        }
                        Spacer(Modifier.height(8.dp))
                        // Discount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", color = Color(0xFF4CAF50))
                            Text("-$${String.format("%.2f", discount)}", color = Color(0xFF4CAF50))
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        // Total Amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Column(horizontalAlignment = Alignment.End) {
                                if (discount > 0) {
                                    Text(
                                        text = "$${String.format("%.2f", originalTotal)}",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                                Text(
                                    text = "$${String.format("%.2f", finalTotal)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF)
                                )
                            }
                        }
                    }
                }
            }

            /* 6️⃣ Bottom Spacing for Sticky Bar */
            item {
                Spacer(Modifier.height(80.dp)) // Extra space for bottom bar
            }
        }
        
        // Error Dialog for order placement failures
        val errorState = orderState as? OrderState.Error
        errorState?.let { err ->
            if (err.message != "User not authenticated") {
                AlertDialog(
                    onDismissRequest = { cartViewModel.resetOrderState() },
                    title = { Text("Order Error") },
                    text = { Text(err.message) },
                    confirmButton = {
                        TextButton(onClick = { cartViewModel.resetOrderState() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
        
        // Login Dialog
        if (showLoginDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showLoginDialog = false
                    cartViewModel.resetOrderState()
                },
                title = { Text("Authentication Required") },
                text = { Text("You need to log in to place an order. Would you like to log in now?") },
                confirmButton = {
                    TextButton(onClick = { 
                        showLoginDialog = false
                        cartViewModel.resetOrderState()
                        navController.navigate("welcome")
                    }) {
                        Text("Log In")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showLoginDialog = false
                        cartViewModel.resetOrderState()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun CartItemCard(cartItem: CartItem, cartViewModel: CartViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image placeholder
            AsyncImage(
                model = cartItem.product.imageUrl,
                contentDescription = cartItem.product.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Product details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cartItem.product.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                
                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { cartViewModel.decreaseQuantity(cartItem) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove, 
                            contentDescription = "Decrease Quantity",
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "${cartItem.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = { cartViewModel.increaseQuantity(cartItem) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "Increase Quantity",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            // Price
            Text(
                text = "$${String.format("%.2f", cartItem.product.price * cartItem.quantity)}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF007AFF),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun RecommendationCard(
    product: Product,
    onAddToCart: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.width(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product Image
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.height(32.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Price
            Text(
                text = "$${String.format("%.2f", product.price)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF007AFF)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Add Button
            Button(
                onClick = onAddToCart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add to Cart",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    CheckoutScreen(
        navController = rememberNavController(), 
        cartViewModel = viewModel(),
        paymentViewModel = viewModel()
    )
} 