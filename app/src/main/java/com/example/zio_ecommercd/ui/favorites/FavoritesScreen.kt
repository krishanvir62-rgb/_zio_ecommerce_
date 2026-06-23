package com.example.zio_ecommercd.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.zio_ecommercd.data.local.FavoriteEntity
import com.example.zio_ecommercd.ui.theme.BrandPrimary
import com.example.zio_ecommercd.ui.theme.BrandSecondary
import com.example.zio_ecommercd.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onProductClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val favorites by viewModel.favorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            .padding(contentPadding)
    ) {
        // Premium Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                Text(text = "My Favorites", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary)
                Text(text = "${favorites.size} items saved", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f))
            }
        }

        var isRefreshing by remember { mutableStateOf(false) }

        if (isRefreshing) {
            LaunchedEffect(true) {
                kotlinx.coroutines.delay(600)
                isRefreshing = false
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            modifier = Modifier.fillMaxSize()
        ) {
            if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "No favorites yet", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tap the heart on products to save them here", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favorites, key = { it.productId }) { favorite ->
                    FavoriteItem(favorite = favorite, onClick = { onProductClick(favorite.productId) }, onRemove = { viewModel.removeFavorite(favorite.productId) })
                }
            }
            }
        }
    }
}

@Composable
private fun FavoriteItem(favorite: FavoriteEntity, onClick: () -> Unit, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0F4F8)),
                contentAlignment = Alignment.Center
            ) {
                if (favorite.imageUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(favorite.imageUrl)
                            .crossfade(true)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = favorite.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) } },
                        error = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(32.dp)) } }
                    )
                } else {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = favorite.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.background(BrandPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(text = favorite.category.ifEmpty { "General" }.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = BrandPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "₹${favorite.price.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandPrimary)
            }

            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFF0F0))) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}





