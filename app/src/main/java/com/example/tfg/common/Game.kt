package com.example.tfg.common

import com.example.tfg.games.GameType
import com.example.tfg.games.hakyuu.Hakyuu
import java.time.LocalDateTime

class Game(
    val gameType: GameType,
    val difficulty: Difficulty,
    var state: List<GameState>,
    //var solution: Nose<GameTypeValue>,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime? = null,
    var errors: List<Move> = emptyList(),
    var numClues: Int = 0
) : GameType by gameType {


    companion object Hakyuu {
        fun create(board: Board): Game {
            val gameState = GameState(board = board)

            return Game(gameType = Hakyuu(), difficulty = Difficulty.EASY, state = listOf(gameState))
        }

        fun example(): Game {
            return Hakyuu.create(
                board = Board.example()
            )
        }

    }
}

