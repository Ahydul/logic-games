package com.example.tfg.state

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import com.example.tfg.common.Board
import com.example.tfg.common.Cell
import com.example.tfg.common.Difficulty
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.Game
import com.example.tfg.common.GameState
import com.example.tfg.common.Move
import com.example.tfg.common.utils.Quadruple
import com.example.tfg.games.GameType
import com.example.tfg.games.Games
import java.util.SortedMap

class ActiveGameViewModel(game: Game) : ViewModel() {

    private val ERRORCELLBACKGROUNDCOLOR = Color.Red.toArgb()
    private val game = game
    private var numErrors = mutableStateOf(0)
    private var statePointer = mutableIntStateOf(0)
    private val isNote = mutableStateOf(false)
    private val isPaint = mutableStateOf(false)
    private val selectedTiles = mutableStateListOf<Coordinate>()

    fun tmp(): List<Int> {
        return getCells().map { it.value }
    }

    init {
        Log.d("VM","ViewModel")
    }

//  Main getters

    private fun getGameStates(): MutableList<GameState> {
        return game.state
    }

    private fun getGameType(): GameType {
        return game.gameType
    }

    fun getNumClues(): Int {
        return game.numClues
    }

    fun getDifficulty(): Difficulty {
        return game.difficulty
    }

    fun getDifficulty(context: Context): String {
        return game.difficulty.toString(context)
    }


    fun getGame(): Games {
        return getGameType().type
    }

    fun getMaxValue(): Int {
        return getGameType().maxRegionSize
    }

    private fun getRegions(): Map<Int, List<Coordinate>> {
        return getGameType().getRegions()
    }

    fun getNumberRegionSizes(): SortedMap<Int, Int> {
        val res = sortedMapOf<Int,Int>()
        for (region in getRegions()) {
            val size = region.value.size
            if (res.contains(size)) res[size] = res[size]!! + 1
            else res.put(size, 1)
        }
        return res
    }

    fun getRegionSize(): Int {
        return getRegions().size
    }

    private fun getActualState(): GameState {
        return getGameStates()[statePointer.intValue]
    }

    private fun getActualMovesPointer(): Int {
        return getActualState().pointer
    }

    private fun getMoves(): MutableList<Move> {
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

    private fun getCells(): MutableList<Cell> {
        return getBoard().cells
    }

    fun getNumErrors(): Int {
        return numErrors.value
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
        removeSelections()
    }

/*
    Cell fuctions
 */

    // Getters
    private fun getCell(index: Int): Cell {
        return getCells()[index]
    }

    fun getCell(coordinate: Coordinate): Cell {
        return getCell(coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!)
    }

    private fun getCells(coordinates: List<Coordinate>): List<Cell> {
        return coordinates.map { getCell(it) }
    }

    private fun isReadOnly(index: Int): Boolean {
        return getCell(index).readOnly
    }

    private fun isReadOnly(coordinate: Coordinate): Boolean {
        return isReadOnly(coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!)
    }

    private fun getCellColor(index: Int): Int {
        return getCell(index).backgroundColor
    }

    fun getCellColor(coordinate: Coordinate): Int {
        return getCellColor(coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!)
    }

    // Setters

    private fun eraseValue(index: Int) {
        if (!getCell(index).isEmpty()) setCell(index = index, newCell = Cell.createWithBackground(backgroundColor = 0))
    }

    private fun addError(index: Int, value: Int) {
        val error = Pair(index, value)
        val res = game.errors.add(error)
        if (res) numErrors.value++
    }

    private fun setCellValue(index: Int, value: Int, isError: Boolean = false) {
        val previousCell = getCell(index)
        val newCell = previousCell.copy(
            value = if (previousCell.value == value) 0 else value,
            isError = isError
        )
        setCell(index = index, newCell = newCell)

        if (isError) addError(index = index, value = value)
        Log.d("d","add $index to errors")
        Log.d("d","${getNumErrors()}")

    }

    private fun setCellNote(index: Int, noteIndex: Int, note: Int) {
        val previousCell = getCell(index)
        val newCell = if (note == 0) {
            previousCell.copy(notes = Cell.emptyNotes())
        } else if (previousCell.getNote(noteIndex) == note) {
            previousCell.copy(noteIndex = noteIndex, noteValue = 0)
        } else {
            previousCell.copy(noteIndex = noteIndex, noteValue = note)
        }
        setCell(index = index, newCell = newCell)
    }

    private fun setCellNote(index: Int, note: Int) {
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
        setCell(index = index, newCell = newCell)
    }

    private fun setCellColor(index: Int, color: Int) {
        val previousCell = getCell(index)
        val newCell = previousCell.copy(
            backgroundColor = if (previousCell.backgroundColor == color) 0 else color
        )
        setCell(index = index, newCell = newCell)
    }

    private fun setCell(coordinate: Coordinate, newCell: Cell) {
        getCells()[coordinate.toIndex(numRows = getNumRows(), numColumns = getNumColumns())!!] = newCell
    }

    private fun setCell(index: Int, newCell: Cell) {
        getCells()[index] = newCell
    }

    private fun setCellsNotes(note: Int, coordinates: List<Coordinate>, ordered: Boolean) {
        coordinates.forEach {
            val index = it.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!
            if (ordered) setCellNote(index = index, note = note, noteIndex = note - 1)
            else setCellNote(index = index, note = note)
        }
    }

    private fun eraseCells(coordinates: List<Coordinate>) {
        coordinates.forEach {
            val index = it.toIndex(numRows = getNumRows(), numColumns = getNumColumns())!!
            eraseValue(index)
        }
    }

    private fun setCellsBackgroundColor(color: Int, coordinates: List<Coordinate>) {
        coordinates.forEach { coordinate ->
            val index = coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!
            setCellColor(color = color, index = index)
        }
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

    fun addSelections(coordinates: List<Coordinate>) {
        selectedTiles.addAll(coordinates)
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

    private fun getColumn(x: Float, width: Int) : Int {
        return  (x * getNumColumns() / width).toInt()
    }
    private fun getRow(y: Float, height: Int) : Int {
        return (y * getNumRows() / height).toInt()
    }

    fun isTileSelected(coordinate: Coordinate): Boolean {
        return selectedTiles.contains(coordinate)
    }

    /*
    Move functions
     */

    private fun movePointerRight() {
        getActualState().pointer = getActualState().pointer + 1
    }

    private fun movePointerLeft() {
        getActualState().pointer = getActualState().pointer - 1
    }

    private fun addMove(move: Move) {
        //Remove moves that won't be accesed anymore
        while (getActualMovesPointer() < getMoves().size - 1) {
            getMoves().removeLast()
        }

        getMoves().add(move)
        movePointerRight()
    }

    fun applyMove(move: Move) {
        var valuesChanged = mutableListOf<Coordinate>()
        for (i in 0..move.coordinates.size - 1) {
            val newCell = move.newCells[i]
            val coordinate = move.coordinates[i]
            if (getCell(coordinate).value != newCell.value) valuesChanged.add(coordinate)

            setCell(coordinate = coordinate, newCell = newCell)
        }

        removeSelections()

        if (valuesChanged.isNotEmpty()) {
            addSelections(valuesChanged)
        }else {
            addSelections(move.coordinates)
        }
    }

    fun unapplyMove(move: Move) {
        var valuesChanged = mutableListOf<Coordinate>()
        for (i in 0..move.coordinates.size - 1) {
            val previousCell = move.previousCells[i]
            val coordinate = move.coordinates[i]
            if (getCell(coordinate).value != previousCell.value) valuesChanged.add(coordinate)

            setCell(coordinate = coordinate, newCell = previousCell)
        }

        removeSelections()

        if (valuesChanged.isNotEmpty()) {
            addSelections(valuesChanged)
        }else {
            addSelections(move.coordinates)
        }
    }

    fun redoMove() {
        val canRedo = getActualMovesPointer() < getMoves().size - 1
        if (!canRedo) return

        movePointerRight()
        applyMove(getMove(getActualMovesPointer()))
    }

    fun undoMove() {
        val canUndo = getActualMovesPointer() > -1
        if (!canUndo) return

        unapplyMove(getMove(getActualMovesPointer()))
        movePointerLeft()
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
        if (game.gameType.noNotes) return
        isNote.value = !isNote()
    }

    private fun findRegionID(coordinate: Coordinate): Int? {
        for (entry in getRegions().entries)
            if (entry.value.contains(coordinate)) return entry.key

        return null
    }

    private fun fromSameRegion(coordinate1: Coordinate, coordinate2: Coordinate): Boolean{
        val regionID = findRegionID(coordinate1)
        return regionID != null && getRegions()[regionID]!!.contains(coordinate2)
    }

    private fun drawDivisorBetween(original: Coordinate, other: Coordinate?): Boolean {
        return other != null && !fromSameRegion(original, other)
    }

    private fun drawDividerRight(coordinate: Coordinate): Boolean {
        return drawDivisorBetween(coordinate, coordinate.moveRight(numColumns = getNumColumns()))
    }

    private fun drawDividerDown(coordinate: Coordinate): Boolean {
        return drawDivisorBetween(coordinate, coordinate.moveDown(numRows = getNumRows()))
    }

    private fun drawDividerLeft(coordinate: Coordinate): Boolean {
        return drawDivisorBetween(coordinate,coordinate.moveLeft())
    }

    private fun drawDividerUp(coordinate: Coordinate): Boolean {
        return drawDivisorBetween(coordinate, coordinate.moveUp())
    }


    fun dividersToDraw(coordinate: Coordinate): Quadruple<Boolean> {
        return Quadruple(
            up = drawDividerUp(coordinate),
            down = drawDividerDown(coordinate),
            left = drawDividerLeft(coordinate),
            right = drawDividerRight(coordinate)
        )
    }

    /*
    Game Actions
     */

    private fun checkValue(position: Int, value: Int): Set<Int> {
        return getGameType().checkValue(
            position = position,
            value = value,
            actualValues = getBoard().cells.map { if (it.isError) 0 else it.value }.toIntArray()
        )
    }

    private fun isError(position: Int, value: Int): Boolean {
        return getGameType().isError(
            position = position,
            value = value
        )
    }

    fun noteOrWriteAction(value: Int, ordered: Boolean = false) {
        val coordinates = selectedTiles.filter { !isReadOnly(it) }.toMutableList()
        if (coordinates.isEmpty()) return

        var previousCells = getCells(coordinates).toMutableList()

        if (isNote()) {
            setCellsNotes(note = value, coordinates = coordinates, ordered = ordered)
        }
        else if (coordinates.size == 1) {
            val index = coordinates.first().toIndex(numRows = getNumRows(), numColumns = getNumColumns())!!

            // Paint positions that causes the error
            val errors = checkValue(position = index, value = value).map { Coordinate.fromIndex(it, getNumRows(),getNumColumns()) }
            previousCells.addAll(getCells(errors))
            setCellsBackgroundColor(color = ERRORCELLBACKGROUNDCOLOR, coordinates = errors)
            coordinates.addAll(errors)

            val isError = isError(position = index, value = value)
            setCellValue(value = value, index = index, isError = isError)

            removeSelections()
        }

        val newCells = getCells(coordinates)
        addMove(Move(coordinates = coordinates, newCells = newCells, previousCells = previousCells))
    }

    fun paintAction(colorInt: Int) {
        val coordinates = selectedTiles.toList()
        val previousCells = getCells(coordinates)

        if (coordinates.isEmpty()) return

        setCellsBackgroundColor(color = colorInt, coordinates = coordinates)

        val newCells = getCells(coordinates)
        addMove(Move(coordinates = coordinates, newCells = newCells, previousCells = previousCells))
    }

    fun eraseAction() {
        val coordinates = selectedTiles.filter { !isReadOnly(it) }
        if (coordinates.isEmpty()) return

        val previousCells = getCells(coordinates)

        eraseCells(coordinates)
        removeSelections()

        val newCells = getCells(coordinates)

        addMove(Move(coordinates = coordinates, newCells = newCells, previousCells = previousCells))
    }

}
