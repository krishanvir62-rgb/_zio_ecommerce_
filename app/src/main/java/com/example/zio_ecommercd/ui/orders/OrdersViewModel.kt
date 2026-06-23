package com.example.zio_ecommercd.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zio_ecommercd.data.model.Order
import com.example.zio_ecommercd.data.repository.AuthRepository
import com.example.zio_ecommercd.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrdersUiState {
    data object Loading : OrdersUiState()
    data class Success(val orders: List<Order>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading
            val userId = authRepository.currentUser?.uid
            if (userId == null) {
                _uiState.value = OrdersUiState.Error("Not logged in")
                return@launch
            }
            orderRepository.getOrdersForUserStream(userId).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { OrdersUiState.Success(it) },
                    onFailure = { OrdersUiState.Error(it.message ?: "Failed to load orders") }
                )
            }
        }
    }
}
