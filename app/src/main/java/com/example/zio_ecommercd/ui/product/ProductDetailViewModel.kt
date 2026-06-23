package com.example.zio_ecommercd.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zio_ecommercd.data.model.Product
import com.example.zio_ecommercd.data.model.Review
import com.example.zio_ecommercd.data.repository.AuthRepository
import com.example.zio_ecommercd.data.repository.FavoriteRepository
import com.example.zio_ecommercd.data.repository.OrderRepository
import com.example.zio_ecommercd.data.repository.ProductRepository
import com.example.zio_ecommercd.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProductDetailState {
    data object Loading : ProductDetailState()
    data class Success(val product: Product) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository,
    private val reviewRepository: ReviewRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val productId: String = savedStateHandle.get<String>("productId") ?: ""

    private val _uiState = MutableStateFlow<ProductDetailState>(ProductDetailState.Loading)
    val uiState: StateFlow<ProductDetailState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _canReview = MutableStateFlow(false)
    val canReview: StateFlow<Boolean> = _canReview.asStateFlow()

    private val _userReview = MutableStateFlow<Review?>(null)
    val userReview: StateFlow<Review?> = _userReview.asStateFlow()

    private val _reviewSubmitting = MutableStateFlow(false)
    val reviewSubmitting: StateFlow<Boolean> = _reviewSubmitting.asStateFlow()

    init {
        loadProduct()
        observeFavorite()
        loadReviews()
        checkCanReview()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            productRepository.getProductByIdStream(productId).collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { ProductDetailState.Success(it) },
                    onFailure = { ProductDetailState.Error(it.message ?: "Failed to load product") }
                )
            }
        }
    }

    private fun observeFavorite() {
        viewModelScope.launch {
            favoriteRepository.isFavorite(productId).collect { fav ->
                _isFavorite.value = fav
            }
        }
    }

    /**
     * Stream reviews from Firestore in real-time.
     * Also extracts the current user's review from the list.
     */
    private fun loadReviews() {
        viewModelScope.launch {
            reviewRepository.getReviewsForProductStream(productId).collect { result ->
                if (result.isSuccess) {
                    val allReviews = result.getOrDefault(emptyList())
                    _reviews.value = allReviews

                    // Find current user's review from the stream
                    val currentUser = authRepository.currentUser
                    if (currentUser != null) {
                        _userReview.value = allReviews.find { it.userId == currentUser.uid }
                    }
                }
            }
        }
    }

    /**
     * Check if user has purchased this product (needed to allow review).
     */
    private fun checkCanReview() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                _canReview.value = orderRepository.hasUserPurchasedProduct(currentUser.uid, productId)
            } else {
                _canReview.value = false
            }
        }
    }

    /**
     * Submit or update the user's review.
     * Repository handles duplicate prevention — one review per user per product.
     */
    fun submitReview(rating: Int, reviewText: String) {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser ?: return@launch
            if (rating <= 0) return@launch

            _reviewSubmitting.value = true
            try {
                val existingReview = _userReview.value

                val reviewToSubmit = Review(
                    id = existingReview?.id ?: "",
                    productId = productId,
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: "User",
                    rating = rating,
                    reviewText = reviewText.trim(),
                    createdAt = existingReview?.createdAt ?: 0L,
                    updatedAt = 0L
                )

                reviewRepository.submitReview(reviewToSubmit)
            } finally {
                _reviewSubmitting.value = false
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProductDetailState.Success) {
                if (_isFavorite.value) {
                    favoriteRepository.removeFavorite(productId)
                } else {
                    favoriteRepository.addFavorite(currentState.product)
                }
            }
        }
    }
}
