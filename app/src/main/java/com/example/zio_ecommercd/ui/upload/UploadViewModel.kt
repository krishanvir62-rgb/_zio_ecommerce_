package com.example.zio_ecommercd.ui.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zio_ecommercd.data.model.Product
import com.example.zio_ecommercd.data.repository.AuthRepository
import com.example.zio_ecommercd.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UploadState {
    data object Idle : UploadState()
    data object Loading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    fun addImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value + uri
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value - uri
    }

    fun uploadProduct(
        title: String,
        description: String,
        price: String,
        category: String
    ) {
        if (title.isBlank() || description.isBlank() || price.isBlank() || category.isBlank()) {
            _uploadState.value = UploadState.Error("Please fill in all fields")
            return
        }

        val priceValue = price.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0) {
            _uploadState.value = UploadState.Error("Please enter a valid price")
            return
        }

        if (_selectedImages.value.size < 3) {
            _uploadState.value = UploadState.Error("Please select at least 3 images")
            return
        }

        viewModelScope.launch {
            _uploadState.value = UploadState.Loading

            val currentUser = authRepository.getCurrentUserInfo()

            val product = Product(
                name = title,
                description = description,
                price = priceValue,
                category = category,
                uploaderId = currentUser?.id ?: "",
                uploaderName = currentUser?.name ?: "Unknown",
                uploaderContact = currentUser?.phone ?: ""
            )

            val result = productRepository.uploadProduct(product, _selectedImages.value)
            _uploadState.value = result.fold(
                onSuccess = {
                    _selectedImages.value = emptyList()
                    UploadState.Success("Product uploaded successfully!")
                },
                onFailure = { UploadState.Error(it.message ?: "Upload failed") }
            )
        }
    }

    fun resetState() {
        _uploadState.value = UploadState.Idle
    }
}
