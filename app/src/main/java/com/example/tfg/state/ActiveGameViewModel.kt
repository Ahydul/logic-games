package com.example.tfg.state

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.geometry.Offset
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
import com.example.tfg.common.entities.WinningStreak
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
import java.time.LocalDateTime
import java.util.SortedMap
private const val ERRORCELLBACKGROUNDCOLOR = -65536

class ActiveGameViewModel(
    private val gameInstance: GameInstance,
    private val gameDao: GameDao,
    val snapshotsAllowed: Boolean
) : ViewModel() {

    private var actualGameStatePointer = 0
    private val numErrors: MutableIntState
    private val isNote = mutableStateOf(false)
    private val isPaint = mutableStateOf(false)
    private val selectedTiles = mutableStateListOf<Coordinate>()
    private val timer = Timer.create(getGame().timer, viewModelScope)

    private var snapshot: (() -> Bitmap?)? = null
    private var filesDirectory: File? = null

    private var _gameCompleted = mutableStateOf(false)


    init {
        numErrors = mutableIntStateOf(getGame().numErrors)
    }

    var completedPopupWasShown = false
    fun gameIsCompleted() = _gameCompleted.value

    fun gameIsNotCompleted() = !_gameCompleted.value

    private fun gameWasCompleted(): Boolean {
        return !errorsDisabled() && getCells().all { it.value.value != 0 && !it.value.isError }
    }

    fun completeTheBoard() {
        getGameType().completedBoard.forEachIndexed { position, value ->
            val cell = getCell(position)
            if (cell.value != value) {
                val newCell = cell.copy(
                    value = value,
                    notes = Cell.emptyNotes(),
                    backgroundColor = 0,
                    isError = false
                )
                setCell(position, newCell)
            }
        }

        gameCompletedFun(false)
    }

    private fun gameCompletedFun(playerWon: Boolean) {
        // Set end date and if player won
        val endDate = getGame().endGame(playerWon = playerWon)

        // Update game timer and update game to DB
        updateTimer()

        stopGame()

        // Delete all game states
        getGameStateIds().forEach { deleteGameStateFromDb(it) }

        // Update winning streak
        if (playerWon) addOneToActualWinningStreak()
        else endActualWinningStreak(endDate)

        _gameCompleted.value = true
    }

    public override fun onCleared() {
        super.onCleared()
        // Stops the timer job and save game to DB
        pauseGame()
    }

//  GameDao functionality

    private fun endActualWinningStreak(endDate: LocalDateTime) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.endActualWinningStreak(endDate = endDate, gameEnum = getGameEnum())
            gameDao.endActualGeneralWinningStreak(endDate = endDate)
        }
    }

    private fun addOneToActualWinningStreak() {
        viewModelScope.launch(Dispatchers.IO) {
            var rowsUpdated = gameDao.addOneToActualWinningStreak(getGameEnum())
            if (rowsUpdated == 0) gameDao.insertWinningStreak(WinningStreak(gameEnum = getGameEnum()))

            rowsUpdated = gameDao.addOneToActualGeneralWinningStreak()
            if (rowsUpdated == 0) gameDao.insertWinningStreak(WinningStreak(gameEnum = null))

        }
    }

    private fun insertGameStateSnapshotToDB(snapshot: GameStateSnapshot) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.insertGameStateSnapshot(snapshot)
        }
    }

    fun getGameStatesBitmapFromDB(): SnapshotStateMap<Int, Bitmap?> {
        return getGameStateSnapshotsFromDB().sortedBy { it.first }.toMutableStateMap()
    }

    fun getGameStateBitmapFromDB(): Bitmap? {
        return Utils.getBitmapFromFile(getGameStateSnapshotFromDB())
    }

    private fun getGameStateSnapshotFromDB(): GameStateSnapshot? {
        return runBlocking {
            gameDao.getGameStateSnapshotByGameStateId(getActualGameStateId())
        }
    }
    private fun getGameStateSnapshotsFromDB(): List<Pair<Int, Bitmap?>> {
        val res = mutableListOf<Pair<Int, Bitmap?>>()
        return runBlocking {
            getGameStateIds().forEach { id ->
                val snapshot = gameDao.getGameStateSnapshotByGameStateId(id)
                val pair = getGameStateIds().indexOf(snapshot?.gameStateId ?: id) to
                        Utils.getBitmapFromFile(snapshot)
                res.add(pair)
            }
            res
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

    private fun deleteGameStateFromDb(gameStateId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = gameDao.getGameStateSnapshotByGameStateId(gameStateId)?.snapshotFilePath
            Utils.deleteFile(file)
            gameDao.deleteGameStateById(gameStateId)
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


//  Snapshot functionality

    fun setSnapshot(snapshot: (() -> Bitmap)?) {
        this.snapshot = snapshot
    }

    fun setSnapshot2(snapshot: (() -> Bitmap?)) {
        this.snapshot = snapshot
    }

    fun setFilesDirectory(filesDirectory: File) {
        this.filesDirectory = filesDirectory
    }

    private var lastSnapshotTaken = getGame().timer
    private fun snapshotTooEarly(): Boolean {
        return timer.passedSeconds.value - lastSnapshotTaken < 10
    }

    fun takeSnapshot() {
        if (gameIsNotCompleted() || snapshotTooEarly() || timerPaused() ||
            snapshot == null || filesDirectory == null) return

        Log.d("snapshot", "Taking snapshot")
        val bitmap = snapshot!!.invoke()

        if (bitmap == null) {
            Log.d("snapshot", "Failed")
            return
        }

        MainScope().launch {
            val bitmapFilePath = Utils.saveBitmapToFile(
                bitmap = bitmap,
                filesDir = filesDirectory!!,
                fileName = "gameStateId-${getActualGameStateId()}",
                directory = getGameEnum().name.lowercase()
            )
            if (bitmapFilePath != null) {
                val gameStateSnapshot = GameStateSnapshot(getActualGameStateId(), bitmapFilePath)
                insertGameStateSnapshotToDB(gameStateSnapshot)
            }
        }

        lastSnapshotTaken = timer.passedSeconds.value
    }


//  Main getters

    private fun getGame() = gameInstance.game

    private fun getGameType() = getGame().gameTypeEntity

    private fun getRealGameType() = getGameType().toGameType()

    private fun getMoves() = gameInstance.moves

    private fun getBoard() = gameInstance.board

    private fun getCells() = gameInstance.cells

    private fun getGameEnum() = getGameType().type

    private fun getGameStateIds() = gameInstance.gameStateIds

    private fun getActualState() = gameInstance.actualGameState

    private fun getActualMovesPointer() = getActualState().pointer

    private fun getMove(pointer: Int) = getMoves()[pointer]

    fun getActualGameStatePosition() = actualGameStatePointer

    fun getActualGameStateId() = getGameStateIds()[getActualGameStatePosition()]

    fun getNumClues() = getGame().numClues

    fun getDifficulty() = getGame().difficulty

    fun getDifficulty(context: Context) = getGame().difficulty.toString(context)

    fun getValue(value: Int): GameValue = getRealGameType().getValue(value)

    fun getMaxValue() = getRealGameType().maxRegionSize()

    private fun getRegions() = getRealGameType().getRegions()

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

    fun getNumErrors() = numErrors.intValue

    fun isPaint() = isPaint.value

    fun isNote() = isNote.value


    /*
        GameState functions
     */

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
        if (pointer < getGameStateIds().size && pointer != getActualGameStatePosition()){
            loadGameState(pointer)
            Log.d("state", "Changed to state: ${getActualState()}")
        }
        removeSelections()
    }

    fun deleteGameState(pointer: Int) {
        if (pointer < getGameStateIds().size && pointer != getActualGameStatePosition()){
            deleteGameStateFromDb(getGameStateIds()[pointer])
            Log.d("state", "Changed to state: ${getActualState()}")
        }
    }

/*
    Timer
 */
    fun getTime() = Timer.formatTime(timer.passedSeconds.value)

    fun timerPaused() = timer.paused.currentState

    fun getTimerState() = timer.paused

    fun pauseGame() {
        if (!timerPaused() && gameIsNotCompleted()) {
            timer.pauseTimer()
            updateTimer()
        }
    }

    fun stopGame() {
        timer.stopTimer()
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

    private fun getCellValue(index: Int) = getCell(index).value

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
        val res = getGame().addError(error)
        if (res) {
            numErrors.intValue++
            updateGameToDb()
        }
    }

    private fun errorsDisabled(): Boolean {
        return getGameStateIds().size > 1
    }

    // Returns if setting this value completes the game
    private fun setCellValue(index: Int, value: Int, isError: Boolean = false): Boolean {
        val previousCell = getCell(index)
        val newCell = previousCell.copy(
            value = if (previousCell.value == value) 0 else value,
            notes = Cell.emptyNotes(),
            // If more than one state errors are disabled: only solved errors are shown
            isError = if (errorsDisabled() && !previousCell.isError) false
                else isError
        )
        setCell(index = index, newCell = newCell)

        if (isError) addError(index = index, value = value)
        else return gameWasCompleted()

        return false
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

    private fun setCellBackgroundColor(index: Int, color: Int) {
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
            setCellBackgroundColor(color = color, index = index)
        }
    }

    fun setIsPaint() {
        if (isNote()) setIsNote()
        isPaint.value = !isPaint()
    }

    fun setIsNote() {
        if (isPaint()) setIsPaint()
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
        require(coordinates.size == newCells.size && coordinates.size == previousCells.size) { "Wrong sizes provided" }
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
        return if (isError(position, value))
            getRealGameType().checkValue(
                position = position,
                value = value,
                actualValues = getCells().map { if (it.value.isError) 0 else it.value.value }.toIntArray()
            )
        else emptySet()
    }

    private fun isError(position: Int, value: Int): Boolean {
        return getRealGameType().isError(
                position = position,
                value = value
            )
    }

    fun noteOrWriteAction(value: Int, ordered: Boolean = false) {
        var gameCompleted = false
        val coordinates = selectedTiles.filter { !isReadOnly(it) }.toMutableList()
        if (coordinates.isEmpty()) return

        var previousCells = getCells(coordinates).toMutableList()

        if (isNote()) {
            setCellsNotes(note = value, coordinates = coordinates, ordered = ordered)
        }
        else if (coordinates.size == 1) {
            val position = coordinates.first().toIndex(numRows = getNumRows(), numColumns = getNumColumns())!!
            val oldErrors = checkValue(position = position, value = getCellValue(position))

            val isError = isError(position = position, value = value)
            gameCompleted = setCellValue(value = value, index = position, isError = isError)

            // Paint positions that causes errors
            val errors = mutableSetOf<Int>()
            getCells().forEachIndexed { pos, ms ->
                val cell = ms.value
                if (cell.isError) errors.addAll(checkValue(position = pos, value = cell.value))
            }
            errors.forEach { pos ->
                val cell = getCell(pos)
                if (cell.backgroundColor != ERRORCELLBACKGROUNDCOLOR){
                    if (pos != position) {
                        previousCells.add(cell)
                        coordinates.add(Coordinate.fromIndex(pos, numColumns = getNumColumns(), numRows = getNumRows()))
                    }
                    setCellBackgroundColor(pos, ERRORCELLBACKGROUNDCOLOR)
                }
            }

            oldErrors.filter { !errors.contains(it) }.forEach { pos ->
                val cell = getCell(pos)
                if (pos != position) {
                    previousCells.add(cell)
                    coordinates.add(Coordinate.fromIndex(pos, numColumns = getNumColumns(), numRows = getNumRows()))
                }
                setCellBackgroundColor(pos, 0) // This gets rid of the error color
            }

            removeSelections()
        }
        else return

        val newCells = getCells(coordinates)
        addMove(coordinates = coordinates, newCells = newCells, previousCells = previousCells)

        if (gameCompleted) gameCompletedFun(playerWon = true)
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

    fun buttonShouldBeEnabled(): Boolean {
        return !timerPaused() && gameIsNotCompleted()
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
