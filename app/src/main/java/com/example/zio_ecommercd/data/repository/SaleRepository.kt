package com.example.zio_ecommercd.data.repository

import com.example.zio_ecommercd.data.model.UpcomingSale
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getActiveSales(): Result<List<UpcomingSale>> {
        return try {
            val snapshot = firestore.collection("upcoming_sales").get().await()
            val sales = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UpcomingSale::class.java)?.copy(id = doc.id)
            }.filter { it.isActive }.sortedByDescending { it.createdAt }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllSales(): Result<List<UpcomingSale>> {
        return try {
            val snapshot = firestore.collection("upcoming_sales")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val sales = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UpcomingSale::class.java)?.copy(id = doc.id)
            }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getActiveSalesStream(): kotlinx.coroutines.flow.Flow<Result<List<UpcomingSale>>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = firestore.collection("upcoming_sales")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val sales = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(UpcomingSale::class.java)?.copy(id = doc.id)
                    }.filter { it.isActive }.sortedByDescending { it.createdAt }
                    trySend(Result.success(sales))
                }
            }
        awaitClose { listener.remove() }
    }

    fun getAllSalesStream(): kotlinx.coroutines.flow.Flow<Result<List<UpcomingSale>>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = firestore.collection("upcoming_sales")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val sales = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(UpcomingSale::class.java)?.copy(id = doc.id)
                    }
                    trySend(Result.success(sales))
                }
            }
        awaitClose { listener.remove() }
    }
}
