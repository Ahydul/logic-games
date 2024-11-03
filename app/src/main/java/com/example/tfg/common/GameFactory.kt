package com.example.tfg.common

import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell
import com.example.tfg.common.entities.Game
import com.example.tfg.common.entities.GameState
import com.example.tfg.common.entities.relations.BoardCellCrossRef
import com.example.tfg.data.GameDao
import com.example.tfg.games.common.AbstractGame
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.games.hakyuu.Hakyuu
import com.example.tfg.games.kendoku.Kendoku

class GameFactory(private val gameDao: GameDao) {

    suspend fun createGame(
        chosenGame: Games,
        difficulty: Difficulty,
        numColumns: Int,
        numRows: Int,
        seed: Long? = (Math.random() * 10000000000).toLong()
    ): Long {
        val seed = seed ?: (Math.random() * 10000000000).toLong()
        // Initialize gameType
        val abstractGame: AbstractGame = when (chosenGame) {
            Games.HAKYUU -> Hakyuu.create(
                numRows = numRows,
                numColumns = numColumns,
                seed = seed,
                difficulty = difficulty
            )
            Games.KENDOKU -> Kendoku.create(
                size = numColumns,
                seed = seed,
                difficulty = difficulty
            )
        }

        return create(
            chosenGame = chosenGame,
            abstractGame = abstractGame,
            difficulty = abstractGame.score.getDifficulty(),
            numColumns = numColumns,
            numRows = if (chosenGame == Games.KENDOKU) numColumns else numRows
        )
    }

    private suspend fun create(chosenGame: Games, abstractGame: AbstractGame, difficulty: Difficulty, numColumns: Int, numRows: Int): Long {
        // Insert AbstractGame
        when (chosenGame) {
            Games.HAKYUU -> gameDao.insertHakyuuGame(abstractGame as Hakyuu)
            Games.KENDOKU -> gameDao.insertKendokuGame(abstractGame as Kendoku)
        }

        // Create game
        val game = Game.create(gameType = chosenGame, abstractGameId = abstractGame.id, difficulty = difficulty, seed = abstractGame.seed)
        val gameId = gameDao.insertGame(game)

        // Create GameState
        val gameState = GameState(gameId = gameId, position = 0)
        gameDao.insertGameState(gameState)

        // Create board
        val board = Board(
            numRows = numRows,
            numColumns = numColumns,
            gameStateId = gameState.gameStateId
        )
        gameDao.insertBoard(board)

        // Initialize cells
        val cells = abstractGame.startBoard.map { Cell.initializeBoardCell(it) }.toTypedArray()
        cells.forEachIndexed { index, cell ->
            gameDao.insertCell(cell)
            val crossRef = BoardCellCrossRef(boardId = board.boardId, cellId = cell.cellId, cellPosition = index)
            gameDao.insertBoardCellCrossRef(crossRef)
        }

        return gameId
    }


    companion object {
        fun exampleHakyuu(): Hakyuu {
            val numColumns = 8
            val numRows = 8

            val seed = 1L

            return Hakyuu.createTesting(
                numRows = numRows,
                numColumns = numColumns,
                seed = seed,
                startBoard = START_STR,
                completedBoard = COMPLETED_STR,
                boardRegions = REGION_STR
            )
        }

        fun exampleHakyuuGame() = Game.create(Games.HAKYUU, 0, Difficulty.EASY, 0)
        fun exampleGameState(gameId: Long) = GameState(gameStateId = 0, gameId = gameId, position = 0)
        fun exampleBoard(gameStateId: Long) = Board(boardId = 0, numRows = 8, numColumns = 8, gameStateId = gameStateId)
        fun exampleCells(cellArray: IntArray) = cellArray.mapIndexed { index, value ->
            when (index) {
                0 -> Cell.exampleWithNote()
                2 -> Cell.exampleError()
                8 -> Cell.exampleBackgroundError()
                11 -> Cell.exampleBackgroundErrorWithError()
                else -> Cell.exampleCell(value)
            }
        }.toTypedArray()

        private const val COMPLETED_STR =
            "1 4 1 2 3 1 2 1\n" +
            "3 1 2 1 4 2 1 3\n" +
            "5 2 7 3 6 1 4 5\n" +
            "1 6 1 2 1 5 2 1\n" +
            "2 3 5 1 2 4 3 2\n" +
            "3 5 1 4 1 2 1 3\n" +
            "1 2 4 1 3 6 5 4\n" +
            "2 1 3 5 4 1 6 1"

        const val REGION_STR =
            "5:[(0,7)]\n" +
            "7:[(1,3)]\n" +
            "10:[(3,0)]\n" +
            "11:[(3,2)]\n" +
            "12:[(3,7)]\n" +
            "17:[(5,4)]\n" +
            "19:[(6,0)]\n" +
            "21:[(7,7)]\n" +
            "2:[(0,2), (0,3)]\n" +
            "4:[(0,5), (0,6)]\n" +
            "6:[(1,1), (1,2)]\n" +
            "9:[(2,3), (3,3), (3,4)]\n" +
            "20:[(7,0), (7,1), (7,2)]\n" +
            "14:[(4,4), (4,5), (4,6), (5,6)]\n" +
            "8:[(1,6), (1,7), (2,6), (2,7), (3,6)]\n" +
            "13:[(4,0), (4,1), (4,2), (4,3), (5,3)]\n" +
            "16:[(5,0), (5,1), (5,2), (6,1), (6,2)]\n" +
            "3:[(0,4), (1,4), (1,5), (2,4), (2,5), (3,5)]\n" +
            "15:[(4,7), (5,7), (6,6), (6,7), (7,5), (7,6)]\n" +
            "18:[(5,5), (6,3), (6,4), (6,5), (7,3), (7,4)]\n" +
            "1:[(0,0), (0,1), (1,0), (2,0), (2,1), (2,2), (3,1)]"

        const val START_STR =
            "- 4 - - 3 1 - -\n" +
            "- - 2 - - 2 - -\n" +
            "- - - - - - - 5\n" +
            "- - - - - - - -\n" +
            "- - - - - 4 - -\n" +
            "3 - - - - - - -\n" +
            "- - 4 - - 6 - -\n" +
            "- - 3 5 - - 6 -"

    }

}