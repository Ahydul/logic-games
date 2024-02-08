package com.example.tfg.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import kotlin.math.min

@Composable
fun HorizontalGrid(
    modifier: Modifier = Modifier,
    rows: Int = 2,
    placement: GridPlacement = GridPlacement.HORIZONTAL,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val numChildrenRow = measurables.size / rows + if (measurables.size % rows == 0) 0 else 1

        if (numChildrenRow==0) return@Layout layout(0,0) {}

        val childrenSize = min(layoutWidth / numChildrenRow, layoutHeight / rows)

        val itemConstraints = constraints.copy(
            minHeight = childrenSize,
            maxHeight = childrenSize,
            maxWidth = childrenSize,
            minWidth = childrenSize
        )

        val placeables = measurables.map { it.measure(itemConstraints) }

        val horizontalSpaceBetween = (layoutWidth - numChildrenRow*childrenSize) / (numChildrenRow - 1)
        val verticalSpaceBetween = (layoutHeight - rows*childrenSize) / rows

        layout(
            width = layoutWidth,
            height = layoutHeight,
        ) {
            val rowX = Array(rows) { 0 }

            placeables.forEachIndexed { index, placeable ->
                val row = if (placement == GridPlacement.HORIZONTAL) {
                    index / numChildrenRow
                }else {
                    index % rows
                }
                placeable.placeRelative(
                    x = rowX[row],
                    y = row * childrenSize + verticalSpaceBetween
                )
                rowX[row] += placeable.width + horizontalSpaceBetween
            }
        }
    }
}