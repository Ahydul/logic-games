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
import com.example.tfg.common.Action
import com.example.tfg.common.Board
import com.example.tfg.common.Cell
import com.example.tfg.common.Coordinate
import com.example.tfg.common.Game
import com.example.tfg.common.GameState
import com.example.tfg.common.Move
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

    private fun getGameStates(): SnapshotStateList<GameState> {
        return game.value.state
    }

    private fun getActualState(): GameState {
        return getGameStates()[statePointer.intValue]
    }
    private fun getActualPointer(): Int {
        return getActualState().pointer
    }
    private fun getMoves(): SnapshotStateList<Move> {
        return getActualState().moves
    }

    private fun getMove(pointer: Int): Move {
        return getMoves()[pointer]
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
        getGameStates().add(getActualState().clone())
    }

    //Actual state is updated with unsync data and actual state is changed
    fun setActualState(pointer: Int) {
        Log.d("state", "Changing to $pointer")
        if (pointer < getGameStates().size && pointer != statePointer.intValue){
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

    private fun isReadOnly(index: Int): Boolean {
        return getCell(index).readOnly
    }

    private fun isReadOnly(coordinate: Coordinate): Boolean {
        return getCell(coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!).readOnly
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
    Move functions
     */

    private fun movePointerRight(): Boolean{
        val canRedo = getActualPointer() < getActualState().moves.size
        if (canRedo) getActualState().pointer += 1
        return canRedo
    }

    private fun movePointerLeft(): Boolean{
        val canUndo = getActualPointer() != 0
        if (canUndo) getActualState().pointer -= 1
        return canUndo
    }

    private fun addMove(move: Move) {
        getMoves().add(move)
        movePointerRight()
    }

    fun redoMove() {
        val success = movePointerRight()
        if (!success) return

        Log.d("move","redo move")

        applyMove(getMove(getActualPointer()))
    }

    fun undoMove() {
        val success = movePointerLeft()
        if (!success) return

        Log.d("move","undo move")

        applyMove(getMove(getActualPointer() ))
    }

    fun applyMove(move: Move) {
        val value = move.value
        val coordinates = move.coordinates

        when (move.action) {
            Action.PAINT -> paintAction(colorInt = value, coordinates = coordinates)
            Action.WRITE -> writeAction(value = value, coordinates = coordinates)
            Action.ORDERED_NOTE -> noteAction(value = value, coordinates = coordinates, ordered = true)
            Action.UNORDERED_NOTE -> noteAction(value = value, coordinates = coordinates, ordered = false)
            Action.REMOVE_PAINT -> removePaintAction(coordinates)
            Action.ERASE -> eraseAction(coordinates)
        }
    }

    fun unapplyMove(move: Move) {
        val value = move.value
        val coordinates = move.coordinates

        when (move.action) {
            Action.PAINT -> paintAction(colorInt = value, coordinates = coordinates)
            Action.WRITE -> writeAction(value = value, coordinates = coordinates)
            Action.ORDERED_NOTE -> noteAction(value = value, coordinates = coordinates, ordered = true)
            Action.UNORDERED_NOTE -> noteAction(value = value, coordinates = coordinates, ordered = false)
            Action.REMOVE_PAINT -> removePaintAction(coordinates)
            Action.ERASE -> eraseAction(coordinates)
        }
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

    private fun noteAction(value: Int, coordinates: List<Coordinate>, ordered: Boolean) {
        coordinates.forEach {
            val index = it.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!
            if (ordered) setCellNote(index = index, note = value, noteIndex = value - 1)
            else setCellNote(index = index, note = value)
        }
    }

    private fun writeAction(value: Int, coordinates: List<Coordinate>) {
        coordinates.forEach {
            val index = it.toIndex(numRows = getNumRows(), numColumns = getNumColumns())!!
            setCellValue(value = value, index = index)
        }
    }

    fun noteOrWriteAction(value: Int, ordered: Boolean = true) {
        val coordinates = selectedTiles.filter { !isReadOnly(it) }
        if (coordinates.isEmpty()) return

        if (isNote()) {
            noteAction(value = value, coordinates = coordinates, ordered = ordered)

            addMove(Move.noteAction(coordinates = coordinates, value = value, ordered = ordered))
        }
        else if (coordinates.size == 1) {
            writeAction(value, coordinates = coordinates)
            removeSelections()

            addMove(Move.writeAction(coordinates = coordinates, value = value))
        }
    }

    private fun removePaintAction(coordinates: List<Coordinate>) {
        coordinates.forEach { removeTileColor(it) }
    }

    private fun paintAction(colorInt: Int, coordinates: List<Coordinate>) {
        val color = Color(colorInt)
        coordinates.forEach { setTileColor(color = color, coordinate = it) }
    }

    fun paintAction(colorInt: Int, removeColor: Boolean) {
        val coordinatesPaint = selectedTiles.filter { !isReadOnly(it) && !(removeColor || coloredTiles[it]?.equals(Color(colorInt)) == true) }
        val coordinatesErase = selectedTiles.filter { !isReadOnly(it) && removeColor || coloredTiles[it]?.equals(Color(colorInt)) == true }
        if (coordinatesPaint.isEmpty() || coordinatesErase.isEmpty()) return

        paintAction(colorInt = colorInt, coordinates = coordinatesPaint)
        removePaintAction(coordinates = coordinatesErase)

        addMove(Move.paintAction(coordinates = coordinatesPaint, value = colorInt))
        addMove(Move.removePaintAction(coordinatesErase))
    }

    private fun eraseAction(coordinates: List<Coordinate>) {
        coordinates.forEach {
            val index = it.toIndex(numRows = getNumRows(), numColumns = getNumColumns())!!
            eraseValue(index)
        }
    }

    fun eraseAction() {
        val coordinates = selectedTiles.filter { !isReadOnly(it) }
        if (coordinates.isEmpty()) return

        eraseAction(coordinates)
        removeSelections()

        addMove(Move.eraseAction(coordinates))
    }

}
