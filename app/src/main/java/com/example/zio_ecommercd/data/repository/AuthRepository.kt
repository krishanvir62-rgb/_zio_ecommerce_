package com.example.zio_ecommercd.data.repository

import android.net.Uri
import com.example.zio_ecommercd.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cloudinaryHelper: CloudinaryHelper
) {

    val currentUser get() = firebaseAuth.currentUser

    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun logout() {
        firebaseAuth.signOut()
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Google Sign In failed"))
            
            // Check if user exists in Firestore
            val doc = firestore.collection("users").document(uid).get().await()
            val user = if (doc.exists()) {
                doc.toObject(User::class.java) ?: throw Exception("Invalid user data")
            } else {
                // Create new user record
                val newUser = User(
                    id = uid,
                    name = result.user?.displayName ?: "Google User",
                    email = result.user?.email ?: "",
                    phone = result.user?.phoneNumber ?: "",
                    photoUrl = result.user?.photoUrl?.toString() ?: ""
                )
                firestore.collection("users").document(uid).set(newUser).await()
                newUser
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("User not found"))
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java) ?: return Result.failure(Exception("User data not found"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, phone: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("User not created"))
            val user = User(
                id = uid,
                name = name,
                email = email,
                phone = phone,
                photoUrl = ""
            )
            firestore.collection("users").document(uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserInfo(): User? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun uploadProfilePhoto(imageUri: Uri): Result<String> {
        val uid = firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val uploadResult = cloudinaryHelper.uploadImage(imageUri)
            uploadResult.fold(
                onSuccess = { secureUrl ->
                    firestore.collection("users").document(uid)
                        .update("photoUrl", secureUrl)
                        .await()
                    Result.success(secureUrl)
                },
                onFailure = { e ->
                    Result.failure(e)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePhone(phone: String): Result<Unit> {
        val uid = firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("users").document(uid)
                .update("address", phone)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
