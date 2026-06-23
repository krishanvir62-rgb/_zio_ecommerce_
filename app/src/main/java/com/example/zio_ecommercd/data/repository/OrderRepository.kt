package com.example.zio_ecommercd.data.repository

import com.example.zio_ecommercd.data.model.Order
import com.example.zio_ecommercd.data.model.OrderItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getOrdersForUser(userId: String): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val orders = snapshot.documents.map { doc ->
                val items = (doc.get("items") as? List<Map<String, Any>>)?.map { item ->
                    OrderItem(
                        productId = item["productId"] as? String ?: "",
                        name = item["name"] as? String ?: "",
                        price = (item["price"] as? Number)?.toDouble() ?: 0.0,
                        quantity = (item["quantity"] as? Number)?.toInt() ?: 0,
                        imageUrl = item["imageUrl"] as? String ?: ""
                    )
                } ?: emptyList()

                Order(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    items = items,
                    subtotal = (doc.get("subtotal") as? Number)?.toDouble() ?: 0.0,
                    deliveryCharge = (doc.get("deliveryCharge") as? Number)?.toDouble() ?: 0.0,
                    total = (doc.get("total") as? Number)?.toDouble() ?: 0.0,
                    status = doc.getString("status") ?: "confirmed",
                    paymentMethod = doc.getString("paymentMethod") ?: "COD",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }.sortedByDescending { it.timestamp }

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOrdersForUserStream(userId: String): kotlinx.coroutines.flow.Flow<Result<List<Order>>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.map { doc ->
                        val items = (doc.get("items") as? List<Map<String, Any>>)?.map { item ->
                            OrderItem(
                                productId = item["productId"] as? String ?: "",
                                name = item["name"] as? String ?: "",
                                price = (item["price"] as? Number)?.toDouble() ?: 0.0,
                                quantity = (item["quantity"] as? Number)?.toInt() ?: 0,
                                imageUrl = item["imageUrl"] as? String ?: ""
                            )
                        } ?: emptyList()

                        Order(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            items = items,
                            subtotal = (doc.get("subtotal") as? Number)?.toDouble() ?: 0.0,
                            deliveryCharge = (doc.get("deliveryCharge") as? Number)?.toDouble() ?: 0.0,
                            total = (doc.get("total") as? Number)?.toDouble() ?: 0.0,
                            status = doc.getString("status") ?: "confirmed",
                            paymentMethod = doc.getString("paymentMethod") ?: "COD",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }.sortedByDescending { it.timestamp }
                    trySend(Result.success(orders))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun hasUserPurchasedProduct(userId: String, productId: String): Boolean {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.any { doc ->
                val items = (doc.get("items") as? List<Map<String, Any>>)?.mapNotNull { it["productId"] as? String }
                items?.contains(productId) == true
            }
        } catch (e: Exception) {
            false
        }
    }
}
