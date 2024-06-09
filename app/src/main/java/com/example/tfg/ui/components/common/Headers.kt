package com.example.tfg.ui.components.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.tfg.R


@Composable
fun MainHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Spacer(modifier = Modifier.weight(1f))
        PopupMenu(
            menuBackgroundColor = MaterialTheme.colorScheme.primary,
            expandedColor = MaterialTheme.colorScheme.outline,
            dismissedColor = MaterialTheme.colorScheme.background
        ) {
            CustomFilledIconButton( /*TODO: THEMES*/
                onClick = { },
                painter =  painterResource(id = R.drawable.paint_brush),
                contentDescription = "Click me for menu",
                color = Color.Red,
            )
            CustomFilledIconButton(
                onClick = {  },
                painter =  painterResource(id = R.drawable.paint_brush),
                contentDescription = "Click me for menu",
                color = Color.Green
            )
            CustomFilledIconButton(
                onClick = {  },
                painter =  painterResource(id = R.drawable.paint_brush),
                contentDescription = "Click me for menu",
                color = Color.Magenta
            )
        }
        CustomIconButton(
            onClick = { /*TODO: Configuration menu*/ },
            painter =  painterResource(id = R.drawable.gear),
            contentDescription = "Click me for menu",
        )
    }
}
