package com.example.rite_medical_application

object ProductRepository {
    // Mutable list to allow threshold updates
    private val _products = mutableListOf(
        Product("P001", "Paracetamol 500mg", 9.99, "https://picsum.photos/200", 50, 20), // High sales - higher threshold
        Product("P002", "Vitamin C 1000mg", 14.99, "https://picsum.photos/201", 30, 15), // Medium sales
        Product("P003", "First Aid Kit Basic", 24.99, "https://picsum.photos/202", 20, 5), // Low sales - lower threshold
        Product("P004", "Digital Thermometer", 19.99, "https://picsum.photos/203", 15, 3), // Low sales
        Product("P005", "Hand Sanitizer 500ml", 4.99, "https://picsum.photos/204", 100, 25), // High sales
        Product("P006", "Face Masks (50 Pack)", 12.99, "https://picsum.photos/205", 0, 30), // High demand when available
        Product("P007", "Multivitamin Complex", 29.99, "https://picsum.photos/206", 40, 10), // Medium sales
        Product("P008", "Bandages Pack", 7.99, "https://picsum.photos/207", 150, 20), // High sales
        Product("P009", "Pain Relief Gel", 11.99, "https://picsum.photos/208", 0, 15), // Medium sales
        Product("P010", "Cough Syrup 200ml", 8.99, "https://picsum.photos/209", 5, 8), // Seasonal - higher threshold
        Product("P011", "Aspirin 325mg", 6.99, "https://picsum.photos/210", 25, 12), // Medium sales
        Product("P012", "Blood Pressure Monitor", 89.99, "https://picsum.photos/211", 8, 2), // Low sales - expensive item
        Product("P013", "Glucose Test Strips", 34.99, "https://picsum.photos/212", 0, 5) // Specialized item
    )
    
    val products: List<Product> get() = _products.toList()
    
    // Function to update a product's low stock threshold
    fun updateProductThreshold(productId: String, newThreshold: Int) {
        val index = _products.indexOfFirst { it.id == productId }
        if (index != -1) {
            val product = _products[index]
            _products[index] = product.copy(lowStockThreshold = newThreshold)
        }
    }
} 