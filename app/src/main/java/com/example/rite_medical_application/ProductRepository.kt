package com.example.rite_medical_application

object ProductRepository {
    val products by lazy {
        listOf(
            Product("P001", "Paracetamol 500mg", 9.99, "https://picsum.photos/200", 50),
            Product("P002", "Vitamin C 1000mg", 14.99, "https://picsum.photos/201", 30),
            Product("P003", "First Aid Kit Basic", 24.99, "https://picsum.photos/202", 20),
            Product("P004", "Digital Thermometer", 19.99, "https://picsum.photos/203", 15),
            Product("P005", "Hand Sanitizer 500ml", 4.99, "https://picsum.photos/204", 100),
            Product("P006", "Face Masks (50 Pack)", 12.99, "https://picsum.photos/205", 200),
            Product("P007", "Multivitamin Complex", 29.99, "https://picsum.photos/206", 40),
            Product("P008", "Bandages Pack", 7.99, "https://picsum.photos/207", 150),
            Product("P009", "Pain Relief Gel", 11.99, "https://picsum.photos/208", 60),
            Product("P010", "Cough Syrup 200ml", 8.99, "https://picsum.photos/209", 45)
        )
    }
} 