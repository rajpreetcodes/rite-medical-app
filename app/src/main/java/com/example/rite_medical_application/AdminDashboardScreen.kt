package com.example.rite_medical_application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class AdminOrder(
    val orderId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val totalAmount: Double = 0.0,
    val status: String = "",
    val timestamp: Long = 0L,
    val deliveryAddress: String = "",
    val items: List<AdminOrderItem> = emptyList()
)

data class AdminOrderItem(
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogoutClick: () -> Unit = {}
) {
    var orders by remember { mutableStateOf<List<AdminOrder>>(emptyList()) }
    var lowStockProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showStockManagement by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    val firestore = FirebaseFirestore.getInstance()
    
    // Load orders and low stock products
    LaunchedEffect(refreshTrigger) {
        try {
            // Load orders from Firestore
            val ordersSnapshot = firestore.collection("orders").get().await()
            val ordersList = ordersSnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val items = (data["items"] as? List<Map<String, Any>>)?.map { itemData ->
                        AdminOrderItem(
                            productName = itemData["productName"] as? String ?: "",
                            quantity = (itemData["quantity"] as? Long)?.toInt() ?: 0,
                            price = (itemData["price"] as? Double) ?: 0.0
                        )
                    } ?: emptyList()
                    
                    AdminOrder(
                        orderId = data["orderId"] as? String ?: "",
                        customerName = data["customerName"] as? String ?: "",
                        customerEmail = data["customerEmail"] as? String ?: "",
                        customerPhone = data["customerPhone"] as? String ?: "",
                        totalAmount = data["totalAmount"] as? Double ?: 0.0,
                        status = data["status"] as? String ?: "",
                        timestamp = data["timestamp"] as? Long ?: 0L,
                        deliveryAddress = data["deliveryAddress"] as? String ?: "",
                        items = items
                    )
                } catch (e: Exception) {
                    null
                }
            }
            orders = ordersList.sortedByDescending { it.timestamp }
            
            // Get low stock products based on individual thresholds
            lowStockProducts = ProductRepository.products.filter { it.stock > 0 && it.stock < it.lowStockThreshold }
            
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Admin Dashboard", 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                actions = {
                    IconButton(onClick = { showStockManagement = !showStockManagement }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Stock Management",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D4A)
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2E7D4A))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Statistics Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Orders",
                        value = orders.size.toString(),
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFF007AFF),
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Low Stock Items",
                        value = lowStockProducts.size.toString(),
                        icon = Icons.Default.Warning,
                        color = Color(0xFFFF9500),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Stock Management Section
                if (showStockManagement) {
                    IndividualStockManagementSection(
                        onThresholdUpdate = {
                            refreshTrigger++
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // Low Stock Products Section
                if (lowStockProducts.isNotEmpty()) {
                    Text(
                        text = "Low Stock Alert",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3B30),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            lowStockProducts.forEach { product ->
                                LowStockItem(product = product)
                                if (product != lowStockProducts.last()) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = Color(0xFFE5E5E5)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // Recent Orders Section
                Text(
                    text = "Recent Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                if (orders.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No orders placed yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    orders.take(10).forEach { order ->
                        OrderCard(order = order)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LowStockItem(product: Product) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = "Stock: ${product.stock} units",
                fontSize = 12.sp,
                color = Color(0xFFFF3B30)
            )
        }
        
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Low Stock",
            tint = Color(0xFFFF9500),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun OrderCard(order: AdminOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order #${order.orderId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = order.customerName,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = formatTimestamp(order.timestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", order.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF007AFF)
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (order.status.uppercase()) {
                                "CONFIRMED" -> Color(0xFF34C759)
                                "PENDING" -> Color(0xFFFF9500)
                                "CANCELLED" -> Color(0xFFFF3B30)
                                else -> Color.Gray
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = order.status.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Customer Details
            Text(
                text = "Customer Details:",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = "Email: ${order.customerEmail}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            if (order.customerPhone.isNotEmpty()) {
                Text(
                    text = "Phone: ${order.customerPhone}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                text = "Address: ${order.deliveryAddress}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            if (order.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Items:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.productName} x${item.quantity}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$${String.format("%.2f", item.price * item.quantity)}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}

@Composable
fun IndividualStockManagementSection(
    onThresholdUpdate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Individual Stock Thresholds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D4A)
                )
                
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF2E7D4A),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Set custom low stock alerts for each product based on sales rate",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Individual Product Threshold Settings
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ProductRepository.products) { product ->
                    IndividualProductThresholdItem(
                        product = product,
                        onThresholdUpdate = onThresholdUpdate
                    )
                }
            }
        }
    }
}

@Composable
fun IndividualProductThresholdItem(
    product: Product,
    onThresholdUpdate: () -> Unit
) {
    var thresholdInput by remember { mutableStateOf(product.lowStockThreshold.toString()) }
    var isEditing by remember { mutableStateOf(false) }
    
    val stockStatus = when {
        product.stock == 0 -> "Out of Stock"
        product.stock < product.lowStockThreshold -> "Low Stock"
        else -> "In Stock"
    }
    
    val statusColor = when {
        product.stock == 0 -> Color(0xFFFF3B30)
        product.stock < product.lowStockThreshold -> Color(0xFFFF9500)
        else -> Color(0xFF34C759)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (product.stock < product.lowStockThreshold && product.stock > 0) 
                Color(0xFFFFF3E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = "Current Stock: ${product.stock} units",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stockStatus,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Alert when below:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                if (isEditing) {
                    OutlinedTextField(
                        value = thresholdInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                thresholdInput = newValue
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(80.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    
                    Button(
                        onClick = {
                            val newThreshold = thresholdInput.toIntOrNull()
                            if (newThreshold != null && newThreshold > 0) {
                                ProductRepository.updateProductThreshold(product.id ?: "", newThreshold)
                                isEditing = false
                                onThresholdUpdate()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF34C759)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Save", fontSize = 10.sp, color = Color.White)
                    }
                    
                    Button(
                        onClick = {
                            thresholdInput = product.lowStockThreshold.toString()
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Cancel", fontSize = 10.sp, color = Color.White)
                    }
                } else {
                    Text(
                        text = "${product.lowStockThreshold} units",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D4A)
                    )
                    
                    Button(
                        onClick = { isEditing = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Edit", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    AdminDashboardScreen()
}
