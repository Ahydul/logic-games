package com.example.tfg.ui.components.common

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection


class ClippedRectangleShape(
    private val widthPercentage: Float = 1f,
    private val heightPercentage: Float = 1f,
    private val minWidth: Int = 0,
    private val minHeight: Int = 0,
    private val leftToRight: Boolean = true,
    private val upToDown: Boolean = true,
): Shape {

    init {
        require(widthPercentage in 0.0..1.0)
        require(heightPercentage in 0.0..1.0)
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        var res = size.toRect()
        val width = res.width * widthPercentage

        res = if (leftToRight){
            res.copy(left = res.right - width.coerceAtLeast(minWidth.toFloat()))
        } else{
            res.copy(right = res.left + width.coerceAtLeast(minWidth.toFloat()))
        }

        val height = res.height * (1-heightPercentage)

        res = if (upToDown){
            res.copy(top = res.top + height.coerceAtMost(minHeight.toFloat()))
        } else{
            res.copy(bottom = res.bottom - height.coerceAtLeast(minHeight.toFloat()))
        }

        return Outline.Rectangle(res)
    }

    override fun toString(): String = "RectangleShape"
}

val LeftSemiCircleShape = RoundedCornerShape(CornerSize(50),CornerSize(0),CornerSize(0),CornerSize(50))
val RightSemiCircleShape = RoundedCornerShape(CornerSize(0),CornerSize(50),CornerSize(50),CornerSize(0))
