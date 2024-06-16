package com.example.tfg.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF20272D),
    secondary = Color(0xFF29313A),
    tertiary = Color(0xFF3F6AA3),
    background = Color(0xFF14181C),
    surfaceVariant = Color(0xFF14181C),
    outline = Color(0xFF3E3E46),
    error = Color(0xFF890A0A),
    outlineVariant = Color(0xFF000000),
    //Text
    onPrimary = Color(0xFFBFC0CB),
    onSecondary = Color(0xFFBFC0CB),
    onBackground = Color(0xFFBFC0CB),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF528bb3),
    secondary = Color(0xFF98c1dd),
    tertiary = Color(0xFFD2E3E7),
    background = Color(0xFFf6fafb),
    surfaceVariant = Color(0xFFDDF2FF),
    outline = Color(0xFF5ba4d8),
    error = Color(0xFF890A0A),
    outlineVariant = Color(0xFF49596A),
    //Text
    onPrimary = Color(0xFF12232B),
    onSecondary = Color(0xFF060c0f),
    onBackground = Color(0xFF242A2A),
)

enum class Theme {
    DARK_MODE,
    LIGHT_MODE;

    companion object {
        fun from(str: String?): Theme {
            return when(str) {
                DARK_MODE.name -> DARK_MODE
                LIGHT_MODE.name -> LIGHT_MODE
                else -> LIGHT_MODE
            }
        }
    }
}

@Composable
fun TFGTheme(
    theme: Theme = Theme.LIGHT_MODE,
    content: @Composable () -> Unit
) {
    val colorScheme = when(theme) {
        Theme.LIGHT_MODE -> LightColorScheme
        Theme.DARK_MODE -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}