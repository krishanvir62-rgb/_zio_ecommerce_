package com.example.zio_ecommercd.ui.cart

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zio_ecommercd.data.local.CartEntity
import com.example.zio_ecommercd.data.model.Product
import com.example.zio_ecommercd.data.repository.AuthRepository
import com.example.zio_ecommercd.data.repository.CartRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    companion object {
        private const val TAG = "CartViewModel"
        private const val MERCHANT_ID = "PGMDUMYY"
        private const val SALT_KEY = "099eb0cd-02cf-4e2a-8aca-3e6c6aff0399"
        private const val SALT_INDEX = "1"
        private const val PAYMENT_API_PATH = "/pg/server/v1/pay"
        private const val REDIRECT_URL = "zioapp://payment/callback"
    }

    val cartItems: StateFlow<List<CartEntity>> = cartRepository.getAllCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemCount: StateFlow<Int> = cartRepository.getCartItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedPaymentMethod = MutableStateFlow("COD")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _orderResult = MutableStateFlow<String?>(null)
    val orderResult: StateFlow<String?> = _orderResult.asStateFlow()

    private var phonePeTransactionId: String = ""

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val currentItems = cartItems.value
            val existingItem = currentItems.find { it.productId == product.id }
            if (existingItem != null) {
                cartRepository.updateQuantity(product.id, existingItem.quantity + quantity)
            } else {
                cartRepository.addToCart(product, quantity)
            }
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, quantity)
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(productId)
        }
    }

    fun getSubtotal(): Double {
        return cartItems.value.sumOf { it.price * it.quantity }
    }

    fun getDeliveryCharge(): Double = 49.0

    fun getTotal(): Double {
        return getSubtotal() + getDeliveryCharge()
    }

    fun initiatePayment(activity: Activity) {
        val method = _selectedPaymentMethod.value
        if (method == "COD") {
            placeOrder(method)
        } else {
            launchPhonePePayment(activity)
        }
    }

    private fun launchPhonePePayment(activity: Activity) {
        try {
            val transactionId = "TXN_${UUID.randomUUID()}"
            val totalInr = getTotal()
            val amountPaise = (totalInr * 100).toLong()

            val payload = JSONObject().apply {
                put("merchantId", MERCHANT_ID)
                put("merchantTransactionId", transactionId)
                put("merchantUserId", authRepository.currentUser?.uid ?: "user_${UUID.randomUUID()}")
                put("amount", amountPaise)
                put("redirectUrl", REDIRECT_URL)
                put("redirectMode", "REDIRECT")
                put("callbackUrl", REDIRECT_URL)
                put("mobileNumber",
                    authRepository.currentUser?.phoneNumber?.takeIf { it.isNotBlank() } ?: "9999999999")
                put("paymentInstrument", JSONObject().apply {
                    put("type", "PAY_PAGE")
                })
            }

            val payloadBase64 = Base64.encodeToString(
                payload.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP
            )

            val checksumInput = payloadBase64 + PAYMENT_API_PATH + SALT_KEY
            val sha256Hex = sha256Hex(checksumInput)
            val checksum = "${sha256Hex}###${SALT_INDEX}"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("phonepe://pay")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("pe_payload", payloadBase64)
                putExtra("pe_checksum", checksum)
                putExtra("pe_merchantId", MERCHANT_ID)
            }

            val phonePeInstalled = try {
                activity.packageManager.getPackageInfo("com.phonepe.app", 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }

            if (phonePeInstalled) {
                activity.startActivity(intent)
            } else {
                _orderResult.value = "PhonePe app is not installed. Please install PhonePe to proceed."
            }
        } catch (e: Exception) {
            Log.e(TAG, "PhonePe launch error", e)
            _orderResult.value = "Payment error: ${e.message}"
        }
    }

    fun onPhonePePaymentSuccess(transactionId: String) {
        phonePeTransactionId = transactionId
        placeOrder("PhonePe_Sandbox")
    }

    fun onPhonePePaymentError(error: String) {
        _orderResult.value = "Payment failed: $error"
    }

    fun placeOrder(paymentMethod: String = _selectedPaymentMethod.value) {
        viewModelScope.launch {
            _isPlacingOrder.value = true
            try {
                val userId = authRepository.currentUser?.uid ?: throw Exception("Not logged in")
                val items = cartItems.value
                if (items.isEmpty()) {
                    _orderResult.value = "Cart is empty"
                    return@launch
                }

                val isPhonePe = paymentMethod == "PhonePe_Sandbox"
                val txnId = if (isPhonePe) phonePeTransactionId else ""
                val paymentStatus = if (isPhonePe && txnId.isNotEmpty()) "SUCCESS" else "PENDING"

                val order = hashMapOf(
                    "userId" to userId,
                    "items" to items.map { item ->
                        mapOf(
                            "productId" to item.productId,
                            "name" to item.name,
                            "price" to item.price,
                            "quantity" to item.quantity,
                            "imageUrl" to item.imageUrl
                        )
                    },
                    "subtotal" to getSubtotal(),
                    "deliveryCharge" to getDeliveryCharge(),
                    "total" to getTotal(),
                    "status" to "confirmed",
                    "paymentMethod" to paymentMethod,
                    "paymentStatus" to paymentStatus,
                    "transactionId" to txnId,
                    "currency" to "INR",
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("orders").add(order).await()
                cartRepository.clearCart()
                phonePeTransactionId = ""
                _orderResult.value = "Order placed successfully!"
            } catch (e: Exception) {
                Log.e(TAG, "Place order error", e)
                _orderResult.value = "Failed: ${e.message}"
            } finally {
                _isPlacingOrder.value = false
            }
        }
    }

    fun clearOrderResult() {
        _orderResult.value = null
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
