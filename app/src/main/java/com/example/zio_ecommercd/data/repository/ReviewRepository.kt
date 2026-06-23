package com.example.zio_ecommercd.data.repository

import com.example.zio_ecommercd.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Real-time stream of all reviews for a product, sorted newest first.
     */
    fun getReviewsForProductStream(productId: String): Flow<Result<List<Review>>> = callbackFlow {
        val listener = firestore.collection("reviews")
            .whereEqualTo("productId", productId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // 1. Get base reviews from snapshot (synchronous)
                    val baseReviews = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Review::class.java)?.copy(id = doc.id)
                    }

                    // 2. Launch coroutine in callbackFlow scope to fetch user details (asynchronous)
                    launch {
                        val fullyPopulatedReviews = baseReviews.map { review ->
                            try {
                                val userDoc = firestore.collection("users").document(review.userId).get().await()
                                val photoUrl = userDoc.getString("photoUrl") ?: ""
                                val name = userDoc.getString("name") ?: review.userName
                                review.copy(
                                    userName = if (name.isNotBlank()) name else review.userName,
                                    userPhotoUrl = photoUrl
                                )
                            } catch (e: Exception) {
                                review
                            }
                        }
                        trySend(Result.success(fullyPopulatedReviews))
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Check if a review already exists for this user + product combo.
     * Returns the existing Review or null.
     */
    suspend fun getUserReviewForProduct(userId: String, productId: String): Review? {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", productId)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Submit or update a review using Firestore Transaction.
     * - If user already reviewed this product, UPDATE that document (no duplicates).
     * - If new review, CREATE a new document.
     * - Atomically recalculates product rating/ratingCount/reviewCount.
     */
    suspend fun submitReview(review: Review): Result<Unit> {
        return try {
            val productRef = firestore.collection("products").document(review.productId)
            val now = System.currentTimeMillis()

            // First check if user already has a review for this product
            val existingReview = getUserReviewForProduct(review.userId, review.productId)

            val reviewRef = if (existingReview != null) {
                // Use existing document ID to update, never create duplicate
                firestore.collection("reviews").document(existingReview.id)
            } else {
                // New review document
                firestore.collection("reviews").document()
            }

            val finalReview = review.copy(
                id = reviewRef.id,
                timestamp = now,
                createdAt = existingReview?.createdAt ?: now,
                updatedAt = now
            )

            firestore.runTransaction { transaction ->
                val productSnapshot = transaction.get(productRef)

                val currentRating = productSnapshot.getDouble("rating") ?: 0.0
                val currentRatingCount = productSnapshot.getLong("ratingCount")?.toInt() ?: 0
                val currentReviewCount = productSnapshot.getLong("reviewCount")?.toInt() ?: 0

                val newRatingCount: Int
                val newReviewCount: Int
                val newAverageRating: Double

                if (existingReview != null) {
                    // Updating existing review — rating count stays same
                    newRatingCount = currentRatingCount
                    val oldReviewText = existingReview.reviewText
                    newReviewCount = if (oldReviewText.isEmpty() && finalReview.reviewText.isNotEmpty()) {
                        currentReviewCount + 1
                    } else if (oldReviewText.isNotEmpty() && finalReview.reviewText.isEmpty()) {
                        currentReviewCount - 1
                    } else {
                        currentReviewCount
                    }

                    val totalRatingSum = (currentRating * currentRatingCount) - existingReview.rating + finalReview.rating
                    newAverageRating = if (newRatingCount > 0) totalRatingSum / newRatingCount else 0.0
                } else {
                    // Brand new review
                    newRatingCount = currentRatingCount + 1
                    newReviewCount = if (finalReview.reviewText.isNotEmpty()) currentReviewCount + 1 else currentReviewCount

                    val totalRatingSum = (currentRating * currentRatingCount) + finalReview.rating
                    newAverageRating = totalRatingSum / newRatingCount
                }

                val boundedRating = newAverageRating.coerceIn(0.0, 5.0)

                transaction.set(reviewRef, finalReview)
                transaction.update(
                    productRef,
                    mapOf(
                        "rating" to boundedRating,
                        "ratingCount" to newRatingCount,
                        "reviewCount" to newReviewCount
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
