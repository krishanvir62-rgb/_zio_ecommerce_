package com.example.zio_ecommercd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zio_ecommercd.ui.auth.AuthViewModel
import com.example.zio_ecommercd.ui.navigation.AppNavigation
import com.example.zio_ecommercd.ui.settings.SettingsViewModel
import com.example.zio_ecommercd.ui.theme.Zio_ecommercdTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        var pendingPhonePeResult: PhonePeResult? = null
    }

    data class PhonePeResult(
        val transactionId: String,
        val success: Boolean
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Subscribe to 'sales' topic for instant push notifications
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("sales")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("FCM", "Subscribed to sales topic")
                }
            }
        
        handlePhonePeCallback(intent)
        enableEdgeToEdge()
        setContent {
            // Request Notification Permission properly in Compose for Android 13+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val permissionState = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                if (permissionState != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            android.util.Log.d("FCM", "Notification permission granted")
                        }
                    }
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            Zio_ecommercdTheme(darkTheme = isDarkMode) {
                val authViewModel: AuthViewModel = hiltViewModel()
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        startDestination = if (isLoggedIn) "home" else "login",
                        activity = this@MainActivity
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePhonePeCallback(intent)
    }

    private fun handlePhonePeCallback(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "zioapp" && data.host == "payment") {
            val txnId = data.getQueryParameter("txnId")
                ?: data.getQueryParameter("transactionId")
                ?: ""
            val responseCode = data.getQueryParameter("responseCode")
            val success = responseCode == "SUCCESS" || txnId.isNotEmpty()
            pendingPhonePeResult = PhonePeResult(
                transactionId = txnId,
                success = success
            )
        }
    }
}
