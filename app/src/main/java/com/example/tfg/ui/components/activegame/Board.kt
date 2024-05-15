package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import com.example.tfg.R
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.HorizontalGrid

@Composable
fun Board(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    Log.d("recomposition", "BOARD recomposition")

    val numColumns = viewModel.getNumColumns()
    val numRows = viewModel.getNumRows()

    Box(
        modifier = modifier
            //Tap -> removes all selections and selects the cell
            //Long press -> keeps selections. Selects or deselects the cell if cell was deselected or selected
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        Log.d("Gesture", "LongPress $it $size")
                        viewModel.setSelection(
                            position = it,
                            size = size,
                        )
                    },
                    onTap = {
                        Log.d("Gesture", "Tap $it $size")
                        viewModel.setSelection(
                            position = it,
                            size = size,
                            removePrevious = true,
                        )
                    }
                )
            }
            //Normal drag -> removes all selections and selects the cells
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        Log.d("Gesture", "DragStart $it $size")
                        viewModel.removeSelections()
                    },
                    onDrag = { change, _ ->
                        Log.d("Gesture", "Drag ${change.position} $size")
                        viewModel.selectTile(
                            position = change.position,
                            size = size,
                        )
                    }
                )
            }
            //Long press drag -> keeps selections. Select or deselect the cells depending if first cell was deselected or selected
            .pointerInput(Unit) {
                var selecting = true
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        Log.d("Gesture", "onLongDragStart $offset $size")
                        //True -> Action=select ; False -> Action=deselect
                        selecting = !viewModel.isSelected(size = size, position = offset)
                    },
                    onDrag = { change, _ ->
                        Log.d("Gesture", "onLongDrag ${change.position} $size")
                        if (selecting) viewModel.selectTile(position = change.position, size = size)
                        else viewModel.deselectTile(position = change.position, size = size)
                    }
                )
            }


    ) {
        val borderColor = colorResource(id = R.color.section_border)
        HorizontalGrid(
            numRows = numRows,
            horizontalSpreadFactor = 0f,
            verticalSpreadFactor = 0f
        ) {
            (0..< viewModel.getNumCells()).forEach { index ->
                val coordinate = Coordinate.fromIndex(index = index, numRows = numRows, numColumns = numColumns)
                val cell = viewModel.getCell(coordinate)

                Cell (
                    cell = cell,
                    borderColor = borderColor,
                    dividersToDraw = remember { viewModel.dividersToDraw(coordinate) },
                    isSelected = remember { { viewModel.isTileSelected(coordinate) } },
                )
            }
        }
    }

}
