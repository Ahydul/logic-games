package com.example.tfg.state

import androidx.compose.runtime.compositionLocalOf

data class DarkTheme(val isDark: Boolean = false)

val LocalTheme = compositionLocalOf { DarkTheme() }