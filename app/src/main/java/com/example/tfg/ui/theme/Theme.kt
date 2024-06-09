package com.example.tfg.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF20272D),
    secondary = Color(0xFF29313A),
    background = Color(0xFF14181C),
    outline = Color(0xFF3E3E46),
    error = Color(0xFF890A0A),
    outlineVariant = Color(0xFF000000),
    //Text
    onPrimary = Color(0xFFBFC0CB),
    onSecondary = Color(0xFFBFC0CB),
    onBackground = Color(0xFFBFC0CB),

)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF20272D),
    secondary = Color(0xFF29313A),
    background = Color(0xFF14181C),
    outline = Color(0xFF3E3E46),
    error = Color(0xFF890A0A),
    outlineVariant = Color(0xFF000000),
    //Text
    onPrimary = Color(0xFFBFC0CB),
    onSecondary = Color(0xFFBFC0CB),
    onBackground = Color(0xFFBFC0CB),
)

@Composable
fun TFGTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}