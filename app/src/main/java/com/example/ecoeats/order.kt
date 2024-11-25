package com.example.ecoeats

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Order(
    val items: @RawValue List<OrderItem>, // List of OrderItems
    val status: String,
    val totalPrice: Double
) : Parcelable
