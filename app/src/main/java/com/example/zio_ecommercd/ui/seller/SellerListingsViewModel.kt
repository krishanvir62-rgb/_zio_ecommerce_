package com.example.zio_ecommercd.ui.seller

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

sealed class SellerListingsState {
    object Loading : SellerListingsState()
    data class Success(val products: List<Product>) : SellerListingsState()
    data class Error(val message: String) : SellerListingsState()
}

@HiltViewModel
class SellerListingsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SellerListingsState>(SellerListingsState.Loading)
    val uiState: StateFlow<SellerListingsState> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow<Boolean?>(null)
    val deleteState: StateFlow<Boolean?> = _deleteState.asStateFlow()

    init {
        loadListings()
    }

    fun loadListings() {
        viewModelScope.launch {
            _uiState.value = SellerListingsState.Loading
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                productRepository.getProductsBySellerIdStream(currentUser.uid).collect { result ->
                    if (result.isSuccess) {
                        _uiState.value = SellerListingsState.Success(result.getOrDefault(emptyList()))
                    } else {
                        _uiState.value = SellerListingsState.Error(result.exceptionOrNull()?.message ?: "Failed to load listings")
                    }
                }
            } else {
                _uiState.value = SellerListingsState.Error("User not logged in")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            val result = productRepository.deleteProduct(productId)
            if (result.isSuccess) {
                _deleteState.value = true
                loadListings() // Reload after deletion
            } else {
                _deleteState.value = false
            }
        }
    }
    
    fun resetDeleteState() {
        _deleteState.value = null
    }
}
