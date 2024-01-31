package com.example.tfg.common

import com.example.tfg.games.GameType
import com.example.tfg.games.hakyuu.Hakyuu

class Game(
    val gameType: GameType,
    val difficulty: Difficulty,
    var state: List<GameState>,
    //var solution: Nose<GameTypeValue>,
    var numErrors: Int = 0,
    var numClues: Int = 0
) : GameType by gameType {


    companion object Hakyuu {
        fun create(board: Board): Game {
            val gameState = GameState(board = board)

            return Game(gameType = Hakyuu(), Difficulty.EASY, listOf(gameState))
        }
    }

    fun pruebas() {
        create(Board.example())

    }

}

