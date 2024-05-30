package com.example.tfg.common

import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell
import com.example.tfg.common.entities.Game
import com.example.tfg.common.entities.GameState
import com.example.tfg.common.entities.relations.BoardCellCrossRef
import com.example.tfg.data.GameDao
import com.example.tfg.games.GameType
import com.example.tfg.games.Games
import com.example.tfg.games.hakyuu.Hakyuu

private const val COMPLETED_STR =
    "1 4 1 2 3 1 2 1\n" +
    "3 1 2 1 4 2 1 3\n" +
    "5 2 7 3 6 1 4 5\n" +
    "1 6 1 2 1 5 2 1\n" +
    "2 3 5 1 2 4 3 2\n" +
    "3 5 1 4 1 2 1 3\n" +
    "1 2 4 1 3 6 5 4\n" +
    "2 1 3 5 4 1 6 1"

private const val REGION_STR =
    "5:[(7,0)]\n" +
    "7:[(3,1)]\n" +
    "10:[(0,3)]\n" +
    "11:[(2,3)]\n" +
    "12:[(7,3)]\n" +
    "17:[(4,5)]\n" +
    "19:[(0,6)]\n" +
    "21:[(7,7)]\n" +
    "2:[(2,0), (3,0)]\n" +
    "4:[(5,0), (6,0)]\n" +
    "6:[(1,1), (2,1)]\n" +
    "9:[(3,2), (3,3), (4,3)]\n" +
    "20:[(0,7), (1,7), (2,7)]\n" +
    "14:[(4,4), (5,4), (6,4), (6,5)]\n" +
    "8:[(6,1), (7,1), (6,2), (7,2), (6,3)]\n" +
    "13:[(0,4), (1,4), (2,4), (3,4), (3,5)]\n" +
    "16:[(0,5), (1,5), (2,5), (1,6), (2,6)]\n" +
    "3:[(4,0), (4,1), (5,1), (4,2), (5,2), (5,3)]\n" +
    "15:[(7,4), (7,5), (6,6), (7,6), (5,7), (6,7)]\n" +
    "18:[(5,5), (3,6), (4,6), (5,6), (3,7), (4,7)]\n" +
    "1:[(0,0), (1,0), (0,1), (0,2), (1,2), (2,2), (1,3)]"

private const val START_STR =
    "- 4 - - 3 1 - -\n" +
    "- - 2 - - 2 - -\n" +
    "- - - - - - - 5\n" +
    "- - - - - - - -\n" +
    "- - - - - 4 - -\n" +
    "3 - - - - - - -\n" +
    "- - 4 - - 6 - -\n" +
    "- - 3 5 - - 6 -"

class GameFactory(private val gameDao: GameDao) {

    suspend fun createGame(
        chosenGame: Games,
        difficulty: Difficulty,
        numColumns: Int,
        numRows: Int,
        seed: Long? = (Math.random() * 10000000000).toLong()
    ): Long {
        // Initialize gameType
        val gameType: GameType = when (chosenGame) {
            Games.HAKYUU -> Hakyuu(
                numRows = numRows,
                numColumns = numColumns,
                seed = seed ?: (Math.random() * 10000000000).toLong()
            )
        }

        // Create game board
        gameType.createGame()

        return create(
            gameType = gameType,
            difficulty = difficulty,
            numColumns = numColumns,
            numRows = numRows
        )
    }

    private suspend fun create(gameType: GameType, difficulty: Difficulty, numColumns: Int, numRows: Int): Long {
        // Create game
        val game = Game.create(gameType = gameType, difficulty = difficulty)
        val gameId = gameDao.insertGame(game)

        // Create GameState
        val gameState = GameState(gameId = gameId)
        val gameStateId = gameDao.insertGameState(gameState)

        // Create board
        val board = Board(
            numRows = numRows,
            numColumns = numColumns,
            gameStateId = gameStateId
        )
        val boardId = gameDao.insertBoard(board)

        // Initialize cells
        val cells = gameType.startBoard.map { Cell.create(it) }.toTypedArray()
        cells.forEachIndexed { index, cell ->
            val cellId = gameDao.insertCell(cell)
            val crossRef = BoardCellCrossRef(boardId = boardId, cellId = cellId, cellPosition = index)
            gameDao.insertBoardCellCrossRef(crossRef)
        }

        return gameId
    }

    suspend fun exampleHakyuuToDB(): Long {
        val numColumns = 8
        val numRows = 8

        return create(
            gameType = exampleHakyuu(),
            difficulty = Difficulty.EASY,
            numColumns = numColumns,
            numRows = numRows
        )
    }


    companion object {
        fun exampleHakyuu(): Hakyuu {
            val numColumns = 8
            val numRows = 8

            val seed = 1L

            return Hakyuu.create(
                numRows = numRows,
                numColumns = numColumns,
                seed = seed,
                startBoard = START_STR,
                completedBoard = COMPLETED_STR,
                regions = REGION_STR
            )
        }
    }

}