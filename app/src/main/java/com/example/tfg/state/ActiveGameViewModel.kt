package com.example.tfg.state

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import com.example.tfg.common.Board
import com.example.tfg.common.Cell
import com.example.tfg.common.Coordinate
import com.example.tfg.common.Game
import com.example.tfg.common.GameState
import com.example.tfg.common.utils.Quadruple

class ActiveGameViewModel : ViewModel() {

    private val game = mutableStateOf(Game.example())
    private var statePointer = mutableIntStateOf(0)
    private val isNote = mutableStateOf(false)
    private val isPaint = mutableStateOf(false)
    private val selectedTiles = mutableStateListOf<Coordinate>()
    private val coloredTiles = mutableStateMapOf<Coordinate, Color>()


    fun tmp(): List<Int> {
        return getCells().map { it.value }
    }

    init {
        Log.d("VM","ViewModel")
    }

//  Main getters

    private fun getGameState(): SnapshotStateList<GameState> {
        return game.value.state
    }

    private fun getActualState(): GameState {
        return getGameState()[statePointer.intValue]
    }

    private fun getBoard(): Board {
        return getActualState().board
    }

    fun getNumColumns(): Int {
        return getBoard().numColumns
    }

    fun getNumRows(): Int {
        return getBoard().numRows
    }

    private fun getCells(): SnapshotStateList<Cell> {
        return getBoard().cells
    }


/*
    GameState functions
 */

    //Actual state is updated with unsync data and cloned into a new GameState
    fun newGameState() {
        Log.d("state", "${getActualState()}")
        getGameState().add(getActualState().clone())
    }

    //Actual state is updated with unsync data and actual state is changed
    fun setActualState(pointer: Int) {
        Log.d("state", "Changing to $pointer")
        if (pointer < getGameState().size && pointer != statePointer.intValue){
            statePointer.intValue = pointer
            Log.d("state", "${getActualState()}")
        }
    }

/*
    Cell fuctions
 */

    fun getCell(coordinate: Coordinate): Cell {
        return getCell(coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!)
    }

    private fun getCell(index: Int): Cell {
        return getCells()[index]
    }

    private fun eraseValue(index: Int) {
        if (!getCell(index).isEmpty()) getCells()[index] = Cell.create(0)
    }

    private fun setCellValue(index: Int, value: Int) {
        Log.d("action", "index:$index value:$value")

        val newCell = getCells()[index].copy(
            value = if (getCells()[index].value == value) { 0 } else { value }
        )
        getCells()[index] = newCell

        Log.d("action", "new cell:${getCell(index)}")
    }

    private fun setCellNote(index: Int, noteIndex: Int, note: Int) {
        Log.d("action", "setCellNote index:$index noteIndex:$noteIndex note:$note")

        val newCell = if (note == 0) {
            getCells()[index].copy(notes = Cell.emptyNotes())
        } else if (getCells()[index].getNote(noteIndex) == note) {
            getCells()[index].copy(noteIndex = noteIndex, noteValue = 0)
        } else {
            getCells()[index].copy(noteIndex = noteIndex, noteValue = note)
        }
        getCells()[index] = newCell

        Log.d("action", "new cell:${getCell(index)}")
    }

    private fun setCellNote(index: Int, note: Int) {
        Log.d("action", "setCellNote index:$index note:$note")

        val cell = getCell(index)
        val newCell = if (note == 0) {
            cell.copy(notes = Cell.emptyNotes())
        } else{
            val noteIndex = cell.findNote(note)
            if (noteIndex != null) { //Note exists
                cell.copy(notes = cell.removeNote(noteIndex))
            } else { //Note doesn't exist
                cell.copy(notes = cell.addNote(note))
            }
        }

        getCells()[index] = newCell

        Log.d("action", "new cell:${getCell(index)}")
    }

/*
    SelectedTiles functions
 */

    private fun coordinateFromPosition(size: IntSize, position: Offset): Coordinate? {
        val coordinate = Coordinate(
            row = getRow(y = position.y, height = size.height),
            column = getColumn(x = position.x, width = size.width)
        )
        return if (coordinate.isOutOfBounds(numRows = getNumRows(), numColumns = getNumColumns())) null
            else {coordinate}
    }

    fun isSelected(size: IntSize, position: Offset): Boolean {
        val coordinate = coordinateFromPosition(size = size, position = position)
        return isSelected(coordinate)
    }

    private fun isSelected(coordinate: Coordinate?): Boolean {
        return selectedTiles.contains(coordinate)
    }

    fun removeSelections() {
        selectedTiles.removeAll { true }
    }

    //If tile is not selected and not a null coordinate select it
    fun selectTile(size: IntSize, position: Offset) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if (coordinate!=null && !isSelected(coordinate)) selectedTiles.add(coordinate)
    }

    //If tile is selected and not a null coordinate deselect it
    fun deselectTile(size: IntSize, position: Offset) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if (isSelected(coordinate)) selectedTiles.remove(coordinate)
    }

    //If tile is selected the action is to deselect and vice versa
    fun setSelection(size: IntSize, position: Offset, removePrevious: Boolean = false) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if(coordinate!=null){
            val selecting = !isSelected(coordinate)

            if (removePrevious) removeSelections()

            if (selecting) selectedTiles.add(coordinate)
            else if (!removePrevious) selectedTiles.remove(coordinate)
        }
    }

    /*
    Color functions
     */

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
        return selectedTiles.contains(coordinate)
    }

    fun getTileColor(coordinate: Coordinate, defaultColor: Color): Color {
        return coloredTiles.getOrDefault(coordinate, defaultColor)
    }

    /*
    Other
     */

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

    fun dividersToDraw(coordinate: Coordinate): Quadruple<Boolean> {
        return Quadruple(
            up = getBoard().drawDividerUp(coordinate),
            down = getBoard().drawDividerDown(coordinate),
            left = getBoard().drawDividerLeft(coordinate),
            right = getBoard().drawDividerRight(coordinate)
        )
    }

    /*
    Game Actions
     */

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
