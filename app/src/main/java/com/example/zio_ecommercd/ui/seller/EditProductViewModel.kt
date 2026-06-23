package com.example.zio_ecommercd.ui.seller

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zio_ecommercd.data.model.Product
import com.example.zio_ecommercd.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditProductState {
    object Idle : EditProductState()
    object Loading : EditProductState()
    data class Loaded(val product: Product) : EditProductState()
    object Saving : EditProductState()
    data class Success(val message: String) : EditProductState()
    data class Error(val message: String) : EditProductState()
}

@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow<EditProductState>(EditProductState.Idle)
    val uiState: StateFlow<EditProductState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = EditProductState.Loading
            val result = productRepository.getProductById(productId)
            if (result.isSuccess) {
                _uiState.value = EditProductState.Loaded(result.getOrThrow())
            } else {
                _uiState.value = EditProductState.Error(result.exceptionOrNull()?.message ?: "Failed to load product")
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = EditProductState.Saving
            val result = productRepository.updateProduct(product)
            if (result.isSuccess) {
                _uiState.value = EditProductState.Success("Product updated successfully")
            } else {
                _uiState.value = EditProductState.Error(result.exceptionOrNull()?.message ?: "Failed to update product")
            }
        }
    }
}
