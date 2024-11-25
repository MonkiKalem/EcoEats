package com.example.ecoeats

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderItem(
    val name: String,
    val price: Double,
    val image: String,
    val quantity: Int
) : Parcelable
