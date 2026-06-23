package com.example.zio_ecommercd.data.repository

import com.example.zio_ecommercd.data.local.FavoriteDao
import com.example.zio_ecommercd.data.local.FavoriteEntity
import com.example.zio_ecommercd.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val firestore: FirebaseFirestore
) {

    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    fun isFavorite(productId: String): Flow<Boolean> = favoriteDao.isFavorite(productId)

    suspend fun addFavorite(product: Product) {
        val effectiveImageUrl = resolveImageUrl(product)
        val entity = FavoriteEntity(
            productId = product.id,
            name = product.name,
            description = product.description,
            price = product.price,
            category = product.category,
            imageUrl = effectiveImageUrl,
            rating = product.rating,
            uploaderName = product.uploaderName,
            uploaderContact = product.uploaderContact
        )
        favoriteDao.addFavorite(entity)
    }

    suspend fun refreshFavoriteImage(productId: String) {
        try {
            val doc = firestore.collection("products").document(productId).get().await()
            val freshProduct = doc.toObject(Product::class.java)?.copy(id = doc.id) ?: return
            val freshImageUrl = freshProduct.imageUrl.ifEmpty { freshProduct.images.firstOrNull() ?: "" }
            if (freshImageUrl.isEmpty()) return
            val existing = favoriteDao.getFavoriteById(productId) ?: return
            if (existing.imageUrl == freshImageUrl) return
            favoriteDao.addFavorite(existing.copy(imageUrl = freshImageUrl))
        } catch (_: Exception) { }
    }

    private suspend fun resolveImageUrl(product: Product): String {
        val fromProduct = product.imageUrl.ifEmpty { product.images.firstOrNull() ?: "" }
        if (fromProduct.isNotEmpty()) return fromProduct
        return try {
            val doc = firestore.collection("products").document(product.id).get().await()
            val fresh = doc.toObject(Product::class.java)?.copy(id = doc.id)
            fresh?.imageUrl?.ifEmpty { fresh.images.firstOrNull() ?: "" } ?: ""
        } catch (_: Exception) { "" }
    }

    suspend fun removeFavorite(productId: String) {
        favoriteDao.removeFavorite(productId)
    }
}
