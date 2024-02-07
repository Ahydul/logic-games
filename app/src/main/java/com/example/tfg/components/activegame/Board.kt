package com.example.tfg.components.activegame

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.tfg.R
import com.example.tfg.state.ActiveGameViewModel
/*

private fun Modifier.customTapGestures(onLongPres: (Offset) -> Unit, onTap: (Offset) -> Unit ): Modifier {
    return this then Modifier.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = onLongPres,
            onTap = onTap
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
}*/

@Composable
fun Board(
    viewModel: ActiveGameViewModel = ActiveGameViewModel(),
    modifier: Modifier = Modifier
) {
    Log.d("TAG", "BOARDcurrentRecomposeScope $currentRecomposeScope")
    val boardState by viewModel.board.collectAsState()

    val numColumns = viewModel.getNumColumns()
    val numRows = viewModel.getNumRows()

    Column(
        modifier = modifier
            //Tap -> removes all selections and selects the cell
            //Long press -> keeps selections. Selects or deselects the cell if cell was deselected or selected
           /* .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        Log.d("TAG", "LongPress $it $size")
                        viewModel.setSelections(
                            position = it,
                            size = size,
                            resetSelects = false
                        )
                    },
                    onTap = {
                        Log.d("TAG", "Tap $it $size")
                        viewModel.setSelections(
                            position = it,
                            size = size,
                            resetSelects = true
                        )
                    }
                )
            }*/

    ) {
        val cellModifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .background(colorResource(id = R.color.cell_background))

        for (row in 0..<numRows) {

            Row(modifier = modifier.weight(1f)) {

                for (col in 0..<numColumns) {
                    val coordinate = Coordinate(row = row, column = col)
                    Cell(
                        cell = viewModel.getCell(row = row, column = col),
                        isSelected = false,
                        dividersToDraw = viewModel.dividersToDraw(row = row, column = col),
                        modifier = cellModifier
                    )
                }
            }
        }
    }

}
