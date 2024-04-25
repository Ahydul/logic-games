package com.example.tfg.common

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.tfg.games.GameType
import com.example.tfg.games.hakyuu.Hakyuu
import java.time.LocalDateTime

class Game private constructor(
    val gameType: GameType,
    val difficulty: Difficulty,
    val state: SnapshotStateList<GameState>,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime? = null,
    var errors: List<Move> = emptyList(),
    var numClues: Int = 0
) {

    companion object {
        private fun create(gameType: GameType, board: Board, difficulty: Difficulty): Game {
            val gameState = GameState(board = board)
            return Game(
                gameType = gameType,
                difficulty = difficulty,
                state = mutableStateListOf(gameState)
            )
        }

        fun example(): Game {
            val numColumns = 8
            val numRows = 8
            val hakyuu = Hakyuu.example()

            return create(
                gameType = hakyuu,
                board = Board.create(numRows = numRows, numColumns = numColumns, cellValues = hakyuu.startBoard),
                difficulty = Difficulty.EASY
            )
        }
    }
}

