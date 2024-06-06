package com.example.tfg.ui.components.common

import android.graphics.Bitmap
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

fun Modifier.addDebugBorder(): Modifier = this.then(
    Modifier.border(color = Color.Red, width = 1.dp)
)

val defaultBitmap = {
    val default = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
    default.eraseColor(Color.Gray.toArgb())
    default
}