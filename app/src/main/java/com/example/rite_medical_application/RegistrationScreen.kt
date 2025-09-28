package com.example.rite_medical_application

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistrationScreen(
    onSignInClick: () -> Unit = {},
    onCreateAccountClick: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var createPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var createPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    
    // Email validation
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length > 5
    }
    
    // Password validation
    fun isValidPassword(password: String): Boolean {
        return password.length > 6 && password.any { it.isDigit() }
    }
    
    // Validation states
    val emailError = when {
        email.isEmpty() -> null
        !isValidEmail(email.trim()) -> "Please enter a valid email address with @"
        else -> null
    }
    
    val passwordError = when {
        createPassword.isEmpty() -> null
        !isValidPassword(createPassword) -> "Password must be longer than 6 characters and contain numbers"
        else -> null
    }
    
    // Validation: All fields filled, passwords match, and terms accepted
    val passwordsMatch = createPassword.isNotEmpty() && createPassword == confirmPassword
    val isFormValid = fullName.trim().isNotEmpty() && 
                      email.trim().isNotEmpty() && 
                      phoneNumber.trim().isNotEmpty() && 
                      createPassword.trim().isNotEmpty() && 
                      confirmPassword.trim().isNotEmpty() && 
                      passwordsMatch && 
                      termsAccepted &&
                      emailError == null &&
                      passwordError == null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // App Logo
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Rite Medical Logo",
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF2E7D4A)
            )
            
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            )
            
            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = emailError != null
            )
            
            // Email validation error message
            if (emailError != null) {
                Text(
                    text = emailError,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone Number Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create Password Input
            OutlinedTextField(
                value = createPassword,
                onValueChange = { createPassword = it },
                label = { Text("Create Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (createPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = passwordError != null,
                trailingIcon = {
                    IconButton(onClick = { createPasswordVisible = !createPasswordVisible }) {
                        Text(
                            text = if (createPasswordVisible) "HIDE" else "SHOW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF007AFF)
                        )
                    }
                }
            )
            
            // Password validation error message
            if (passwordError != null) {
                Text(
                    text = passwordError,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(
                            text = if (confirmPasswordVisible) "HIDE" else "SHOW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF007AFF)
                        )
                    }
                }
            )
            
            // Password match error message
            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                Text(
                    text = "Passwords do not match",
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Terms & Conditions Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF007AFF),
                        uncheckedColor = Color(0xFF8E8E93)
                    )
                )
                Text(
                    text = buildAnnotatedString {
                        append("I agree to the ")
                        withStyle(style = SpanStyle(
                            color = Color(0xFF007AFF),
                            fontWeight = FontWeight.Bold
                        )) {
                            append("Terms & Conditions")
                        }
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.clickable { termsAccepted = !termsAccepted }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Create Account Button
            Button(
                onClick = { 
                    if (isFormValid) {
                        onCreateAccountClick(
                            fullName.trim(), 
                            email.trim(), 
                            phoneNumber.trim(), 
                            createPassword.trim(), 
                            confirmPassword.trim()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color(0xFF007AFF) else Color(0xFFCCCCCC),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = isFormValid
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign In Text
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(style = SpanStyle(
                            color = Color(0xFF007AFF),
                            fontWeight = FontWeight.Bold
                        )) {
                            append("Sign In")
                        }
                    },
                    fontSize = 16.sp,
                    color = Color(0xFF1C1C1E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { onSignInClick() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    RegistrationScreen()
} 