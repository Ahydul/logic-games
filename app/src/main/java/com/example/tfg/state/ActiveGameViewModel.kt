package com.example.tfg.state

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.common.GameInstance
import com.example.tfg.common.utils.Timer
import com.example.tfg.common.entities.Action
import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.entities.GameState
import com.example.tfg.common.entities.Move
import com.example.tfg.common.entities.relations.BoardCellCrossRef
import com.example.tfg.common.entities.relations.GameStateSnapshot
import com.example.tfg.common.entities.relations.MoveWithActions
import com.example.tfg.common.utils.Quadruple
import com.example.tfg.common.utils.Utils
import com.example.tfg.data.GameDao
import com.example.tfg.games.GameValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.SortedMap
private const val ERRORCELLBACKGROUNDCOLOR = -65536

class ActiveGameViewModel(private val gameInstance: GameInstance, private val gameDao: GameDao) : ViewModel() {
    private var actualGameStatePointer = 0
    private val numErrors: MutableState<Int>
    private val isNote = mutableStateOf(false)
    private val isPaint = mutableStateOf(false)
    private val selectedTiles = mutableStateListOf<Coordinate>()
    private val timer = Timer.create(getGame().timer, viewModelScope)

    private var snapshot: (() -> Bitmap)? = null
    private var filesDirectory: File? = null

    fun setSnapshot(snapshot: (() -> Bitmap)?) {
        this.snapshot = snapshot
    }

    fun setFilesDirectory(filesDirectory: File) {
        this.filesDirectory = filesDirectory
    }

    fun takeSnapshot() {
        if (snapshot != null && filesDirectory != null) {
            Log.d("snapshot", "Taking snapshot")
            MainScope().launch {
                val bitmapFilePath = Utils.saveBitmapToFile(
                    bitmap = snapshot!!.invoke(),
                    filesDir = filesDirectory!!,
                    fileName = "gameStateId-${getActualGameStateId()}",
                    directory = getGameEnum().name.lowercase()
                )
                if (bitmapFilePath != null) {
                    val gameStateSnapshot = GameStateSnapshot(getActualGameStateId(), bitmapFilePath)
                    insertGameStateSnapshotToDB(gameStateSnapshot)
                }
            }
        }
    }

    init {
        numErrors = mutableStateOf(getGame().errors.size)
    }

    private fun getActualGameStateId(): Long {
        return getGameStateIds()[actualGameStatePointer]
    }

    public override fun onCleared() {
        super.onCleared()
        // Stops the timer job and save game to DB
        pauseGame()
    }

//  GameDao functionality

    private fun insertGameStateSnapshotToDB(snapshot: GameStateSnapshot) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.insertGameStateSnapshot(snapshot)
        }
    }

    fun getGameStateBitmapFromDB(): Map<Long, Bitmap> {
        return getGameStateSnapshotFromDB().mapNotNull {
            Utils.getBitmapFromFile(it.snapshotFilePath)
                ?.let { f -> it.gameStateId to f }
        }.toMap()
    }

    private fun getGameStateSnapshotFromDB(): List<GameStateSnapshot> {
        return runBlocking {
            gameDao.getGameStateSnapshots()
        }
    }

    private fun updateGameToDb() {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.updateGame(getGame())
        }
    }

    private fun getActualGameStateFromDb(): GameState {
        return getGameStateByIdFromDb(getActualGameStateId())
    }

    private fun getGameStateByIdFromDb(gameStateId: Long): GameState {
        return runBlocking { gameDao.getGameStateById(gameStateId) }
    }

    private fun getMovesFromDb(gameStateId: Long): MutableList<MoveWithActions> {
        return runBlocking { gameDao.getMovesByGameStateId(gameStateId) }
    }

    private fun getBoardFromDb(gameStateId: Long): Board {
        return runBlocking { gameDao.getBoardByGameStateId(gameStateId) }
    }

    private fun getCellsFromDb(boardId: Long): List<Cell> {
        return runBlocking { gameDao.getBoardCells(boardId) }
    }

    private fun updateCellToDb(cell: Cell) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.updateCell(cell)
        }
    }

    private suspend fun deleteMovesFromDb(movesId: List<Long>) {
        gameDao.deleteMoves(movesId)
        gameDao.deleteActionsByMovesPosition(movesId)
    }

    private suspend fun insertActionsToDB(actions: List<Action>) {
        gameDao.insertActions(actions)
    }

    private suspend fun insertMoveToDB(move: Move) {
        gameDao.insertMove(move)
    }

    private fun addMoveDB(move: Move, toRemove: List<Long>, actions: List<Action>) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteMovesFromDb(toRemove)
            insertMoveToDB(move)
            insertActionsToDB(actions)
        }
    }


    private fun updateGameStateInDb() {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.updateGameState(getActualState())
        }
    }

    private fun insertGameStateToDB(newGameState: GameState) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.insertGameState(newGameState)
        }
    }

    private fun insertBoardToDB(newBoard: Board) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.insertBoard(newBoard)
        }
    }

    private fun insertNewCellToDb(newCell: Cell, boardId: Long, cellPosition: Int): Long {
        return runBlocking {
            val cellId = gameDao.insertCell(newCell)
            gameDao.insertBoardCellCrossRef(BoardCellCrossRef(boardId = boardId, cellId = cellId, cellPosition = cellPosition))
            cellId
        }
    }


//  Main getters

    private fun getGame() = gameInstance.game

    private fun getGameType() = getGame().gameType

    private fun getMoves() = gameInstance.moves

    private fun getBoard() = gameInstance.board

    private fun getCells() = gameInstance.cells

    private fun getGameEnum() = getGameType().type

    private fun getGameStateIds() = gameInstance.gameStateIds

    private fun getActualState() = gameInstance.actualGameState

    private fun getActualMovesPointer() = getActualState().pointer

    private fun getMove(pointer: Int) = getMoves()[pointer]

    fun getNumClues() = getGame().numClues

    fun getDifficulty() = getGame().difficulty

    fun getDifficulty(context: Context) = getGame().difficulty.toString(context)

    fun getValue(value: Int): GameValue = getGameType().getValue(value)

    fun getMaxValue() = getGameType().maxRegionSize()

    private fun getRegions() = getGameType().getRegions()

    fun getRegionSize() = getRegions().size

    fun getNumberRegionSizes(): SortedMap<Int, Int> {
        val res = sortedMapOf<Int,Int>()
        for (region in getRegions()) {
            val size = region.value.size
            if (res.contains(size)) res[size] = res[size]!! + 1
            else res.put(size, 1)
        }
        return res
    }

    fun getNumColumns() = getBoard().numColumns

    fun getNumRows() = getBoard().numRows

    fun getNumCells() = getNumRows() * getNumColumns()

    fun getNumErrors() = numErrors.value

    fun isPaint() = isPaint.value

    fun isNote() = isNote.value


    /*
        GameState functions
     */
//TODO: STATE

    private fun loadCells(boardId: Long) {
        // All of this is to force recomposition only in new cells
        val newCells = getCellsFromDb(boardId)
        val tmpCell = Cell.create(-1)
        newCells.forEachIndexed { index, cell ->
            if (getCells()[index].value != cell) {
                getCells()[index].value = tmpCell
                getCells()[index].value = cell
            }else{
                getCells()[index].value.cellId = cell.cellId
            }
        }
    }

    private fun loadGameState(gameStatePointer: Int) {
        actualGameStatePointer = gameStatePointer
        gameInstance.actualGameState = getActualGameStateFromDb()
        gameInstance.board = getBoardFromDb(getActualState().gameStateId)
        gameInstance.moves = getMovesFromDb(getActualState().gameStateId)
        loadCells(getBoard().boardId)
    }

    private fun loadNewGameState(newGameState: GameState, newBoard: Board) {
        getGameStateIds().add(newGameState.gameStateId)
        actualGameStatePointer = getGameStateIds().size - 1
        gameInstance.actualGameState = newGameState
        gameInstance.board = newBoard
        gameInstance.moves = mutableListOf()
        loadCells(newBoard.boardId)
    }

    fun newGameState() {
        Log.d("state", "Actual state: ${getActualState()}")

        val newGameState = GameState.create(from = getActualState(), position = getGameStateIds().size)
        val newBoard = Board.create(from = getBoard(), gameStateId = newGameState.gameStateId)

        insertGameStateToDB(newGameState)
        insertBoardToDB(newBoard)
        getCells().forEachIndexed { position, cell ->
            insertNewCellToDb(cell.value.copy(cellId = 0), boardId = newBoard.boardId, cellPosition = position)
        }

        loadNewGameState(newGameState, newBoard)

        Log.d("state", "New state: ${getActualState()}")
    }

    fun setActualState(pointer: Int) {
        Log.d("state", "Actual state: ${getActualState()}")
        if (pointer < getGameStateIds().size && pointer != actualGameStatePointer){
            loadGameState(pointer)
            Log.d("state", "Changed to state: ${getActualState()}")
        }
        removeSelections()
    }
/*
    Timer
 */
    fun getTime() = Timer.formatTime(timer.passedSeconds.value)

    fun timerPaused() = timer.paused.value

    fun pauseGame() {
        timer.pauseTimer()
        updateTimer()
    }

    fun resumeGame() {
        timer.startTimer(viewModelScope)
    }

    private fun updateTimer() {
        gameInstance.game.timer = timer.passedSeconds.value
        updateGameToDb()
    }


    /*
        Cell fuctions
     */

    // Getters
    private fun getCell(index: Int): Cell = getCells()[index].value

    fun getCell(coordinate: Coordinate) = getCell(coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!)

    private fun getCells(coordinates: List<Coordinate>) = coordinates.map { getCell(it) }

    private fun isReadOnly(index: Int) = getCell(index).readOnly

    private fun isReadOnly(coordinate: Coordinate) = isReadOnly(coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!)

    private fun getCellColor(index: Int) = getCell(index).backgroundColor

    fun getCellColor(coordinate: Coordinate) = getCellColor(coordinate.toIndex(numColumns = getNumColumns(), numRows = getNumRows())!!)


    // Setters

    private fun setCell(index: Int, newCell: Cell) {
        getCells()[index].value = newCell
        updateCellToDb(newCell)
    }

    private fun eraseValue(index: Int): Boolean {
        val cell = getCell(index)
        if (!cell.isEmpty()) {
            setCell(index = index, newCell = cell.copyErase())
            return true
        }
        return false
    }

    private fun addError(index: Int, value: Int) {
        val error = Pair(index, value)
        val res = getGame().errors.add(error)
        updateGameToDb()
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
        setCell(index = coordinate.toIndex(numRows = getNumRows(), numColumns = getNumColumns())!!, newCell = newCell)
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

    fun setIsPaint() {
        isPaint.value = !isPaint()
    }

    fun setIsNote() {
        isNote.value = !isNote()
    }


    /*
        SelectedTiles functions
     */

    fun isTileSelected(coordinate: Coordinate?) = selectedTiles.contains(coordinate)

    private fun getColumn(x: Float, width: Int) = (x * getNumColumns() / width).toInt()

    private fun getRow(y: Float, height: Int) = (y * getNumRows() / height).toInt()

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
        return isTileSelected(coordinate)
    }

    fun removeSelections() {
        selectedTiles.removeAll { true }
    }

    fun addSelection(coordinate: Coordinate) {
        selectedTiles.add(coordinate)
    }

    fun addSelections(coordinates: List<Coordinate>) {
        selectedTiles.addAll(coordinates)
    }

    //If tile is not selected and not a null coordinate select it
    fun selectTile(size: IntSize, position: Offset) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if (coordinate!=null && !isTileSelected(coordinate)) selectedTiles.add(coordinate)
    }

    //If tile is selected and not a null coordinate deselect it
    fun deselectTile(size: IntSize, position: Offset) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if (isTileSelected(coordinate)) selectedTiles.remove(coordinate)
    }

    //If tile is selected the action is to deselect and vice versa
    fun setSelection(size: IntSize, position: Offset, removePrevious: Boolean = false) {
        val coordinate = coordinateFromPosition(size = size, position = position)
        if(coordinate!=null){
            val selecting = !isTileSelected(coordinate)

            if (removePrevious) removeSelections()

            if (selecting) selectedTiles.add(coordinate)
            else if (!removePrevious) selectedTiles.remove(coordinate)
        }
    }


    /*
    Move functions
     */

    private fun movePointerRight() {
        getActualState().pointer = getActualState().pointer + 1
        updateGameStateInDb()
    }

    private fun movePointerLeft() {
        getActualState().pointer = getActualState().pointer - 1
        updateGameStateInDb()
    }

    private fun addMove(coordinates: List<Coordinate>, newCells: List<Cell>, previousCells: List<Cell>) {
        val gameStateId = getActualGameStateId()
        val pointer = getActualMovesPointer()
        val move = Move(position = pointer + 1, gameStateId = gameStateId)
        val actions = coordinates.mapIndexedNotNull { index, coordinate ->
            val newCell = newCells[index]
            val previousCell = previousCells[index]

            if (newCell == previousCell) null
            else{
                val position = coordinate.toIndex(numRows = getNumRows(), numColumns = getNumColumns())!!
                Action(
                    newCell = newCell,
                    previousCell = previousCell,
                    cellIndex = position,
                    moveId = move.moveId
                )
            }
        }.toList()

        if (actions.isEmpty()) return

        // Remove moves that won't be accessed anymore
        val toRemove = mutableListOf<Long>()
        while (pointer < getMoves().size - 1) {
            toRemove.add(getMoves().removeLast().move.moveId)
        }

        addMoveDB(move = move, toRemove = toRemove, actions = actions)

        getMoves().add(MoveWithActions(move, actions))
        movePointerRight()
    }

    private fun applyMove(moveWithActions: MoveWithActions) {
        removeSelections()
        moveWithActions.actions.forEach {
            val newCell = it.newCell
            val previousCell = it.previousCell
            val coordinate = Coordinate.fromIndex(it.cellIndex, getNumRows(), getNumColumns())
            //We select unless its a color error and its not the cell actively changed
            if (newCell.value != previousCell.value || newCell.backgroundColor != ERRORCELLBACKGROUNDCOLOR)
                addSelection(coordinate)
            setCell(coordinate = coordinate, newCell = newCell)
        }
    }

    private fun unapplyMove(moveWithActions: MoveWithActions) {
        removeSelections()
        moveWithActions.actions.forEach {
            val newCell = it.newCell
            val previousCell = it.previousCell
            val coordinate = Coordinate.fromIndex(it.cellIndex, getNumRows(), getNumColumns())
            //We select unless its a color error and its not the cell actively changed
            if (previousCell.value != newCell.value || newCell.backgroundColor != ERRORCELLBACKGROUNDCOLOR) {
                addSelection(coordinate)
            }
            setCell(coordinate = coordinate, newCell = previousCell)
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
    Game Actions
     */

    private fun checkValue(position: Int, value: Int): Set<Int> {
        return getGameType().checkValue(
            position = position,
            value = value,
            actualValues = getCells().map { if (it.value.isError) 0 else it.value.value }.toIntArray()
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
        else return

        val newCells = getCells(coordinates)
        addMove(coordinates = coordinates, newCells = newCells, previousCells = previousCells)
    }

    fun paintAction(colorInt: Int) {
        val coordinates = selectedTiles.toList()
        val previousCells = getCells(coordinates)

        if (coordinates.isEmpty()) return

        setCellsBackgroundColor(color = colorInt, coordinates = coordinates)

        val newCells = getCells(coordinates)
        addMove(coordinates = coordinates, newCells = newCells, previousCells = previousCells)
    }

    fun eraseAction() {
        val coordinates = selectedTiles.filter { !isReadOnly(it) }
        if (coordinates.isEmpty()) return

        val previousCells = getCells(coordinates)

        eraseCells(coordinates)
        removeSelections()

        val newCells = getCells(coordinates)

        addMove(coordinates = coordinates, newCells = newCells, previousCells = previousCells)
    }



    /*
    Other
     */

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
        //When outside of bounds (null) we draw divisor
        return other == null || !fromSameRegion(original, other)
    }

    private fun drawDividerRight(coordinate: Coordinate) = drawDivisorBetween(coordinate, coordinate.moveRight(getNumColumns()))

    private fun drawDividerDown(coordinate: Coordinate) = drawDivisorBetween(coordinate, coordinate.moveDown(getNumRows()))

    private fun drawDividerLeft(coordinate: Coordinate) = drawDivisorBetween(coordinate,coordinate.moveLeft())

    private fun drawDividerUp(coordinate: Coordinate) = drawDivisorBetween(coordinate, coordinate.moveUp())

    fun dividersToDraw(coordinate: Coordinate): Quadruple<Boolean> {
        return Quadruple(
            up = drawDividerUp(coordinate),
            down = drawDividerDown(coordinate),
            left = drawDividerLeft(coordinate),
            right = drawDividerRight(coordinate)
        )
    }

}
