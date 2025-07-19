package com.example.rite_medical_application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Launch effect to run side-effect only once when screen is first displayed
    LaunchedEffect(key1 = true) {
        delay(2000L) // Wait for 2 seconds
        navController.navigate("auth_choice") {
            popUpTo("splash") { inclusive = true } // Navigate to next screen and remove splash from back stack
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder Icon for app logo
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Rite Medical Logo",
            modifier = Modifier.size(120.dp),
            tint = Color(0xFF2E7D4A) // Medical green color
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = rememberNavController())
} 