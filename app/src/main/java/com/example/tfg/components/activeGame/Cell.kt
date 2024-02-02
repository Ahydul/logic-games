package com.example.tfg.components.activeGame

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.R
import com.example.tfg.common.Board


@Composable
fun Cell(
    value: Int, isSelected: Boolean,
     drawDividerDown: Boolean, drawDividerUp: Boolean,
     drawDividerRight: Boolean, drawDividerLeft: Boolean,
     modifier: Modifier = Modifier
) {
    Log.d("TAG", "CELLcurrentRecomposeScope $currentRecomposeScope")

    val borderColor = colorResource(id = R.color.section_border)
    val cellValueColor = colorResource(id = R.color.cell_value)

    Box(
        modifier = modifier
            .border(
                width = 0.2.dp,
                color = colorResource(id = R.color.board_grid)
            )
    ) {
        Text(
            text =  if (value == 0) { "" } else {value.toString()},
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.Center),
            color = cellValueColor
        )
        //Paints region borders and selecting UI
        Canvas(modifier = Modifier.matchParentSize()) {
            val borderSize = 1.dp.toPx()
            val drawDivider = { start: Offset, end: Offset ->
                drawLine(
                    color = borderColor,
                    start = start,
                    end = end,
                    strokeWidth = borderSize
                )
            }
            val drawHorizontalDivider = { y:Float ->
                drawDivider(Offset(0f, y), Offset(size.width, y))
            }
            val drawVerticalDivider = { x:Float ->
                drawDivider(Offset(x, size.width), Offset(x, 0f))
            }

            if (drawDividerUp) drawHorizontalDivider(0f + borderSize / 2)
            if (drawDividerDown) drawHorizontalDivider(size.height - borderSize / 2)
            if (drawDividerRight) drawVerticalDivider(size.width - borderSize / 2)
            if (drawDividerLeft) drawVerticalDivider(0f + borderSize / 2)

            if(isSelected){
                drawOval(color = Color.Red)
            }
        }
    }
}


//Tap -> removes all selections and selects the cell
//Long press -> keeps selections. Selects or deselects the cell if cell was deselected or selected
private fun Modifier.customTapGestures(selectedTiles: MutableList<Int>, numColumns: Int, numRows: Int): Modifier {
    return this then Modifier.pointerInput(Unit) {

        val action = { offset: Offset, resetSelects: Boolean  ->
            val index = Board.getIndex(size = size, position = offset, numColumns = numColumns, numRows = numRows)

            //True -> Action=select ; False -> Action=deselect
            val selecting = !selectedTiles.contains(index)
            Log.d("TAG", "onLongPress: $selecting")

            if (!selecting) {
                if (resetSelects) selectedTiles.removeAll { true }
                else selectedTiles.remove(index)
            }
            else selectedTiles.add(index!!)//Shoudn't be out of bounds
        }
        detectTapGestures(
            onLongPress = { offset ->
                action(offset, false)
            },
            onTap = { offset ->
                action(offset, true)
            }
        )
    }
}

//Normal drag -> removes all selections and selects the cells
private fun Modifier.customDragGestures(selectedTiles: MutableList<Int>, numColumns: Int, numRows: Int): Modifier {
    return this then Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { selectedTiles.removeAll { true } },
            onDrag = { change, _ ->
                val index = Board.getIndex(size = size, position = change.position, numColumns = numColumns, numRows = numRows)

                if (index != null && !selectedTiles.contains(index)) {
                    selectedTiles.add(index)
                }
            }
        )
    }
}

//Long press drag -> keeps selections. Select or deselect the cells depending if first cell was deselected or selected
private fun Modifier.customDragGesturesAfterLongPress(selectedTiles: MutableList<Int>, numColumns: Int, numRows: Int): Modifier {
    return this then Modifier.pointerInput(Unit) {
        //Boolean to control selecting/deselecting behaviour
        var selecting = true
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                val index = Board.getIndex(size = size, position = offset, numColumns = numColumns, numRows = numRows)

                //True -> Action=select ; False -> Action=deselect
                selecting = !selectedTiles.contains(index)
            },
            onDrag = { change, _ ->
                val index = Board.getIndex(size = size, position = change.position, numColumns = numColumns, numRows = numRows)
                if (index != null) {
                    if (selecting && !selectedTiles.contains(index))
                        selectedTiles.add(index)

                    if (!selecting && selectedTiles.contains(index))
                        selectedTiles.remove(index)
                }
            }
        )
    }
}

@Composable
fun Board(
    board: MutableState<Board>,
    selectedTiles: SnapshotStateList<Int>,
    modifier: Modifier = Modifier
) {
    Log.d("TAG", "BOARDcurrentRecomposeScope $currentRecomposeScope")

    val numColumns = board.value.numColumns
    val numRows = board.value.numRows

    Column(
        modifier = modifier
            .customTapGestures(
                selectedTiles = selectedTiles,
                numColumns = numColumns,
                numRows = numRows
            )
            .customDragGestures(
                selectedTiles = selectedTiles,
                numColumns = numColumns,
                numRows = numRows
            )
            .customDragGesturesAfterLongPress(
                selectedTiles = selectedTiles,
                numColumns = numColumns,
                numRows = numRows
            )
    ) {
        val cellModifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .background(colorResource(id = R.color.cell_background))

        for (row in 0 .. numRows - 1) {

            Row(modifier = modifier.weight(1f)) {

                for (col in 0..numColumns - 1) {

                    Cell(
                        value = board.value.getCellValue(row = row, column = col),
                        isSelected = selectedTiles.contains(board.value.indexToInt(row = row, column = col)!!),
                        drawDividerRight = board.value.drawDividerRight(row = row, column = col),
                        drawDividerDown = board.value.drawDividerDown(row = row, column = col),
                        drawDividerLeft = board.value.drawDividerLeft(row = row, column = col),
                        drawDividerUp = board.value.drawDividerUp(row = row, column = col),

                        modifier = cellModifier
                    )
                }
            }
        }
    }

}
