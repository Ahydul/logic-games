package com.example.tfg.games

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

class CustomPainter constructor(
    private val images: List<Painter>,
    override val intrinsicSize: Size = images.first().intrinsicSize
) : Painter() {

    override fun DrawScope.onDraw() {
        for (image in images) {
        }
    }

}