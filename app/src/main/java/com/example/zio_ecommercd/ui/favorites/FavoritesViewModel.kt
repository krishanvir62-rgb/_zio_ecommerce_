package com.example.zio_ecommercd.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zio_ecommercd.data.local.FavoriteEntity
import com.example.zio_ecommercd.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntity>> = favoriteRepository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshStaleFavorites()
    }

    private fun refreshStaleFavorites() {
        viewModelScope.launch {
            val list = favoriteRepository.getAllFavorites().first()
            list.filter { it.imageUrl.isEmpty() }.forEach { fav ->
                favoriteRepository.refreshFavoriteImage(fav.productId)
            }
        }
    }

    fun removeFavorite(productId: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(productId)
        }
    }
}
