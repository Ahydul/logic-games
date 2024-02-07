package com.example.tfg.state

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import com.example.tfg.common.Board
import com.example.tfg.common.Cell
import com.example.tfg.common.Coordinate
import com.example.tfg.utils.Quadruple

class ActiveGameViewModel : ViewModel() {

    //private val _game = Game.example()

    private var board by mutableStateOf(Board.example())
    private var isNote = mutableStateOf(false)
    private var isPaint = mutableStateOf(false)

    //private val _moves = MutableStateFlow(_game.state[0].moves)
    //val moves: StateFlow<List<Move>> = _moves.asStateFlow()

    private var selectedTiles = mutableStateListOf<Coordinate>()

    init {
        Log.d("VM","ViewModel")
    }

    fun getNumColumns(): Int {
        return board.numColumns
    }
    fun getNumRows(): Int {
        return board.numRows
    }

    fun setCellValue(index: Int, value: Int) {
        val cells = board.cells
        val newCell = cells[index]

        newCell.value = if (newCell.value == value) { 0 } else { value }
        cells[index] = newCell

        board = board.copy(cells = cells)
    }

    private fun setCellColor(index: Int, color: Color, defaultColor: Color) {
        val cells = board.cells
        val newCell = cells[index]
        if (newCell.backGroundColor != color) newCell.backGroundColor = color
        else newCell.backGroundColor = defaultColor
        cells[index] = newCell

        board = board.copy(cells = cells)
    }
    private fun setCellNote(index: Int, noteIndex: Int, note: Int) {
        val cells = board.cells
        val newCell = cells[index]
        if (newCell.notes[noteIndex] == 0) newCell.notes[noteIndex] = note
        cells[index] = newCell

        board = board.copy(cells = cells)
    }
    private fun setCellNote(index: Int, note: Int) {
        Log.d("setCellNote","$index, $note")
        val cells = board.cells
        val newCell = cells[index]
        if (note == 0)
            newCell.notes.forEachIndexed { _: Int, i: Int ->
                newCell.notes[i] = 0
            }
        else
            newCell.notes.forEachIndexed { n: Int, i: Int ->
                if (n == 0) newCell.notes[i] = note
            }

        board = board.copy(cells = cells)
    }


    fun getCell(coordinate: Coordinate): Cell {
        return board.cells[coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!]
    }
    private fun getCell(index: Int): Cell {
        return board.cells[index]
    }

    fun dividersToDraw(coordinate: Coordinate): Quadruple<Boolean> {
        return Quadruple(
            up = board.drawDividerUp(coordinate),
            down = board.drawDividerDown(coordinate),
            left = board.drawDividerLeft(coordinate),
            right = board.drawDividerRight(coordinate)
        )
    }
    private fun coordinateFromPosition(size: IntSize, position: Offset): Coordinate? {
        val coordinate = Coordinate(
            row = getRow(y = position.y, height = size.height),
            column = getColumn(x = position.x, width = size.width)
        )
        return if (coordinate.isOutOfBounds(numRows = getNumRows(), numColumns = getNumColumns())) null
            else {coordinate}
    }

    private fun indexFromPosition(size: IntSize, position: Offset) : Int? {
        val coordinate = Coordinate(
            row = getRow(y = position.y, height = size.height),
            column = getColumn(x = position.x, width = size.width)
        )
        return coordinate.toIndex(numRows = getNumRows(), numColumns = getNumColumns())
    }

    fun isSelected(size: IntSize, position: Offset): Boolean {
        val coordinate = coordinateFromPosition(size = size, position = position)
        return selectedTiles.contains(coordinate)
    }
    private fun isSelected(coordinate: Coordinate): Boolean {
        return selectedTiles.contains(coordinate)
    }
    fun removeSelections() {
        selectedTiles.removeAll { true }
    }

    fun selectTile(size: IntSize, position: Offset) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if(coordinate!=null){
            //If actual tile is not selected select it
            if (!isSelected(coordinate)) selectedTiles.add(coordinate)
        }
    }
    fun deselectTile(size: IntSize, position: Offset) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if(coordinate!=null){
            //If actual tile is selected deselect it
            if (isSelected(coordinate)) selectedTiles.remove(coordinate)
        }
    }

    fun setSelection(size: IntSize, position: Offset) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if(coordinate!=null){
            //If actual tile is selected the action is to deselect and vice versa
            val selecting = !isSelected(coordinate)

            if (selecting) selectedTiles.add(coordinate)
            else selectedTiles.remove(coordinate)
        }
    }

    private fun getColumn(x: Float, width: Int) : Int {
        return  (x * getNumColumns() / width).toInt()
    }
    private fun getRow(y: Float, height: Int) : Int {
        return (y * getNumRows() / height).toInt()
    }

    fun isTileSelected(coordinate: Coordinate): Boolean {
        return selectedTiles.contains(coordinate!!)
    }

    fun isPaint(): Boolean {
        return isPaint.value
    }

    fun isNote(): Boolean {
        return isNote.value
    }

    fun setIsPaint() {
        isPaint.value = !isPaint()
    }
    fun setIsNote() {
        isNote.value = !isNote()
    }

    fun paintAction (color: Color, defaultColor: Color ) {
        for (tile in selectedTiles)
            setCellColor(
                index = tile.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!,
                color = color,
                defaultColor = defaultColor
            )
    }

    private fun noteAction (value: Int, ordered: Boolean = true) {
        for (tile in selectedTiles){
            val cell = getCell(value)
            if(!cell.readOnly) {
                val tile = tile.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!
                if (ordered) setCellNote(
                    index = tile,
                    note = value,
                    noteIndex = value
                )
                else setCellNote(
                    index = tile,
                    note = value)
            }
        }
    }
    private fun writeAction (value: Int) {
        if(selectedTiles.size == 1) {
            val index = selectedTiles[0].toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!
            val cell = getCell(index = index)
            if(!cell.readOnly){
                setCellValue(index = index, value = value)
                selectedTiles.removeAll{ true }
            }
        }
    }

    fun action(value: Int) {
        if (isNote()) noteAction(value = value)
        else writeAction(value)
    }

}
