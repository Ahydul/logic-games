package com.example.tfg.ui.components.mainactivity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val mod = Modifier.fillMaxWidth()
    Column(modifier = modifier
        .fillMaxHeight()
    ) {
        Spacer(modifier = mod.weight(8f))
        ContinueButtons(modifier = mod.weight(5f))
        Spacer(modifier = mod.weight(4f))
    }
}
