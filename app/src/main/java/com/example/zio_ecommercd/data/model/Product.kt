package com.example.zio_ecommercd.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val reviewCount: Int = 0,
    val uploaderId: String = "",
    val uploaderName: String = "",
    val uploaderContact: String = ""
)
