package com.example.tfg.ui.components.mainactivity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tfg.ui.components.common.MainHeader
import com.example.tfg.ui.components.common.NavigationBar

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .fillMaxHeight()
        .padding(top = 10.dp)
    ) {
        MainHeader(modifier = modifier.weight(1f))
        Spacer(modifier = modifier.weight(8f))
        ContinueButtons(modifier = modifier.weight(5f))
        Spacer(modifier = modifier.weight(4f))
        NavigationBar(modifier = modifier.weight(2f))
    }
}
