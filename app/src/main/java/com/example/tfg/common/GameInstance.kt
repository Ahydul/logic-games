package com.example.tfg.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell
import com.example.tfg.common.entities.Game
import com.example.tfg.common.entities.GameState
import com.example.tfg.common.entities.relations.MoveWithActions
import com.example.tfg.data.GameDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GameInstance(
    val game: Game,
    val gameStateIds: MutableList<Long>,
    var actualGameState: GameState,
    var moves: MutableList<MoveWithActions>,
    var board: Board,
    val cells: Array<MutableState<Cell>>
)
{
    companion object {
        fun create(gameId: Long, gameDao: GameDao): GameInstance {
            val game = runBlocking { gameDao.getGameById(gameId) }
            val gameStateIds = runBlocking { gameDao.getGameStateIdsByGameId(gameId) }
            if (gameStateIds.isEmpty()) GlobalScope.launch { gameDao.deleteGame(game) }
            val gameStateId = gameStateIds[0]
            val actualGameState = runBlocking { gameDao.getGameStateById(gameStateId) }
            val moves = runBlocking { gameDao.getMovesByGameStateId(gameStateId) }
            val board = runBlocking { gameDao.getBoardByGameStateId(gameStateId) }
            val cells = runBlocking { gameDao.getBoardCells(board.boardId) }.map { mutableStateOf(it) }.toTypedArray()

            return GameInstance(
                game = game,
                gameStateIds = gameStateIds,
                actualGameState = actualGameState,
                moves = moves,
                board = board,
                cells = cells
            )
        }

        fun example(): GameInstance {
            val game = GameFactory.exampleHakyuuGame()
            val gameStateIds = mutableListOf<Long>()
            val actualGameState = GameFactory.exampleGameState(game.gameId)
            val moves = mutableListOf<MoveWithActions>()
            val board = GameFactory.exampleBoard(gameStateId = actualGameState.gameStateId)
            val cells = GameFactory.exampleCells(game.gameTypeEntity.startBoard).map { mutableStateOf(it) }.toTypedArray()

            return GameInstance(
                game = game,
                gameStateIds = gameStateIds,
                actualGameState = actualGameState,
                moves = moves,
                board = board,
                cells = cells
            )
        }
    }


}