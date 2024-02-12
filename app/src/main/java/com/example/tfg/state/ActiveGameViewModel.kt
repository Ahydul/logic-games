package com.example.tfg.state

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import com.example.tfg.common.Board
import com.example.tfg.common.Cell
import com.example.tfg.common.Coordinate
import com.example.tfg.common.utils.Quadruple

class ActiveGameViewModel : ViewModel() {

    //private val _game = Game.example()

    private val board = Board.example()

    private val cells = mutableStateListOf<Cell>()

    private var isNote = mutableStateOf(false)
    private var isPaint = mutableStateOf(false)

    //private val _moves = MutableStateFlow(_game.state[0].moves)
    //val moves: StateFlow<List<Move>> = _moves.asStateFlow()

    private var selectedTiles = mutableStateListOf<Coordinate>()
    private var coloredTiles = mutableStateMapOf<Coordinate, Color>()


    fun tmp(): List<Int> {
        return cells.map { it.value }
    }

    init {
        Log.d("VM","ViewModel")
        cells.addAll(board.cells)
    }

    fun getNumColumns(): Int {
        return board.numColumns
    }
    fun getNumRows(): Int {
        return board.numRows
    }

    private fun eraseValue(index: Int) {
        if (!getCell(index).isEmpty()) cells[index] = Cell.create(0)
    }

    fun setCellValue(index: Int, value: Int) {
        Log.d("action", "index:$index value:$value")

        val newCell = cells[index].copy(
            value = if (cells[index].value == value) { 0 } else { value }
        )
        cells[index] = newCell

        Log.d("action", "new cell:${getCell(index)}")
    }

    private fun setCellNote(index: Int, noteIndex: Int, note: Int) {
        Log.d("action", "setCellNote index:$index noteIndex:$noteIndex note:$note")

        val newCell = if (note == 0) {
            cells[index].copy(notes = Cell.emptyNotes())
        } else if (cells[index].getNote(noteIndex) == note) {
            cells[index].copy(noteIndex = noteIndex, noteValue = 0)
        } else {
            cells[index].copy(noteIndex = noteIndex, noteValue = note)
        }
        cells[index] = newCell

        Log.d("action", "new cell:${getCell(index)}")
    }

    private fun setCellNote(index: Int, note: Int) {
        Log.d("action", "setCellNote index:$index note:$note")

        val cell = getCell(index)
        val newCell = if (note == 0) {
            cell.copy(notes = Cell.emptyNotes())
        } else{
            val noteIndex = cell.findNote(note)
            if (noteIndex != -1) { //Note exists
                cell.copy(notes = cell.removeNote(noteIndex))
            } else { //Note doesn't exist
                cell.copy(notes = cell.addNote(note))
            }
        }

        cells[index] = newCell

        Log.d("action", "new cell:${getCell(index)}")
    }


    fun getCell(coordinate: Coordinate): Cell {
        return getCell(coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!)
    }
    private fun getCell(index: Int): Cell {
        return cells[index]
    }
    fun getCellValue(coordinate: Coordinate): Int {
        return getCell(coordinate).value
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
    fun setSelectionRemoveOthers(size: IntSize, position: Offset) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if(coordinate!=null){
            //If actual tile is selected the action is to deselect and vice versa
            val selecting = !isSelected(coordinate)
            removeSelections()
            if (selecting) selectedTiles.add(coordinate)
        }
    }

    private fun setTileColor(coordinate: Coordinate, color: Color) {
        coloredTiles[coordinate] = color
    }
    private fun removeTileColor(coordinate: Coordinate) {
        coloredTiles.remove(coordinate)
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

    fun getTileColor(coordinate: Coordinate, defaultColor: Color): Color {
        return coloredTiles.getOrDefault(coordinate, defaultColor)
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

    //As I don't know how to properly get the default background color from resources outside composable I use a boolean
    fun paintAction(color: Color, removeColor: Boolean) {
        for (tile in selectedTiles)
            if (removeColor || coloredTiles[tile]?.equals(color) == true) removeTileColor(tile)
            else setTileColor(coordinate = tile, color = color)
    }

    private fun noteAction(value: Int, ordered: Boolean = true) {
        for (tile in selectedTiles){
            val tile = tile.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!
            val cell = getCell(tile)

            if(cell.readOnly) return

            if (ordered) setCellNote(index = tile, note = value, noteIndex = value - 1)
            else setCellNote(index = tile, note = value)
        }
    }

    private fun writeAction(value: Int) {
        if(selectedTiles.size == 1) {
            val index = selectedTiles[0].toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!
            val cell = getCell(index = index)
            if(!cell.readOnly){
                setCellValue(index = index, value = value)
                selectedTiles.removeAll{ true }
            }
        }
    }

    fun eraseAction() {
        for (tile in selectedTiles){
            val cell = getCell(tile)
            if(!cell.readOnly) {
                eraseValue(tile.toIndex(numRows = getNumRows(), numColumns = getNumColumns())!!)
            }
        }
        removeSelections()
    }

    fun action(value: Int) {
        if (isNote()) noteAction(value = value)
        else writeAction(value)
    }

}
