package com.example.rite_medical_application

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OrderRepository private constructor() {
    private val baseUrl = "http://10.0.2.2:5678/" // Android emulator address for localhost
    
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val apiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    
    suspend fun sendOrderNotification(orders: List<Order>) = withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf("body" to orders)
            val response = apiService.sendOrderNotification(requestBody)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun processOrder(orders: List<Order>) = withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf("body" to orders)
            val response = apiService.processOrder(requestBody)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun updateInventory(orders: List<Order>) = withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf("body" to orders)
            val response = apiService.updateInventory(requestBody)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: OrderRepository? = null
        
        fun getInstance(): OrderRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OrderRepository().also { INSTANCE = it }
            }
        }
    }
} 