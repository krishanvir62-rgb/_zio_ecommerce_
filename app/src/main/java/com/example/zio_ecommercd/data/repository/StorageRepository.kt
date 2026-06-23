package com.example.zio_ecommercd.data.repository

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val cloudinaryHelper: CloudinaryHelper
) {
    suspend fun uploadImages(imageUris: List<Uri>): Result<List<String>> {
        return try {
            val uploadedUrls = mutableListOf<String>()
            for (uri in imageUris) {
                val result = cloudinaryHelper.uploadImage(uri)
                if (result.isSuccess) {
                    uploadedUrls.add(result.getOrNull() ?: "")
                } else {
                    return Result.failure(result.exceptionOrNull() ?: Exception("Failed to upload an image"))
                }
            }
            Result.success(uploadedUrls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
