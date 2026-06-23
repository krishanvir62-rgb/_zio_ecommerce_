package com.example.zio_ecommercd.data.repository

import com.example.zio_ecommercd.data.local.CartDao
import com.example.zio_ecommercd.data.local.CartEntity
import com.example.zio_ecommercd.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val cartDao: CartDao
) {

    fun getAllCartItems(): Flow<List<CartEntity>> = cartDao.getAllCartItems()

    fun getCartItemCount(): Flow<Int> = cartDao.getCartItemCount().map { it ?: 0 }

    suspend fun addToCart(product: Product, quantity: Int = 1) {
        val effectiveImageUrl = product.imageUrl.ifEmpty { product.images.firstOrNull() ?: "" }
        val entity = CartEntity(
            productId = product.id,
            name = product.name,
            price = product.price,
            imageUrl = effectiveImageUrl,
            category = product.category,
            quantity = quantity
        )
        cartDao.addToCart(entity)
    }

    suspend fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.removeFromCart(productId)
        } else {
            cartDao.updateQuantity(productId, quantity)
        }
    }

    suspend fun removeFromCart(productId: String) {
        cartDao.removeFromCart(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }
}
