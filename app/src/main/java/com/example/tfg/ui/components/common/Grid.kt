package com.example.tfg.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import kotlin.math.min

@Composable
fun GridLayout(
    modifier: Modifier = Modifier,
    numRows: Int = 2,
    placement: GridPlacement = GridPlacement.HORIZONTAL,
    verticalSpreadFactor: Float = 1f,
    horizontalSpreadFactor: Float = 1f,
    componentsScale: Float = 1f,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val numChildrenPerRow = measurables.size / numRows + if (measurables.size % numRows == 0) 0 else 1

        if (numChildrenPerRow==0) return@Layout layout(0,0) {}

        val childrenSize = (min(layoutWidth / numChildrenPerRow, layoutHeight / numRows) * componentsScale).toInt()

        val itemConstraints = constraints.copy(
            minHeight = childrenSize,
            maxHeight = childrenSize,
            maxWidth = childrenSize,
            minWidth = childrenSize
        )

        val placeables = measurables.map { it.measure(itemConstraints) }

        val horizontalFreeSpace = layoutWidth - numChildrenPerRow*childrenSize
        val horizontalSpaceBetween = if (numChildrenPerRow == 1) 0
            else ((horizontalFreeSpace / (numChildrenPerRow - 1)) * horizontalSpreadFactor).toInt()
        val initialHorizontalSpace = (horizontalFreeSpace - (numChildrenPerRow - 1) * horizontalSpaceBetween ) / 2

        val verticalFreeSpace = layoutHeight - numRows*childrenSize
        val verticalSpaceBetween = if (numRows == 1) 0
            else ((verticalFreeSpace / (numRows - 1)) * verticalSpreadFactor).toInt()
        val initialVerticalSpace = (verticalFreeSpace - (numRows - 1) * verticalSpaceBetween ) / 2

        layout(
            width = layoutWidth,
            height = layoutHeight,
        ) {
            val rowX = Array(numRows) { initialHorizontalSpace }

            placeables.forEachIndexed { index, placeable ->
                val row = if (placement == GridPlacement.HORIZONTAL) index / numChildrenPerRow
                            else index % numRows
                val verticalSpace = if (row == 0) 0 else verticalSpaceBetween
                placeable.placeRelative(
                    x = rowX[row],
                    y = row * (childrenSize + verticalSpace) + initialVerticalSpace
                )
                rowX[row] += placeable.width + horizontalSpaceBetween
            }
        }
    }
}