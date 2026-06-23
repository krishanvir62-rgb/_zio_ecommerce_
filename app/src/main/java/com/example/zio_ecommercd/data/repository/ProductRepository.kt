package com.example.zio_ecommercd.data.repository

import android.net.Uri
import com.example.zio_ecommercd.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageRepository: StorageRepository
) {

    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products").get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductById(productId: String): Result<Product> {
        return try {
            val doc = firestore.collection("products").document(productId).get().await()
            val product = doc.toObject(Product::class.java)?.copy(id = doc.id)
                ?: return Result.failure(Exception("Product not found"))
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProduct(product: Product, imageUris: List<Uri>): Result<Product> {
        return try {
            val storageResult = storageRepository.uploadImages(imageUris)
            if (storageResult.isFailure) {
                return Result.failure(storageResult.exceptionOrNull() ?: Exception("Image upload failed"))
            }
            val imageUrls = storageResult.getOrNull() ?: emptyList()

            val productWithImages = product.copy(
                imageUrl = imageUrls.firstOrNull() ?: "",
                images = imageUrls
            )

            val docRef = firestore.collection("products").document()
            val finalProduct = productWithImages.copy(id = docRef.id)
            docRef.set(finalProduct).await()

            Result.success(finalProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsBySellerId(sellerId: String): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("uploaderId", sellerId)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            firestore.collection("products")
                .document(product.id)
                .set(product)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            firestore.collection("products")
                .document(productId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllProductsStream(): kotlinx.coroutines.flow.Flow<Result<List<Product>>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = firestore.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java)?.copy(id = doc.id)
                    }
                    trySend(Result.success(products))
                }
            }
        awaitClose { listener.remove() }
    }

    fun getProductByIdStream(productId: String): kotlinx.coroutines.flow.Flow<Result<Product>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = firestore.collection("products").document(productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val product = snapshot.toObject(Product::class.java)?.copy(id = snapshot.id)
                    if (product != null) {
                        trySend(Result.success(product))
                    } else {
                        trySend(Result.failure(Exception("Invalid product data")))
                    }
                } else {
                    trySend(Result.failure(Exception("Product not found")))
                }
            }
        awaitClose { listener.remove() }
    }

    fun getProductsBySellerIdStream(sellerId: String): kotlinx.coroutines.flow.Flow<Result<List<Product>>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = firestore.collection("products")
            .whereEqualTo("uploaderId", sellerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java)?.copy(id = doc.id)
                    }
                    trySend(Result.success(products))
                }
            }
        awaitClose { listener.remove() }
    }
}
