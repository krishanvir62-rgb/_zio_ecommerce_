package com.example.zio_ecommercd.data.model

data class UpcomingSale(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val discountPercent: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val category: String = ""
)
