package com.example.tfg.common

import androidx.compose.runtime.mutableStateListOf
import com.example.tfg.games.GameType
import com.example.tfg.games.Games
import com.example.tfg.games.hakyuu.Hakyuu
import java.time.LocalDateTime
import kotlin.random.Random

class Game private constructor(
    val gameType: GameType,
    val difficulty: Difficulty,
    val state: MutableList<GameState>,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime? = null,
    var errors: MutableSet<Pair<Int,Int>> = mutableSetOf(),
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
            val board = Board.create(numRows = numRows, numColumns = numColumns, cellValues = hakyuu.startBoard)

            board.cells[2].notes[0] = 1
            board.cells[2].notes[1] = 10
            board.cells[2].notes[2] = 11

            return create(
                gameType = hakyuu,
                board = board,
                difficulty = Difficulty.EASY
            )
        }

        fun create(chosenGame: Games, difficulty: Difficulty, numColumns: Int, numRows: Int, seed: Long = (Math.random()*10000000000).toLong()): Game {
            // Initialize game
            val random = Random(seed)
            val game = when(chosenGame) {
                Games.HAKYUU -> Hakyuu.create(numRows = numRows, numColumns = numColumns, random = random)
            }
            // Create game board
            game.createGame()
            val board = Board.create(numRows = numRows, numColumns = numColumns, cellValues = game.startBoard)

            return create(
                gameType = game,
                board = board,
                difficulty = difficulty
            )
        }
    }
}

