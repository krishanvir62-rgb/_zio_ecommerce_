package com.example.zio_ecommercd.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = OnPrimaryWhite,
    primaryContainer = Color(0xFFF8FAFC),
    onPrimaryContainer = BrandPrimary,
    secondary = BrandSecondary,
    onSecondary = OnPrimaryWhite,
    secondaryContainer = Color(0xFFF8FAFC),
    onSecondaryContainer = BrandSecondary,
    tertiary = BrandAccent,
    onTertiary = OnPrimaryWhite,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1A1A2E),
    surface = SurfaceWhite,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = Color(0xFF6B6B80),
    error = ErrorRed,
    onError = OnPrimaryWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccentAmber,
    onPrimary = Color.Black,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = DarkAccentAmber,
    secondary = DarkAccentAmber,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = DarkAccentAmber,
    tertiary = BrandAccent,
    onTertiary = Color.Black,
    background = SurfaceDark,
    onBackground = Color.White,
    surface = SurfaceDarkCard,
    onSurface = Color.White,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = Color.LightGray,
    error = ErrorRed,
    onError = OnPrimaryWhite
)

@Composable
fun Zio_ecommercdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


