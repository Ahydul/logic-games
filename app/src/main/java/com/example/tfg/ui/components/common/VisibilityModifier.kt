package com.example.tfg.ui.components.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

fun Modifier.visibility(visible: Boolean): Modifier {
    return layout{ measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(placeable.width, placeable.height) {
            if (visible) {
                // place this item in the original position
                placeable.placeRelative(0, 0)
            }
        }
    }
}