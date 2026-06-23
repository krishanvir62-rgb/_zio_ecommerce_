package com.example.zio_ecommercd.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.zio_ecommercd.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        // Show the splash screen for 2.5 seconds
        delay(2500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        // Full screen Custom UI Image
        Image(
            painter = painterResource(id = R.drawable.splash_branding),
            contentDescription = "Custom Splash Screen UI",
            contentScale = ContentScale.Crop, // Fills the entire screen
            modifier = Modifier.fillMaxSize()
        )
    }
}

