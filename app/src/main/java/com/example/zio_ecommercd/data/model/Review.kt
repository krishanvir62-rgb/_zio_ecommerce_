package com.example.zio_ecommercd.data.model

data class Review(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val rating: Int = 0,
    val reviewText: String = "",
    val timestamp: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
