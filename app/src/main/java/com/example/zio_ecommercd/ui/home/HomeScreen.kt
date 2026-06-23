package com.example.zio_ecommercd.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.zio_ecommercd.data.model.UpcomingSale
import com.example.zio_ecommercd.ui.components.ProductCard
import com.example.zio_ecommercd.ui.theme.BrandAccent
import com.example.zio_ecommercd.ui.theme.BrandPrimary
import com.example.zio_ecommercd.ui.theme.BrandSecondary
import com.example.zio_ecommercd.ui.theme.ShimmerBase
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onProductClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val upcomingSales by viewModel.upcomingSales.collectAsState()
    val notificationCount by viewModel.notificationCount.collectAsState()

    val categories = listOf("All", "Electronics", "Clothing", "Shoes", "Watches", "Accessories")
    var selectedCategory by remember { mutableStateOf("All") }
    
    var isRefreshing by remember { mutableStateOf(false) }

    if (isRefreshing) {
        LaunchedEffect(true) {
            // Because we use streams, we just wait a bit for visual feedback
            // and the flow handles actual updates instantly
            delay(800)
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
        modifier = Modifier.fillMaxSize().padding(contentPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
        ) {
        // ── Overlapping Header & Search Bar ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp) // Height includes the header and the overlapping search bar
        ) {
            // Dark Premium Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.secondary)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = (-40).dp, y = (-20).dp)
                        .size(150.dp)
                        .background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)))
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onProfileClick() }
                    ) {
                        // Profile photo
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(2.dp, BrandAccent, CircleShape)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUrl = currentUser?.photoUrl?.takeIf { it.isNotEmpty() }
                            if (photoUrl != null) {
                                SubcomposeAsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Welcome,", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = currentUser?.name?.split(" ")?.firstOrNull() ?: "Guest",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // Notification bell with badge
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.15f))
                                .clickable {
                                    viewModel.clearNotificationBadge()
                                    onNotificationClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        if (notificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 2.dp, end = 2.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimary)
                                    .border(2.dp, BrandSecondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (notificationCount > 9) "9+" else "$notificationCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Floating Search Bar overlapping the boundary
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearch(it) },
                    placeholder = { Text("Search for products...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = BrandPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Category chips ──
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1f, label = "chipScale")
                
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isSelected) BrandPrimary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isSelected) Color.Transparent else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .clickable {
                            selectedCategory = category
                            viewModel.onSearch(if (category == "All") "" else category)
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Content ──
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) { items(6) { ShimmerProductCard() } }
            }
            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Oops! Something went wrong", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                        Text(state.message, color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            is HomeUiState.Success -> {
                if (state.products.isEmpty() && upcomingSales.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No products found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                            Text("Try a different search or category", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp), // Extra padding for bottom nav
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Sale Banners
                        if (upcomingSales.isNotEmpty() && searchQuery.isBlank()) {
                            item(span = { GridItemSpan(2) }) {
                                Column {
                                    SaleBannerCarousel(sales = upcomingSales)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        // Products Header
                        if (state.products.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Popular Deals", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                                    Text("See All", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                                }
                            }
                        }

                        // Product Grid
                        items(state.products, key = { it.id }) { product ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { visible = true }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                            ) {
                                ProductCard(product = product, onClick = { onProductClick(product.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun SaleBannerCarousel(sales: List<UpcomingSale>) {
    Column {
        if (sales.size == 1) {
            SaleBannerCard(sale = sales[0])
        } else {
            val pagerState = rememberPagerState(pageCount = { sales.size })
            LaunchedEffect(pagerState) {
                while (true) {
                    delay(4000)
                    pagerState.animateScrollToPage((pagerState.currentPage + 1) % sales.size, animationSpec = tween(800))
                }
            }
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(end = 40.dp),
                pageSpacing = 16.dp
            ) { page -> SaleBannerCard(sale = sales[page]) }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                sales.forEachIndexed { index, _ ->
                    val isActive = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isActive) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) BrandPrimary else Color.LightGray)
                    )
                }
            }
        }
    }
}

@Composable
private fun SaleBannerCard(sale: UpcomingSale) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(BrandPrimary, BrandAccent), start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))
    ) {
        Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 30.dp, y = (-30).dp).size(120.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)))
        Box(modifier = Modifier.align(Alignment.BottomStart).offset(x = (-20).dp, y = 20.dp).size(80.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) { Text("🔥 MEGA SALE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(sale.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(sale.description, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.25f))
                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("UP TO ${sale.discountPercent}% OFF", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
private fun ShimmerProductCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.8f, animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "shimmerAlpha")
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(androidx.compose.material3.MaterialTheme.colorScheme.surface).padding(12.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).background(ShimmerBase.copy(alpha = shimmerAlpha)))
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.width(60.dp).height(14.dp).clip(RoundedCornerShape(12.dp)).background(ShimmerBase.copy(alpha = shimmerAlpha)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(18.dp).clip(RoundedCornerShape(8.dp)).background(ShimmerBase.copy(alpha = shimmerAlpha)))
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(8.dp)).background(ShimmerBase.copy(alpha = shimmerAlpha)))
        }
    }
}







