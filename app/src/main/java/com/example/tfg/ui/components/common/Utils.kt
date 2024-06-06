package com.example.tfg.ui.components.common

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.addDebugBorder(): Modifier = this.then(
    Modifier.border(color = Color.Red, width = 1.dp)
)