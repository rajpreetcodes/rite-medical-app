package com.example.rite_medical_application.network

import com.example.rite_medical_application.models.Order
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("customer-notification") // Replace with your actual webhook path
    fun sendOrderUpdate(@Body order: Order): Call<Void>
} 