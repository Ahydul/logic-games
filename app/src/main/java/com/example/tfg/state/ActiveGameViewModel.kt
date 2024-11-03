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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
import com.example.tfg.data.Converters
import com.example.tfg.data.DataStorePreferences
import com.example.tfg.data.GameDao
import com.example.tfg.games.common.BoardData
import com.example.tfg.games.common.GameValue
import com.example.tfg.games.common.Games
import com.example.tfg.games.hakyuu.Hakyuu
import com.example.tfg.games.kendoku.Kendoku
import com.example.tfg.games.kendoku.KendokuOperation
import com.example.tfg.ui.theme.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.LocalDateTime
import java.util.SortedMap
import kotlin.math.floor

class ActiveGameViewModel(
    private val gameInstance: GameInstance,
    private val gameDao: GameDao,
    private val dataStore: DataStore<Preferences>?,
    private val filesDirectory: File?
) : ViewModel() {

    val snapshotsAllowed: Flow<Boolean>? = dataStore?.let {
        it.data.map { preferences ->
            preferences[DataStorePreferences.SNAPSHOTS_ALLOWED] ?: true
        }
    }

    val themeUserSetting: Flow<Theme>? = dataStore?.let {
        it.data.map { preferences ->
            Theme.from(preferences[DataStorePreferences.THEME])
        }
    }

    val checkErrorsAutomatically: Flow<Boolean>? = dataStore?.let {
        it.data.map { preferences ->
           preferences[DataStorePreferences.CHECK_ERRORS_AUTOMATICALLY] ?: true
        }
    }

    private var actualGameStatePosition = 0
    private val numErrors: MutableIntState
    private val numClues: MutableIntState
    private var maxCluesAllowed = 3
    private val isNote = mutableStateOf(false)
    private val isPaint = mutableStateOf(false)
    private val selectedTiles = mutableStateListOf<Coordinate>()
    private val timer = Timer.create(getGame().timer, viewModelScope)

    private var snapshot: (() -> Bitmap?)? = null
    private var snapshotTooEarly = false

    private var _gameCompleted = mutableStateOf(false)


    init {
        numErrors = mutableIntStateOf(getGame().numErrors)
        numClues = mutableIntStateOf(getGame().numClues)
    }

    var completedPopupWasShown = false
    fun gameIsCompleted() = _gameCompleted.value

    fun gameIsNotCompleted() = !_gameCompleted.value

    private fun gameWasCompleted(): Boolean {
        return !errorsAreCheckedManually() && getCells().all { it.value.value != 0 && !it.value.isError }
    }

    fun completeTheBoard() {
        getCompletedBoard().forEachIndexed { position, value ->
            val cell = getCell(position)
            val newCell = if (cell.value != value) cell.copyOnlyIndex(value = value)
                else cell.copyOnlyIndex(value = value)

            setCell(position, newCell)
        }

        gameCompletedFun(false)
    }

    private fun gameCompletedFun(playerWon: Boolean) {
        // Set end date and if player won
        val endDate = getGame().endGame(playerWon = playerWon)

        updateGameToDB(timer = timer.passedSeconds.value, playerWon = playerWon, endDate = endDate)

        stopGame()

        // Take screenshot of finished board
        Utils.runFunctionWithDelay(delayMillis = 100) { takeFinalSnapshot() }

        // Delete all game states with timeout to avoid race conditions
        // as last cell is updating and the game may be deleted before the cell
        getGameStateIds().forEach {
            Utils.runFunctionWithDelay(delayMillis = 1000) { deleteGameStateFromDb(it) }
        }

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
            gameDao.endActualWinningStreak(endDate = endDate, difficulty = getDifficulty(), gameEnum = getGameType())
            gameDao.endActualGeneralWinningStreak(endDate = endDate, difficulty = getDifficulty())
            gameDao.endActualGeneralWinningStreak(endDate = endDate, difficulty = null)
        }
    }

    private fun addOneToActualWinningStreak() {
        viewModelScope.launch(Dispatchers.IO) {
            var rowsUpdated = gameDao.addOneToActualWinningStreak(gameEnum = getGameType(), difficulty = getDifficulty())
            if (rowsUpdated == 0) gameDao.insertWinningStreak(WinningStreak(gameEnum = getGameType(), difficulty = getDifficulty()))

            rowsUpdated = gameDao.addOneToActualGeneralWinningStreak(difficulty = getDifficulty())
            if (rowsUpdated == 0) gameDao.insertWinningStreak(WinningStreak(gameEnum = null, difficulty = getDifficulty()))

            rowsUpdated = gameDao.addOneToActualGeneralWinningStreak(difficulty = null)
            if (rowsUpdated == 0) gameDao.insertWinningStreak(WinningStreak(gameEnum = null, difficulty = null))
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
    // Pair<Position, Bitmap>
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

    private fun updateGameErrorsToDb(error: Pair<Int, Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.updateGameErrors(gameId = getGameId(), newError = Converters.fromPair(error))
        }
    }

    private fun updateGameTimerToDB(timer: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.updateGameTimer(timer = timer, gameId = getGameId())
        }
    }

    private fun updateGameToDB(timer: Int, playerWon: Boolean, endDate: LocalDateTime) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.updateGameTimerAndEndDate(timer = timer, endDate = endDate, playerWon = playerWon, gameId = getGameId())
        }
    }


    private fun addClueToGameDB() {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.addClueToGame(getGameId())
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

    private fun updateGameStatePointerRightInDb(gameStateId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.movePointerRight(gameStateId)
        }
    }

    private fun updateGameStatePointerLeftInDb(gameStateId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.movePointerLeft(gameStateId)
        }
    }

    private fun updateGameStatePositionLeftInDb(gameStateId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.movePositionLeft(gameStateId)
        }
    }


    private fun deleteGameStateFromDb(gameStateId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = gameDao.getGameStateSnapshotByGameStateId(gameStateId)?.snapshotFilePath
            Utils.deleteFile(file)
            gameDao.deleteGameStateById(gameStateId)
        }
    }

    private fun insertGameStateToDB(newGameState: GameState, newBoard: Board, newCells: List<Cell>) {
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.insertGameState(newGameState)
            gameDao.insertBoard(newBoard)
            newCells.forEachIndexed { position, cell ->
                insertNewCellToDb(cell, newBoard.boardId, position)
            }
        }
    }

    suspend fun insertNewCellToDb(newCell: Cell, boardId: Long, cellPosition: Int) {
        gameDao.insertCell(newCell)
        gameDao.insertBoardCellCrossRef(BoardCellCrossRef(boardId = boardId, cellId = newCell.cellId, cellPosition = cellPosition))
    }


//  Snapshot functionality

    fun setSnapshotNull() {
        this.snapshot = null
    }

    fun setSnapshot2(snapshot: (() -> Bitmap?)) {
        this.snapshot = snapshot
    }

    private fun snapshotAllowed(): Boolean {
        return runBlocking { snapshotsAllowed?.first() } ?: true
    }

    fun takeSnapshot() {
        if (!snapshotAllowed() || gameIsCompleted() || snapshotTooEarly || timerPaused() ||
            snapshot == null || filesDirectory == null) return

        val bitmap = snapshot?.invoke()
        if (bitmap == null) return

        MainScope().launch {
            val bitmapFilePath = Utils.saveBitmapToFile(
                bitmap = bitmap,
                filesDir = filesDirectory,
                fileName = "gameStateId-${getActualGameStateId()}",
                directory = getGameType().name.lowercase()
            )
            if (bitmapFilePath != null) {
                val gameStateSnapshot = GameStateSnapshot(getActualGameStateId(), bitmapFilePath)
                insertGameStateSnapshotToDB(gameStateSnapshot)
                snapshotTooEarly = true
            }
        }
    }

    private fun takeFinalSnapshot() {
        if (!snapshotAllowed() || snapshot == null || filesDirectory == null) return

        val bitmap = snapshot?.invoke()
        if (bitmap == null) return

        MainScope().launch {
            Utils.saveBitmapToFile(
                bitmap = bitmap,
                filesDir = filesDirectory,
                fileName = "final-${getGameId()}",
                directory = getGameType().name.lowercase()
            )
        }
    }


//  Main getters

    private fun getGame() = gameInstance.game

    private fun getGameId() = getGame().gameId

    private fun getAbstractGame() = gameInstance.abstractGame

    private fun getMoves() = gameInstance.moves

    private fun getBoard() = gameInstance.board

    private fun getCells() = gameInstance.cells

    private fun getGameType() = getGame().gameType

    private fun getGameStateIds() = gameInstance.gameStateIds

    fun getNumberOfGameStates() = getGameStateIds().size

    private fun getActualState() = gameInstance.actualGameState

    private fun getActualMovesPointer() = getActualState().pointer

    private fun getMove(pointer: Int) = getMoves()[pointer]

    fun getActualGameStatePosition() = actualGameStatePosition

    fun getActualGameStateId() = getGameStateIds()[getActualGameStatePosition()]

    fun getNumClues() = numClues.value

    fun getScoreValue() = getAbstractGame().score.get().toString()

    fun getMaxNumCluesAllowed() = maxCluesAllowed

    fun getDifficulty() = getGame().difficulty

    fun getDifficulty(context: Context) = getGame().difficulty.toString(context)

    fun getValue(value: Int): GameValue = getAbstractGame().getValue(value)

    fun getMaxValue() = getAbstractGame().maxRegionSize()

    private fun getRegions() = getAbstractGame().getRegions()

    private fun getCompletedBoard() = getAbstractGame().completedBoard

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
        loadCells(newCells)
    }

    private fun loadCells(newCells: List<Cell>) {
        // All of this is to force recomposition only in new cells
        val tmpCell = Cell.initializeBoardCell(-1)
        newCells.forEachIndexed { index, cell ->
            if (getCells()[index].value != cell) {
                getCells()[index].value = tmpCell
                getCells()[index].value = cell
            }else{
                getCells()[index].value.cellId = cell.cellId
            }
        }
    }

    private fun loadGameState(gameStatePosition: Int) {
        actualGameStatePosition = gameStatePosition
        gameInstance.actualGameState = getActualGameStateFromDb()
        gameInstance.board = getBoardFromDb(getActualState().gameStateId)
        gameInstance.moves = getMovesFromDb(getActualState().gameStateId)
        loadCells(getBoard().boardId)
    }

    private fun loadNewGameState(newGameState: GameState, newBoard: Board, newCells: List<Cell>) {
        snapshotTooEarly = false

        getGameStateIds().add(newGameState.gameStateId)
        actualGameStatePosition = newGameState.position
        gameInstance.actualGameState = newGameState
        gameInstance.board = newBoard
        gameInstance.moves = mutableListOf()
        loadCells(newCells)
    }

    fun cloneGameState(position: Int) {
        val newGameState = GameState.create(gameId = getGameId(), position = getNumberOfGameStates())
        val previousGameStateId = getGameStateIds()[position]
        val previousBoard = getBoardFromDb(previousGameStateId)
        val newBoard = Board.create(from = previousBoard, gameStateId = newGameState.gameStateId)
        val newCells = getCellsFromDb(boardId = previousBoard.boardId).mapIndexed { _, cell ->
            cell.copyWithNewIndex()
        }

        insertGameStateToDB(newGameState, newBoard, newCells)
        loadNewGameState(newGameState, newBoard, newCells)
    }

    fun setActualState(position: Int) {
        if (position < getNumberOfGameStates() && position != getActualGameStatePosition()){
            loadGameState(position)
        }
        removeSelections()
    }

    fun deleteGameState(position: Int) {
        if (position < getNumberOfGameStates() && position != getActualGameStatePosition()){

            deleteGameStateFromDb(getGameStateIds()[position])

            val actualGameStateId = getActualGameStateId()
            getGameStateIds().removeAt(position)
            if (getNumberOfGameStates() > position) {
                (position..< getNumberOfGameStates()).forEach { otherPointer ->
                    val otherGameStateId = getGameStateIds()[otherPointer]
                    updateGameStatePositionLeftInDb(otherGameStateId)
                    if (actualGameStateId == otherGameStateId) {
                        getActualState().movePositionLeft()
                        actualGameStatePosition -= 1
                    }
                }
            }

            if (getNumberOfGameStates() == 1) checkErrors()

            Log.d("state", "Changed to state: ${getActualState()}")
        }
    }

    /*
        Timer
     */
    fun getTime() = Timer.formatTime(timer.passedSeconds.value)

    fun timerPaused() = timer.paused.value

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
        updateGameTimerToDB(gameInstance.game.timer)
    }


    /*
        Cell fuctions
     */

    // Getters
    private fun getCell(index: Int): Cell = getCells()[index].value

    fun getCell(coordinate: Coordinate) = getCell(getPosition(coordinate))

    private fun getCells(coordinates: Collection<Coordinate>) = coordinates.map { getCell(it) }

    private fun getCells2(positions: Collection<Int>) = positions.map { getCell(it) }

    private fun isReadOnly(index: Int) = getCell(index).readOnly

    private fun isReadOnly(coordinate: Coordinate) = isReadOnly(getPosition(coordinate))

    private fun getCellColor(index: Int) = getCell(index).backgroundColor

    private fun getCellValue(index: Int) = getCell(index).value

    private fun getCellValue(coordinate: Coordinate) = getCellValue(getPosition(coordinate))

    fun getCellColor(coordinate: Coordinate) = getCellColor(getPosition(coordinate))


    // Setters

    private fun setCell(index: Int, newCell: Cell) {
        snapshotTooEarly = false
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
            updateGameErrorsToDb(error)
        }
    }

    private fun addClue() {
        getGame().addClue()
        numClues.intValue++
        addClueToGameDB()
    }


    // Returns if setting this value completes the game
    private fun setCellValue(index: Int, value: Int, isError: Boolean = false) {
        val previousCell = getCell(index)
        val newCell = previousCell.copy(
            value = if (previousCell.value == value) 0 else value,
            notes = Cell.emptyNotes(),
            isError = isError
        )
        setCell(index = index, newCell = newCell)

        if (isError) addError(index = index, value = value)
    }

    private fun setCellError(index: Int, isError: Boolean, color: Int? = null) {
        val previousCell = getCell(index)
        val newCell = if (color != null)
            previousCell.copy(
                isError = isError,
                backgroundColor = if (previousCell.backgroundColor == color) 0 else color
            )
        else previousCell.copy(
            isError = isError
        )

        setCell(index = index, newCell = newCell)

        if (isError) addError(index = index, value = previousCell.value)
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
        setCell(index = coordinate.toIndex(getNumColumns()), newCell = newCell)
    }

    private fun setCellsNotes(note: Int, coordinates: List<Coordinate>, ordered: Boolean) {
        coordinates.forEach {
            val index = it.toIndex(getNumColumns())
            if (ordered) setCellNote(index = index, note = note, noteIndex = note - 1)
            else setCellNote(index = index, note = note)
        }
    }

    private fun eraseCells(coordinates: List<Coordinate>) {
        coordinates.forEach {
            val index = it.toIndex(getNumColumns())
            eraseValue(index)
        }
    }

    private fun setCellsBackgroundColor(color: Int, coordinates: List<Coordinate>) {
        coordinates.forEach { coordinate ->
            val index = getPosition(coordinate)
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
        val row = if (getNumColumns() > getNumRows()) {
            val factor = getNumRows().toFloat() / getNumColumns()
            val actualHeight = size.height * factor

            val actualY = position.y - ((size.height - actualHeight) / 2)

            floor(actualY * getNumRows() / actualHeight).toInt()
        }
        else (position.y * getNumRows() / size.height).toInt()

        val column = if (getNumColumns() < getNumRows()) {
            val factor = getNumColumns().toFloat() / getNumRows()
            val actualWidth = (size.width * factor).toInt()
            val actualX = position.x - ((size.width - actualWidth) / 2)

            floor(actualX * getNumColumns() / actualWidth).toInt()
        }
        else (position.x * getNumColumns() / size.width).toInt()


        val coordinate = Coordinate(
            row = row,
            column = column
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
        else if (removePrevious) removeSelections()
    }


    /*
    Move functions
     */

    private fun movePointerRight() {
        getActualState().pointer = getActualState().pointer + 1
        updateGameStatePointerRightInDb(getActualGameStateId())
    }

    private fun movePointerLeft() {
        getActualState().pointer = getActualState().pointer - 1
        updateGameStatePointerLeftInDb(getActualGameStateId())
    }

    private fun addMove(coordinates: Collection<Coordinate>, newCells: List<Cell>, previousCells: List<Cell>) {
        require(coordinates.size == newCells.size && coordinates.size == previousCells.size) { "Wrong sizes provided" }
        val gameStateId = getActualGameStateId()
        val pointer = getActualMovesPointer()
        val move = Move(position = pointer + 1, gameStateId = gameStateId)
        val actions = coordinates.mapIndexedNotNull { index, coordinate ->
            val newCell = newCells[index]
            val previousCell = previousCells[index]

            if (newCell == previousCell) null
            else{
                val position = coordinate.toIndex(getNumColumns())
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
            val coordinate = getCoordinate(it.cellIndex)
            //We select unless its a color error and its not the cell actively changed
            if (newCell.value != previousCell.value || newCell.backgroundColor != Cell.ERROR_CELL_BACKGROUND_COLOR)
                addSelection(coordinate)
            setCell(coordinate = coordinate, newCell = newCell)
        }
    }

    private fun unapplyMove(moveWithActions: MoveWithActions) {
        removeSelections()
        moveWithActions.actions.forEach {
            val newCell = it.newCell
            val previousCell = it.previousCell
            val coordinate = getCoordinate(it.cellIndex)
            //We select unless its a color error and its not the cell actively changed
            if (previousCell.value != newCell.value || newCell.backgroundColor != Cell.ERROR_CELL_BACKGROUND_COLOR) {
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

    private fun cellsToIntArrayValues() = getCells().map { if (it.value.isError) 0 else it.value.value }.toIntArray()

    private fun cellsToPossibleValues(): Array<MutableList<Int>> {
        return getCells().map {
            if (it.value.hasNoNotes()) mutableListOf()
            else it.value.notes.toMutableList()
        }.toTypedArray()
    }

    private fun checkValue(position: Int, value: Int): Set<Int> {
        return if (value != 0 && isError(position, value))
            getAbstractGame().checkValue(
                position = position,
                value = value,
                actualValues = cellsToIntArrayValues()
            )
        else emptySet()
    }

    private fun isError(position: Int, value: Int): Boolean {
        return getAbstractGame().isError(
            position = position,
            value = value
        )
    }

    fun noteOrWriteAction(value: Int, ordered: Boolean = false) {
        val coordinates = selectedTiles.filter { !isReadOnly(it) && (!isNote() || getCellValue(it) == 0) }.toMutableList()
        if (coordinates.isEmpty()) return

        var previousCells = getCells(coordinates).toMutableList()

        if (isNote()) {
            setCellsNotes(note = value, coordinates = coordinates, ordered = ordered)
            val newCells = getCells(coordinates)
            addMove(coordinates = coordinates, newCells = newCells, previousCells = previousCells)
        }
        else if (coordinates.size == 1) {
            val position = coordinates.first().toIndex(getNumColumns())

            setCellValue(value = value, index = position)

            if (!errorsAreCheckedManually()) {
                // Paint positions that causes errors
                checkErrors(coordinates = coordinates, previousCells = previousCells) // This creates the move
            }
            else {
                val newCells = getCells(coordinates)
                addMove(coordinates = coordinates, newCells = newCells, previousCells = previousCells)
            }
            removeSelections()
        }
        else return
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
        val coordinates = selectedTiles.filter { !isReadOnly(it) }.toMutableList()
        if (coordinates.isEmpty()) return

        val previousCells = getCells(coordinates).toMutableList()

        eraseCells(coordinates)

        if (!errorsAreCheckedManually()) {
            // Paint positions that causes errors
            checkErrors(coordinates = coordinates, previousCells = previousCells) // This creates the move
        }
        else {
            val newCells = getCells(coordinates)
            addMove(coordinates = coordinates, newCells = newCells, previousCells = previousCells)
        }

        removeSelections()
    }


    /*
    Other
     */

    // For debug
    fun solveBoardOneStep() {
        val abstractGame = getAbstractGame()
        val possibleValues = cellsToPossibleValues()
        val actualValues = cellsToIntArrayValues()
        (abstractGame as Hakyuu).solveBoardOneStep(BoardData(possibleValues = possibleValues, actualValues = actualValues))

        actualValues.indices.forEach { position ->
            val newCell = getCell(position).copy(
                value = actualValues[position],
                notes = possibleValues[position].toIntArray(),
                isError = false,
                backgroundColor = 0
            )

            setCell(index = position, newCell = newCell)
        }
    }


    // For when more than one state
    fun checkErrors(
        coordinates: MutableList<Coordinate> = mutableListOf(),
        previousCells: MutableList<Cell> = mutableListOf()
    ) {
        var noErrorsFound = true
        val errorstmp = mutableSetOf<Int>()
        //Set errors
        getPositions().forEach { position ->
            val cell = getCell(position)
            val coordinate = getCoordinate(position)
            if (previousCells.all { it.cellId != cell.cellId }) {
                previousCells.add(cell)
                coordinates.add(coordinate)
            }

            val value = cell.value
            val wasError = cell.isError
            var isError = isError(position, value)

            noErrorsFound = !isError

            // If error changed we set it
            if (wasError != isError && getCellValue(position) != 0) {
                errorstmp.add(position)
                setCellError(position, isError)
            }
        }

        // The background errors must be found after the cell errors
        val backgroundErrors = mutableSetOf<Int>()
        getPositions().forEach { position ->
            val value = getCellValue(position)
            val errors = checkValue(position = position, value = value)
            backgroundErrors.addAll(errors)
        }

        getPositions().forEach { position ->
            val hadBGError = getCell(position).hasErrorBackground()
            val hasBGError = backgroundErrors.contains(position)

            // If the background error changed we set it
            if (hadBGError != hasBGError) {
                setCellBackgroundColor(position, Cell.ERROR_CELL_BACKGROUND_COLOR)
            }
        }

        if (noErrorsFound) {
            gameCompletedFun(true)
        }
        else {
            val newCells = getCells(coordinates)
            addMove(coordinates = coordinates, newCells = newCells, previousCells = previousCells)
        }
    }

    private fun getCoordinate(position: Int): Coordinate {
        return Coordinate.fromIndex(position, numColumns = getNumColumns(), numRows = getNumRows())
    }

    private fun getPosition(coordinate: Coordinate): Int {
        return coordinate.toIndex(getNumColumns())
    }

    private fun getPositions() = (0..< getNumCells())

    private fun getRandomPosition(): Int { //There must be a value
        return getPositions().shuffled().find { position -> getCellValue(position) == 0 }!!
    }

    private fun oneOrNoCellsLeft(): Boolean {
        return getPositions().count { position -> getCellValue(position) == 0 } < 2
    }

    private fun noCluesLeft(): Boolean {
        return getNumClues() == maxCluesAllowed
    }

    fun giveClue() {
        //If only one value left refuse to allow player to "win"
        if (noCluesLeft() || oneOrNoCellsLeft()) return

        val position = if (selectedTiles.size == 1) selectedTiles.first().toIndex(numColumns = getNumColumns())
        else getRandomPosition()

        if (getCellValue(position) != 0) return
        addClue()
        setCell(
            index = position,
            newCell = getCell(position).copyOnlyIndex(value = getCompletedBoard()[position])
        )

        // We don't create a move to now avoid repeating clues

    }

    fun buttonShouldBeEnabled(): Boolean {
        return !timerPaused() && gameIsNotCompleted()
    }

    private fun errorsAreCheckedManually(): Boolean {
        return getNumberOfGameStates() > 1 || !runBlocking { checkErrorsAutomatically?.first() ?: true }
    }

    fun hasMoreThanOneGameState() = getNumberOfGameStates() > 1

    fun setCheckErrorsAutomatically(status: Boolean) {
        viewModelScope.launch {
            dataStore?.edit { preferences ->
                preferences[DataStorePreferences.CHECK_ERRORS_AUTOMATICALLY] = status
            }
        }
        // To check possible errors that weren't manually checked
        if (status) checkErrors()
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

    fun getCorner(position: Int): Pair<Int, KendokuOperation>? {
        val regionID = getAbstractGame().getRegionId(position)
        val position2 = getRegions()[regionID]!!.first().toIndex(getNumColumns())
        return if (getGameType() == Games.KENDOKU && position == position2) {
            val number = (getAbstractGame() as Kendoku).operationResultPerRegion[regionID]!!
            val operation = (getAbstractGame() as Kendoku).operationPerRegion[regionID]!!
            number to operation
        }
            else null
    }

}
