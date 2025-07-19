package com.example.rite_medical_application.repositories

import android.util.Log
import com.example.rite_medical_application.models.Order
import com.example.rite_medical_application.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OrderRepository {
    private val apiService: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5678/") // Use 10.0.2.2 for Android emulator localhost access. Change to production n8n URL in release build.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    fun sendOrderUpdate(order: Order, onSuccess: () -> Unit, onError: (String) -> Unit) {
        apiService.sendOrderUpdate(order).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("OrderRepository", "Webhook sent successfully for order ${order.orderId}")
                    onSuccess()
                } else {
                    Log.e("OrderRepository", "Webhook failed: ${response.code()}")
                    onError("Failed to send notification: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("OrderRepository", "Webhook error: ${t.message}")
                onError("Error sending notification: ${t.message}")
            }
        })
    }
} 