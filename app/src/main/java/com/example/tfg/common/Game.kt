package com.example.tfg.common

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.tfg.games.GameType
import com.example.tfg.games.hakyuu.Hakyuu
import java.time.LocalDateTime
import kotlin.random.Random

class Game private constructor(
    val gameType: GameType,
    val difficulty: Difficulty,
    val state: SnapshotStateList<GameState>,
    //val solution: List<Int>,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime? = null,
    var errors: List<Move> = emptyList(),
    var numClues: Int = 0
) : GameType by gameType {


    companion object {
        private fun create(gameType: GameType, board: Board, difficulty: Difficulty): Game {
            val gameState = GameState(board = board)
            return Game(gameType = gameType, difficulty = difficulty, state = mutableStateListOf(gameState))
        }

        fun example(): Game {
            val numColumns = 8
            val numRows = 8
            val x: Int = ((numRows+numColumns) * 1.5).toInt()
            val random = Random(46600748394535)

            val gameType = Hakyuu.create(
                numRows = numColumns,
                numColumns = numRows,
                minNumberOfRegions = x,
                random = random)

            return create(
                gameType = gameType,
                board = emptyBoard(numColumns = numColumns, numRows = numRows),
                difficulty = Difficulty.EASY,
            )
        }

        private fun exampleBoard(): Board {
            val cellValues = IntArray(size = 6*6, init = { 0 })
            cellValues[4] = 2
            cellValues[16] = 3
            cellValues[19] = 3
            cellValues[31] = 4

            return Board.create(
                numColumns = 6,
                numRows = 6,
                cellValues = cellValues,
            )
        }

        private fun emptyBoard(numColumns: Int, numRows: Int): Board {
            val cellValues = IntArray(size = numColumns*numRows, init = { 0 })
            return Board.create(
                numColumns = numColumns,
                numRows = numRows,
                cellValues = cellValues,
            )

        }
    }
}

