package com.example.zio_ecommercd.ui.cart

import android.app.Activity
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.zio_ecommercd.MainActivity
import com.example.zio_ecommercd.data.local.CartEntity
import com.example.zio_ecommercd.ui.theme.BrandPrimary
import com.example.zio_ecommercd.ui.theme.BrandSecondary
import com.example.zio_ecommercd.ui.theme.ErrorRed

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    contentPadding: PaddingValues = PaddingValues()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val isPlacingOrder by viewModel.isPlacingOrder.collectAsState()
    val orderResult by viewModel.orderResult.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()

    val subtotal = viewModel.getSubtotal()
    val delivery = viewModel.getDeliveryCharge()
    val total = viewModel.getTotal()

    val activity = LocalContext.current as? Activity

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                MainActivity.pendingPhonePeResult?.let { result ->
                    MainActivity.pendingPhonePeResult = null
                    if (result.success) {
                        viewModel.onPhonePePaymentSuccess(result.transactionId)
                    } else {
                        viewModel.onPhonePePaymentError("Payment was not completed")
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    orderResult?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearOrderResult() },
            title = { Text(text = if (message.contains("success", ignoreCase = true)) "Order Confirmed" else "Order Failed", fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary) },
            text = { Text(message, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) },
            confirmButton = { TextButton(onClick = { viewModel.clearOrderResult() }) { Text("OK", color = BrandPrimary, fontWeight = FontWeight.Bold) } },
            shape = RoundedCornerShape(24.dp)
        )
    }

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
                Text(text = "Shopping Cart", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(text = "${cartItems.size} items", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Your cart is empty", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Looks like you haven't added anything yet.", fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cartItems, key = { it.productId }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { viewModel.updateQuantity(item.productId, item.quantity + 1) },
                        onDecrease = { viewModel.updateQuantity(item.productId, item.quantity - 1) },
                        onRemove = { viewModel.removeItem(item.productId) }
                    )
                }
            }

            // Elevated Billing Bottom Sheet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(text = "Payment Method", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentMethodChip(label = "COD", icon = Icons.Default.LocalShipping, isSelected = selectedPaymentMethod == "COD", onClick = { viewModel.selectPaymentMethod("COD") })
                        PaymentMethodChip(label = "UPI", icon = Icons.Default.Payment, isSelected = selectedPaymentMethod == "UPI", onClick = { viewModel.selectPaymentMethod("UPI") })
                        PaymentMethodChip(label = "Card", icon = Icons.Default.CreditCard, isSelected = selectedPaymentMethod == "Card", onClick = { viewModel.selectPaymentMethod("Card") })
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    BillingRow(label = "Subtotal", value = "₹${subtotal.toInt()}")
                    Spacer(modifier = Modifier.height(8.dp))
                    BillingRow(label = "Delivery", value = "₹${delivery.toInt()}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.height(12.dp))
                    BillingRow(label = "Total", value = "₹${total.toInt()}", isBold = true)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Action Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BrandPrimary)
                            .clickable(enabled = !isPlacingOrder, onClick = { if (selectedPaymentMethod == "COD") { viewModel.placeOrder("COD") } else { activity?.let { viewModel.initiatePayment(it) } } })
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPlacingOrder) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = if (selectedPaymentMethod == "COD") "Place Order" else "Pay Now", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(60.dp)) // Nav bar padding
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodChip(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BrandPrimary.copy(alpha = 0.1f) else androidx.compose.material3.MaterialTheme.colorScheme.background)
            .border(width = if (isSelected) 2.dp else 0.dp, color = if (isSelected) BrandPrimary else Color.Transparent, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) BrandPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) BrandPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CartItemCard(item: CartEntity, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0F4F8)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).crossfade(true).build(), contentDescription = item.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)))
                } else {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.category.ifEmpty { "General" }.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = BrandPrimary.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "₹${(item.price * item.quantity).toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandPrimary)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFFF0F0))) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(androidx.compose.material3.MaterialTheme.colorScheme.background).clickable(onClick = onDecrease), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    }
                    Box(modifier = Modifier.width(32.dp).height(32.dp).clip(RoundedCornerShape(8.dp)).background(Color.Transparent), contentAlignment = Alignment.Center) {
                        Text(text = "${item.quantity}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.secondary)
                    }
                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(BrandPrimary.copy(alpha = 0.1f)).clickable(onClick = onIncrease), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = BrandPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BillingRow(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = if (isBold) 18.sp else 14.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium, color = if (isBold) BrandSecondary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = if (isBold) 22.sp else 16.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold, color = if (isBold) BrandPrimary else BrandSecondary)
    }
}





