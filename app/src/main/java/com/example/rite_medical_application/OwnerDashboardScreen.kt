package com.example.rite_medical_application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

// Updated data class to match Google Sheets structure
data class LowStockItem(
    val alertDate: String,
    val sku: String,
    val productName: String,
    val currentStock: Int,
    val lowStockThreshold: Int,
    val status: String
)

// Service to fetch low stock alerts from Google Sheets
class LowStockService {
    companion object {
        // Replace with your Google Sheets CSV export URL
        // Go to your Google Sheets -> File -> Share -> Publish to web -> Link tab -> Select "Low_Stock_Alerts" sheet -> CSV format
        private const val GOOGLE_SHEETS_CSV_URL = "https://docs.google.com/spreadsheets/d/YOUR_SHEET_ID/export?format=csv&gid=YOUR_SHEET_GID"
    }
    
    suspend fun fetchLowStockItems(): List<LowStockItem> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(GOOGLE_SHEETS_CSV_URL)
                val connection = url.openConnection()
                val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
                
                val items = mutableListOf<LowStockItem>()
                var isFirstLine = true
                
                reader.forEachLine { line ->
                    if (isFirstLine) {
                        isFirstLine = false
                        return@forEachLine // Skip header row
                    }
                    
                    val columns = line.split(",")
                    if (columns.size >= 6) {
                        try {
                            val item = LowStockItem(
                                alertDate = columns[0].trim('"'),
                                sku = columns[1].trim('"'),
                                productName = columns[2].trim('"'),
                                currentStock = columns[3].trim('"').toIntOrNull() ?: 0,
                                lowStockThreshold = columns[4].trim('"').toIntOrNull() ?: 0,
                                status = columns[5].trim('"')
                            )
                            // Only include active alerts
                            if (item.status.equals("ACTIVE", ignoreCase = true)) {
                                items.add(item)
                            }
                        } catch (e: Exception) {
                            // Skip invalid rows
                        }
                    }
                }
                
                reader.close()
                items
            } catch (e: Exception) {
                // Return sample data if API call fails
                listOf(
                    LowStockItem("2024-01-01", "P101", "Paris-G Biscuits", 8, 10, "ACTIVE")
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    navController: NavController
) {
    // Sample data for the owner dashboard
    val todaysOrders = 15
    val pendingShipments = 8
    val todaysRevenue = 1247.50
    
    // State for low stock items
    var lowStockItems by remember { mutableStateOf<List<LowStockItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Fetch low stock items when screen loads
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = null
            val service = LowStockService()
            lowStockItems = service.fetchLowStockItems()
        } catch (e: Exception) {
            errorMessage = "Failed to load low stock alerts: ${e.message}"
            // Use sample data as fallback
            lowStockItems = listOf(
                LowStockItem("2024-01-01", "P101", "Paris-G Biscuits", 8, 10, "ACTIVE")
    )
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Owner Dashboard",
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Today's Orders Card
            MetricCard(
                title = "Today's Orders",
                value = todaysOrders.toString(),
                icon = Icons.Filled.ShoppingCart,
                backgroundColor = Color(0xFF4CAF50),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Row for Pending Shipments and Revenue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    title = "Pending Shipments",
                    value = pendingShipments.toString(),
                    icon = Icons.Filled.LocalShipping,
                    backgroundColor = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Revenue Today",
                    value = "$${String.format("%.0f", todaysRevenue)}",
                    icon = Icons.Filled.TrendingUp,
                    backgroundColor = Color(0xFF009688),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Update Inventory Button
            Button(
                onClick = {
                    // TODO: Implement Excel upload functionality
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Upload,
                    contentDescription = "Upload",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "Update Inventory (Upload Excel)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Low Stock Items Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Section Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Warning",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "Low Stock Items",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Loading state
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        // Error message
                        errorMessage?.let { message ->
                            Text(
                                text = message,
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    
                    // Low Stock Items List
                        if (lowStockItems.isNotEmpty()) {
                    lowStockItems.forEach { item ->
                        LowStockItemCard(item = item)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                        } else {
                        Text(
                            text = "All items are well stocked! ✅",
                            fontSize = 16.sp,
                            color = Color(0xFF388E3C),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LowStockItemCard(item: LowStockItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "SKU: ${item.sku}",
                    fontSize = 11.sp,
                    color = Color(0xFF999999)
                )
                Text(
                    text = "Current: ${item.currentStock} | Threshold: ${item.lowStockThreshold}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
                if (item.alertDate.isNotEmpty()) {
                    Text(
                        text = "Alert: ${item.alertDate.take(10)}", // Show only date part
                        fontSize = 10.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFFFCDD2),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "LOW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OwnerDashboardScreenPreview() {
    OwnerDashboardScreen(navController = rememberNavController())
} 