package com.example.zio_ecommercd.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zio_ecommercd.data.model.Product
import com.example.zio_ecommercd.data.model.UpcomingSale
import com.example.zio_ecommercd.data.model.User
import com.example.zio_ecommercd.data.repository.AuthRepository
import com.example.zio_ecommercd.data.repository.ProductRepository
import com.example.zio_ecommercd.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val products: List<Product>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

    private val _upcomingSales = MutableStateFlow<List<UpcomingSale>>(emptyList())
    val upcomingSales: StateFlow<List<UpcomingSale>> = _upcomingSales.asStateFlow()

    private val _notificationCount = MutableStateFlow(0)
    val notificationCount: StateFlow<Int> = _notificationCount.asStateFlow()

    private var allProducts = listOf<Product>()

    init {
        loadProducts()
        loadUser()
        loadUpcomingSales()
    }

    fun loadUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUserInfo()
        }
    }

    fun uploadProfilePhoto(imageUri: Uri) {
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            val result = authRepository.uploadProfilePhoto(imageUri)
            result.onSuccess {
                _currentUser.value = authRepository.getCurrentUserInfo()
            }
            _isUploadingPhoto.value = false
        }
    }

    fun updatePhone(phone: String, callback: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.updatePhone(phone)
            result.onSuccess {
                _currentUser.value = authRepository.getCurrentUserInfo()
            }
            callback(result)
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            productRepository.getAllProductsStream().collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { products ->
                        allProducts = products
                        HomeUiState.Success(filterProducts(products, _searchQuery.value))
                    },
                    onFailure = { e -> HomeUiState.Error(e.message ?: "Failed to load products") }
                )
            }
        }
    }

    fun loadUpcomingSales() {
        viewModelScope.launch {
            saleRepository.getActiveSalesStream().collect { result ->
                result.onSuccess { sales ->
                    _upcomingSales.value = sales
                    val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    val readIds = prefs.getStringSet("read_sales_ids", emptySet()) ?: emptySet()
                    val unreadCount = sales.count { !readIds.contains(it.id) }
                    _notificationCount.value = unreadCount
                }
            }
        }
    }

    fun clearNotificationBadge() {
        val currentSalesIds = _upcomingSales.value.map { it.id }.toSet()
        val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val existingReadIds = prefs.getStringSet("read_sales_ids", emptySet()) ?: emptySet()
        
        prefs.edit()
            .putStringSet("read_sales_ids", existingReadIds + currentSalesIds)
            .apply()
            
        _notificationCount.value = 0
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
        _uiState.value = HomeUiState.Success(filterProducts(allProducts, query))
    }

    private fun filterProducts(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products
        return products.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }
}
