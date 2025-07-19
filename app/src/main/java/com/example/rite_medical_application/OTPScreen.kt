@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.rite_medical_application

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

@Composable
fun OTPScreen(
    navController: NavController,
    phoneNumber: String = "+91 ******1234",
    authViewModel: AuthViewModel = viewModel()
) {
    var otpCode by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(30) }
    var isTimerActive by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    
    // Observe auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.OtpVerified -> {
                // Navigate to complete profile screen for new users
                navController.navigate("complete_profile") {
                    popUpTo("welcome") { inclusive = true }
                }
            }
            else -> { /* Handle other states if needed */ }
        }
    }
    
    // Countdown timer
    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            isTimerActive = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF007AFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAFAFA)
                )
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Verification Title
            Text(
                text = "Verifying your number",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone number display
            Text(
                text = "Enter the 6-digit code sent to",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = phoneNumber,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Hidden TextField for actual input
            OutlinedTextField(
                value = otpCode,
                onValueChange = { 
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        otpCode = it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.dp), // Hide the actual text field
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true
            )
            
            // OTP Display Boxes
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                repeat(6) { index ->
                    OTPDigitDisplay(
                        digit = otpCode.getOrNull(index)?.toString() ?: "",
                        isActive = index == otpCode.length && otpCode.length < 6
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Verify Button
            Button(
                onClick = {
                    if (otpCode.length == 6) {
                        authViewModel.verifyOtp(otpCode)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = otpCode.length == 6 && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF),
                    disabledContainerColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Verify",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (otpCode.length == 6) Color.White else Color(0xFF9CA3AF)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Timer/Resend Section
            if (isTimerActive) {
                Text(
                    text = "Resend code in 0:${timeLeft.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            } else {
                TextButton(
                    onClick = {
                        // Reset timer and resend OTP
                        timeLeft = 30
                        isTimerActive = true
                        otpCode = "" // Clear current OTP
                        authViewModel.resendOtp(phoneNumber, context as Activity)
                    }
                ) {
                    Text(
                        text = "Resend OTP",
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Help Text
            Text(
                text = "Didn't receive the code? Check your SMS or try calling.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Error Dialog
        errorMessage?.let { error ->
            AlertDialog(
                onDismissRequest = { authViewModel.clearError() },
                title = { Text("Verification Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { authViewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun OTPDigitDisplay(
    digit: String,
    isActive: Boolean
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isActive || digit.isNotEmpty()) 2.dp else 1.dp,
                color = if (isActive || digit.isNotEmpty()) Color(0xFF007AFF) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (digit.isNotEmpty()) {
            Text(
                text = digit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )
        } else if (isActive) {
            // Blinking cursor effect for active box
            Text(
                text = "|",
                fontSize = 24.sp,
                color = Color(0xFF007AFF),
                textAlign = TextAlign.Center
            )
        } else {
            // Placeholder dot for empty boxes
            Text(
                text = "•",
                fontSize = 24.sp,
                color = Color(0xFFD1D5DB),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OTPScreenPreview() {
    OTPScreen(
        navController = rememberNavController(),
        phoneNumber = "+91 ******1234"
    )
} 