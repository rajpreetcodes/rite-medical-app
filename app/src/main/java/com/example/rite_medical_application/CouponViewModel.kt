package com.example.rite_medical_application

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CouponViewModel : ViewModel() {
    
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()
    
    fun applyCoupon(coupon: Coupon?) {
        _appliedCoupon.value = coupon
    }
    
    fun removeCoupon() {
        _appliedCoupon.value = null
    }
    
    fun calculateDiscount(coupon: Coupon, cartTotal: Double): Double {
        return when (coupon.discountType) {
            DiscountType.PERCENTAGE -> {
                val percentageDiscount = (cartTotal * coupon.discount) / 100
                minOf(percentageDiscount, coupon.maxDiscount)
            }
            DiscountType.FIXED_AMOUNT -> coupon.discount
        }
    }
}
