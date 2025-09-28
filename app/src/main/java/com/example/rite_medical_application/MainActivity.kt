package com.example.rite_medical_application

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rite_medical_application.ui.theme.Rite_medical_applicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Rite_medical_applicationTheme {
                // Create navController
                val navController = rememberNavController()
                
                // Create the view models
                val authViewModel: AuthViewModel = viewModel()
        val cartViewModel: CartViewModel = viewModel()
        val paymentViewModel: PaymentViewModel = viewModel()
        val couponViewModel: CouponViewModel = viewModel()
                
                // Set up NavHost
                NavHost(
                    navController = navController,
                    startDestination = "welcome",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Welcome screen route (new entry point)
                    composable("welcome") {
                        WelcomeScreen(navController, authViewModel)
                    }
                    
                    // OTP Verification screen route
                    composable(
                        route = "otp_verification/{phoneNumber}",
                        arguments = listOf(
                            navArgument("phoneNumber") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                        OTPScreen(
                            navController = navController,
                            phoneNumber = Uri.decode(phoneNumber),
                            authViewModel = authViewModel
                        )
                    }
                    
                    // Complete Profile screen route
                    composable("complete_profile") {
                        CompleteProfileScreen(navController)
                    }
                    
                    // Splash screen route
                    composable("splash") {
                        SplashScreen(navController)
                    }
                    
                    // Auth choice screen route
                    composable("auth_choice") {
                        AuthChoiceScreen(
                            onSignInClick = {
                                navController.navigate("login")
                            },
                            onCreateAccountClick = {
                                navController.navigate("registration")
                            }
                        )
                    }
                    
                    // Login screen route
                    composable("login") {
                        LoginScreen(
                            onSignUpClick = {
                                navController.navigate("registration")
                            },
                            onForgotPasswordClick = {
                                navController.navigate("forgot_password")
                            },
                            onSignInClick = { email, password ->
                                // TODO: Implement Firebase authentication logic
                                // For now, navigate directly to dashboard
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // Dashboard screen route
                    composable("dashboard") {
                        UserDashboardScreen(navController, cartViewModel)
                    }
                    
                    // Product Detail screen route
                    composable(
                        route = "product_detail/{productName}",
                        arguments = listOf(
                            navArgument("productName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val productName = backStackEntry.arguments?.getString("productName") ?: ""
                        ProductDetailScreen(
                            navController = navController,
                            productName = Uri.decode(productName)
                        )
                    }
                    
                    // Checkout screen route
                    composable("checkout") {
                        CheckoutScreen(navController, cartViewModel, paymentViewModel, couponViewModel)
                    }
                    
                    // Payment Selection screen route
                    composable("payment_selection") {
                        PaymentSelectionScreen(navController, paymentViewModel)
                    }
                    
                    // Address Selection screen route
                    composable("address_selection") {
                        AddressScreen(navController) { selectedAddress ->
                            // Handle address selection
                            navController.popBackStack()
                        }
                    }
                    
                    // Coupon Selection screen route
                    composable(
                        route = "coupon_selection/{cartTotal}",
                        arguments = listOf(
                            navArgument("cartTotal") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val cartTotal = backStackEntry.arguments?.getString("cartTotal")?.toDoubleOrNull() ?: 0.0
                        CouponScreen(
                            navController = navController,
                            cartTotal = cartTotal
                        ) { selectedCoupon ->
                            // Apply coupon via ViewModel
                            couponViewModel.applyCoupon(selectedCoupon)
                            navController.popBackStack()
                        }
                    }
                    
                    // Order Confirmation screen route
                    composable(
                        route = "order_confirmation/{orderId}",
                        arguments = listOf(
                            navArgument("orderId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                        OrderConfirmationScreen(
                            navController = navController,
                            orderId = Uri.decode(orderId)
                        )
                    }
                    
                    // Create Schedule screen route
                    composable(
                        route = "create_schedule/{productName}",
                        arguments = listOf(
                            navArgument("productName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val productName = backStackEntry.arguments?.getString("productName") ?: ""
                        CreateScheduleScreen(
                            navController = navController,
                            productName = Uri.decode(productName)
                        )
                    }
                    
                    // Registration screen route
                    composable("registration") {
                        RegistrationScreen(
                            onSignInClick = {
                                navController.navigate("login") {
                                    popUpTo("registration") { inclusive = true }
                                }
                            },
                            onCreateAccountClick = { fullName, email, phone, password, confirmPassword ->
                                // TODO: Implement Firebase registration logic
                                // For now, navigate back to login to indicate success
                                navController.navigate("login") {
                                    popUpTo("registration") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // Forgot Password screen route
                    composable("forgot_password") {
                        ForgotPasswordScreen(
                            onBackToSignIn = {
                                navController.popBackStack()
                            },
                            onResetPasswordClick = { email ->
                                // TODO: Implement password reset logic
                            }
                        )
                    }
                }
            }
        }
    }
}

