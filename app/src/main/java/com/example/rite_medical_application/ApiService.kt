package com.example.rite_medical_application

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("webhook-test/customer-notification")
    suspend fun sendOrderNotification(@Body orderData: Map<String, Any>): Response<Void>
    
    @POST("webhook-test/order-processing")
    suspend fun processOrder(@Body orderData: Map<String, Any>): Response<Void>
    
    @POST("webhook-test/inventory-update")
    suspend fun updateInventory(@Body orderData: Map<String, Any>): Response<Void>
} 