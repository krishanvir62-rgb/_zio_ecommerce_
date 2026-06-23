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

sealed class UploadProductState {
    data object Idle : UploadProductState()
    data object Loading : UploadProductState()
    data class Success(val message: String) : UploadProductState()
    data class Error(val message: String) : UploadProductState()
}

@HiltViewModel
class UploadProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadProductState>(UploadProductState.Idle)
    val uploadState: StateFlow<UploadProductState> = _uploadState.asStateFlow()

    fun uploadProduct(name: String, description: String, price: String, category: String, uris: List<Uri>) {
        if (name.isBlank() || price.isBlank() || description.isBlank() || uris.size < 3) {
            _uploadState.value = UploadProductState.Error("Please fill all details and select at least 3 images.")
            return
        }

        val priceValue = price.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0) {
            _uploadState.value = UploadProductState.Error("Invalid price.")
            return
        }

        _uploadState.value = UploadProductState.Loading

        viewModelScope.launch {
            val user = authRepository.getCurrentUserInfo()
            val product = Product(
                name = name,
                description = description,
                price = priceValue,
                category = category,
                uploaderId = user?.id ?: "",
                uploaderName = user?.name ?: "Unknown Seller",
                uploaderContact = user?.email ?: ""
            )

            val result = productRepository.uploadProduct(product, uris)
            result.fold(
                onSuccess = {
                    _uploadState.value = UploadProductState.Success("Product uploaded successfully!")
                },
                onFailure = {
                    _uploadState.value = UploadProductState.Error(it.message ?: "Failed to upload product.")
                }
            )
        }
    }

    fun resetState() {
        _uploadState.value = UploadProductState.Idle
    }
}
