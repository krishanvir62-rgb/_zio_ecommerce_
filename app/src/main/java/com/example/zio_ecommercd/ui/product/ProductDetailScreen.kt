package com.example.zio_ecommercd.ui.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.zio_ecommercd.data.model.Review
import com.example.zio_ecommercd.ui.cart.CartViewModel
import com.example.zio_ecommercd.ui.theme.BrandPrimary
import com.example.zio_ecommercd.ui.theme.BrandSecondary
import com.example.zio_ecommercd.ui.theme.SuccessGreen
import com.example.zio_ecommercd.ui.theme.WarningYellow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    cartViewModel: CartViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val canReview by viewModel.canReview.collectAsState()
    val userReview by viewModel.userReview.collectAsState()
    val reviewSubmitting by viewModel.reviewSubmitting.collectAsState()
    var addedToCart by remember { mutableStateOf(false) }

    val cartButtonColor by animateColorAsState(targetValue = if (addedToCart) SuccessGreen else BrandPrimary, animationSpec = tween(400), label = "cartColor")
    val cartButtonElevation by animateDpAsState(targetValue = if (addedToCart) 2.dp else 16.dp, animationSpec = tween(400), label = "cartElevation")

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.secondary) }
                }
                Text(text = "Details", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = if (isFavorite) BrandPrimary else BrandSecondary, modifier = Modifier.size(24.dp))
                    }
                }
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
            when (val state = uiState) {
                is ProductDetailState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrandPrimary, strokeWidth = 3.dp) }
                }
                is ProductDetailState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = state.message, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                is ProductDetailState.Success -> {
                    val product = state.product
                    val coroutineScope = rememberCoroutineScope()
                    val allImages = if (product.images.isNotEmpty()) product.images else if (product.imageUrl.isNotEmpty()) listOf(product.imageUrl) else emptyList()
                    val pagerState = rememberPagerState(pageCount = { allImages.size })

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        
                        // Image Carousel
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).height(320.dp).shadow(4.dp, RoundedCornerShape(32.dp)).clip(RoundedCornerShape(32.dp)).background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (allImages.isNotEmpty()) {
                                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current).data(allImages[page]).crossfade(true).build(),
                                        contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)),
                                        loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(32.dp), strokeWidth = 2.dp) } },
                                        error = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp)) } }
                                    )
                                }
                            } else {
                                Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            }
                        }

                        // Dots and Thumbnails
                        if (allImages.size > 1) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.Center) {
                                repeat(allImages.size) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier.padding(horizontal = 4.dp).size(if (isSelected) 10.dp else 8.dp).clip(CircleShape).background(if (isSelected) BrandPrimary else BrandPrimary.copy(alpha = 0.3f))
                                    )
                                }
                            }
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(allImages) { index, imageUrl ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) BrandPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current).data(imageUrl).crossfade(true).build(),
                                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                            }
                        }

                        // Product Details Content
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 24.dp, vertical = 32.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.background(BrandPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                                    Text(text = product.category.ifEmpty { "General" }.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = BrandPrimary)
                                }
                                if (product.ratingCount > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = String.format(Locale.US, "%.1f", product.rating), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                                        Text(text = " (${product.ratingCount})", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = product.name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary, lineHeight = 32.sp)

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "₹${product.price.toInt()}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = BrandPrimary)

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(text = "Description", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = product.description, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp, fontWeight = FontWeight.Medium)

                            Spacer(modifier = Modifier.height(32.dp))
                            Box(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(20.dp)) {
                                Column {
                                    Text(text = "Seller Information", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(text = product.uploaderName.ifEmpty { "Verified Seller" }, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                                            if (product.uploaderContact.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(text = product.uploaderContact, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(text = "Ratings & Reviews", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (canReview || userReview != null) {
                                ReviewInputSection(
                                    existingReview = userReview,
                                    onSubmit = { rating, text -> viewModel.submitReview(rating, text) },
                                    isSubmitting = reviewSubmitting
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (reviews.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                    Text(text = "No reviews yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                }
                            } else {
                                reviews.forEach { review ->
                                    ReviewItem(review = review)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(cartButtonElevation, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cartButtonColor)
                                    .clickable { if (!addedToCart) { cartViewModel.addToCart(product); addedToCart = true } }
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = if (addedToCart) Icons.Default.CheckCircle else Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = if (addedToCart) "Added to Cart" else "Add to Cart", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewInputSection(existingReview: Review?, onSubmit: (Int, String) -> Unit, isSubmitting: Boolean = false) {
    var rating by remember { mutableIntStateOf(existingReview?.rating ?: 0) }
    var reviewText by remember { mutableStateOf(existingReview?.reviewText ?: "") }
    var isEditing by remember { mutableStateOf(existingReview == null) }

    // When existingReview changes (e.g. after Firestore refresh), sync local state
    LaunchedEffect(existingReview) {
        if (existingReview != null) {
            rating = existingReview.rating
            reviewText = existingReview.reviewText
            isEditing = false
        } else {
            isEditing = true
        }
    }

    Box(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(20.dp)) {
        if (!isEditing && existingReview != null) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Your Review", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                    TextButton(onClick = { isEditing = true }) {
                        Text(text = "Edit", color = BrandPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(5) { index ->
                        Icon(imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(20.dp))
                    }
                }
                if (reviewText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = reviewText, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column {
                Text(text = if (existingReview != null) "Edit Your Review" else "Write a Review", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = WarningYellow,
                            modifier = Modifier.size(40.dp).clickable { rating = index + 1 }.padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Write your experience (optional)...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onSubmit(rating, reviewText)
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = rating > 0 && !isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (existingReview != null) "Update Review" else "Submit Review", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateString = if (review.timestamp > 0) dateFormat.format(Date(review.timestamp)) else ""

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (review.userPhotoUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(review.userPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = review.userName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        loading = {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BrandPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            }
                        },
                        error = {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BrandPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Text(text = review.userName.take(1).uppercase(), color = BrandPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(BrandPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Text(text = review.userName.take(1).uppercase(), color = BrandPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = review.userName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
            Text(text = dateString, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (review.reviewText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = review.reviewText, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
    }
}

