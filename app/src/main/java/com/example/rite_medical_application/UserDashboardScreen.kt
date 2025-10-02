package com.example.rite_medical_application

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import androidx.navigation.NavController
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.navigation.compose.rememberNavController
import com.example.rite_medical_application.ProductRepository
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

data class Product(
    val id: String? = null,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val stock: Int,
    val lowStockThreshold: Int = 10 // Default threshold, can be customized per product
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    // Categories with "All" option
    val categories = listOf(
        "All",
        "Pain Relief",
        "Vitamins",
        "First Aid",
        "Diabetes Care",
        "Personal Care",
        "Baby Care",
        "Health Devices",
        "Ayurvedic"
    )
    
    // Products from repository
    val products = ProductRepository.products
    
    // Promotional banners
    val promoImages = listOf(
        "https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=800&h=300",
        "https://images.unsplash.com/photo-1584362917165-526a968579e8?w=800&h=300",
        "https://images.unsplash.com/photo-1603791440384-56cd371ee9a7?w=800&h=300"
    )
    
    // Order again products (frequently bought items)
    val orderAgainProducts = remember {
        listOf(
            Product(id = "P001", name = "Paracetamol 500mg", price = 5.99, imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400", stock = 100),
            Product(id = "P011", name = "Vitamin C", price = 12.99, imageUrl = "https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=400", stock = 50),
            Product(id = "P004", name = "Digital Thermometer", price = 15.99, imageUrl = "https://images.unsplash.com/photo-1559757175-0eb30cd8c063?w=400", stock = 30)
        )
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Collect cart items state for badge
    val cartItems by cartViewModel.cartItems.collectAsState()
    
    // Category mapping for products (based on product names)
    val getProductCategory = { product: Product ->
        when {
            product.name.contains("Paracetamol", ignoreCase = true) || 
            product.name.contains("Aspirin", ignoreCase = true) -> "Pain Relief"
            product.name.contains("Vitamin", ignoreCase = true) -> "Vitamins"
            product.name.contains("Thermometer", ignoreCase = true) || 
            product.name.contains("Bandage", ignoreCase = true) -> "First Aid"
            product.name.contains("Strip", ignoreCase = true) -> "Diabetes Care"
            product.name.contains("Sanitizer", ignoreCase = true) -> "Personal Care"
            else -> "Health Devices"
        }
    }
    
    // Enhanced filtering logic - separate in-stock and out-of-stock products
    val (inStockProducts, outOfStockProducts) = remember(searchQuery, selectedCategory, products) {
        val filtered = products.filter { product ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                product.name.contains(searchQuery, ignoreCase = true)
            }
            val matchesCategory = if (selectedCategory == "All") true else {
                getProductCategory(product) == selectedCategory
            }
            matchesSearch && matchesCategory
        }
        
        // Separate in-stock and out-of-stock products
        val inStock = filtered.filter { it.stock > 0 }
        val outOfStock = filtered.filter { it.stock == 0 }
        
        Pair(inStock, outOfStock)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MedShop", fontWeight = FontWeight.Bold) },
                actions = {
                    // Cart Icon with Badge
                    BadgedBox(
                        badge = { 
                            if (cartItems.isNotEmpty()) {
                                Badge { 
                                    Text("${cartItems.sumOf { it.quantity }}") 
                                } 
                            }
                        }
                    ) {
                        IconButton(onClick = { navController.navigate("checkout") }) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Color(0xFF007AFF)
                            )
                        }
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
                .padding(horizontal = 16.dp)
                .   verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for medicines and products...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Promotional Banner Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(promoImages) { imageUrl ->
                    PromoCard(imageUrl = imageUrl)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Categories Filter
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF007AFF),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Order Again Section
            Text(
                text = "Order Again",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(orderAgainProducts) { product ->
                    OrderAgainCard(
                        product = product,
                        onAddToCart = { 
                            if (product.stock > 0) {
                                cartViewModel.addToCart(product) 
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Products Section
            Text(
                text = "All Products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Products Grid - Show in-stock products first, then out-of-stock
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(600.dp)
            ) {
                // In-stock products
                items(inStockProducts) { product ->
                    ProductCard(
                        product = product,
                        cartViewModel = cartViewModel,
                        onClick = { 
                            val encodedName = Uri.encode(product.name)
                            navController.navigate("product_detail/$encodedName")
                        },
                        isOutOfStock = false
                    )
                }
                
                // Out-of-stock products (greyed out)
                items(outOfStockProducts) { product ->
                    ProductCard(
                        product = product,
                        cartViewModel = cartViewModel,
                        onClick = { 
                            val encodedName = Uri.encode(product.name)
                            navController.navigate("product_detail/$encodedName")
                        },
                        isOutOfStock = true
                    )
                }
            }
            
            // Bottom spacing for better scrolling experience
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PromoCard(imageUrl: String) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Promotional Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun OrderAgainCard(
    product: Product,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product Image
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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
            
            // Add Button or Out of Stock indicator
            if (product.stock > 0) {
                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .background(Color(0xFF007AFF), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add to cart",
                        tint = Color.White
                    )
                }
            } else {
                Text(
                    text = "Out of Stock",
                    fontSize = 8.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    cartViewModel: CartViewModel,
    onClick: () -> Unit,
    isOutOfStock: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) Color(0xFFF5F5F5) else Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Product Image
                Box {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        alpha = if (isOutOfStock) 0.5f else 1f
                    )
                    
                    // Out of stock overlay
                    if (isOutOfStock) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "OUT OF STOCK",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Product Name
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    color = if (isOutOfStock) Color.Gray else Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Stock info
                if (product.stock <= 10 && product.stock > 0) {
                    Text(
                        text = "Only ${product.stock} left!",
                        fontSize = 10.sp,
                        color = Color(0xFFFF9500),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Price and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        fontWeight = FontWeight.Bold,
                        color = if (isOutOfStock) Color.Gray else Color(0xFF007AFF),
                        fontSize = 16.sp
                    )
                    
                    if (isOutOfStock) {
                        Text(
                            text = "Not Available",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Button(
                            onClick = { cartViewModel.addToCart(product) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF007AFF)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserDashboardScreenPreview() {
    UserDashboardScreen(navController = rememberNavController(), cartViewModel = CartViewModel())
} 