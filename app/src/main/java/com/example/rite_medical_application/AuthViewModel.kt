package com.example.rite_medical_application

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    // State variables
    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    sealed class AuthState {
        object Initial : AuthState()
        object Loading : AuthState()
        object OtpSent : AuthState()
        object OtpVerified : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    fun sendOtpToPhone(phoneNumber: String, activity: Activity) {
        _isLoading.value = true
        _authState.value = AuthState.Loading
        _errorMessage.value = null
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("AuthViewModel", "onVerificationCompleted: $credential")
                signInWithPhoneAuthCredential(credential)
            }
            
            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("AuthViewModel", "onVerificationFailed", e)
                _isLoading.value = false
                _authState.value = AuthState.Error(e.message ?: "Verification failed")
                _errorMessage.value = e.message ?: "Verification failed"
            }
            
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("AuthViewModel", "onCodeSent: $verificationId")
                _verificationId.value = verificationId
                _isLoading.value = false
                _authState.value = AuthState.OtpSent
            }
        }
        
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    
    fun verifyOtp(otpCode: String) {
        val verificationId = _verificationId.value
        if (verificationId.isNullOrEmpty()) {
            _authState.value = AuthState.Error("Verification ID not found")
            _errorMessage.value = "Verification ID not found"
            return
        }
        
        _isLoading.value = true
        _authState.value = AuthState.Loading
        _errorMessage.value = null
        
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        signInWithPhoneAuthCredential(credential)
    }
    
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("AuthViewModel", "signInWithCredential:success")
                    val user = task.result?.user
                    _authState.value = AuthState.OtpVerified
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("AuthViewModel", "signInWithCredential:failure", task.exception)
                    _authState.value = AuthState.Error(task.exception?.message ?: "Authentication failed")
                    _errorMessage.value = task.exception?.message ?: "Authentication failed"
                }
            }
    }
    
    fun resendOtp(phoneNumber: String, activity: Activity) {
        sendOtpToPhone(phoneNumber, activity)
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun getCurrentUser() = firebaseAuth.currentUser
    
    fun signOut() {
        firebaseAuth.signOut()
        _verificationId.value = null
        _authState.value = AuthState.Initial
    }
    
    fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
} 