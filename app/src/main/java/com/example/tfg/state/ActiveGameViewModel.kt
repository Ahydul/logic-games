package com.example.tfg.state

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tfg.common.Board
import com.example.tfg.common.Cell
import com.example.tfg.utils.Quadruple
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class ActiveGameViewModel : ViewModel() {
    private val _color = MutableStateFlow(0xFFFFFFFF)
    val color = _color.asStateFlow()

    var composeColor by mutableStateOf(0xFFFFFFFF)
        private set

    fun generateColor(){
        val color = Random.nextLong(0xFFFFFFFF)
        Log.d("","${color}")
        _color.value = color
        composeColor = color
    }


    //private val _game = Game.example()
    private val _board = MutableStateFlow(Board.example())
    val board = _board.asStateFlow()

    //private val _moves = MutableStateFlow(_game.state[0].moves)
    //val moves: StateFlow<List<Move>> = _moves.asStateFlow()

    //val selectedTiles = mutableStateListOf<Int>()

    fun getNumColumns(): Int {
        return board.value.numColumns
    }
    fun getNumRows(): Int {
        return board.value.numRows
    }

    fun setCellValue(index: Int, value: Int) {
        _board.update {
            var cells = it.cells
            var newCell = cells[index]
            newCell.value = value
            cells[index] = newCell
            it.copy(cells = cells)
        }
        Log.d("PUTA","CHANGING VALUE ${_board.value}")

    }
    // Function to get cell at a specific index
    fun getCell(index: Int): Cell {
        return board.value.cells[index]
    }

    fun getCellValue(column: Int, row: Int): Int {
        return getCell(column = column,row).value
    }

    fun getCell(column: Int, row: Int): Cell {
        return board.value.cells[indexToInt(column = column, row=row)!!]
    }

    fun dividersToDraw(column: Int, row: Int): Quadruple<Boolean> {
        return Quadruple(
            up = board.value.drawDividerUp(column = column, row = row),
            down = board.value.drawDividerDown(column = column, row = row),
            left = board.value.drawDividerLeft(column = column, row = row),
            right = board.value.drawDividerRight(column = column, row = row),
            )
    }

    fun indexToInt(column: Int, row: Int): Int? {
        return board.value.indexToInt(row = row, column = column)
    }
    /*
        private fun removeSelections() {
            selectedTiles.removeAll { true }
        }

        fun setSelections(size: IntSize, position: Offset, resetSelects: Boolean) {
            val index = indexFromPosition(size = size, position = position)

            //If actual tile is selected the action is to deselect and vice versa
            val selecting = !selectedTiles.contains(index)

            if (!selecting) {
                if (resetSelects) removeSelections()
                else selectedTiles.remove(index)
            }
            else selectedTiles.add(index!!)//Shoudn't be out of bounds

        }

        private fun getColumn(x: Float, width: Int) : Int {
            return  (x * getNumColumns() / width).toInt()
        }
        private fun getRow(y: Float, height: Int) : Int {
            return (y * getNumRows() / height).toInt()
        }
        private fun indexFromPosition(size: IntSize, position: Offset) : Int? {
            return indexToInt(
                row = getRow(y = position.y, height = size.height),
                column = getColumn(x = position.x, width = size.width))
        }

        fun isTileSelected(coordinate: Coordinate): Boolean {
            return selectedTiles.contains(indexToInt(row = coordinate.row, column = coordinate.column)!!)
        }*/

}
