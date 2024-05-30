package com.example.tfg.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tfg.common.entities.Action
import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell
import com.example.tfg.common.entities.Game
import com.example.tfg.common.entities.GameState
import com.example.tfg.common.entities.Move
import com.example.tfg.common.entities.relations.BoardCellCrossRef
import com.example.tfg.common.entities.relations.MoveWithActions

@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGame(game: Game): Long
    @Update
    suspend fun updateGame(game: Game)
    @Query("DELETE FROM Game WHERE gameId = :gameId")
    suspend fun deleteGame(gameId: Int)
    @Delete
    suspend fun deleteGame(game: Game)
    @Query("SELECT * from Game WHERE gameId = :id")
    suspend fun getGameById(id: Long): Game
    @Query("SELECT * from Game ORDER BY startDate ASC")
    suspend fun getAllGames(): List<Game>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGameState(gameState: GameState): Long
    @Update
    suspend fun updateGameState(gameState: GameState)
    @Query("SELECT * FROM GameState WHERE gameStateId = :gameStateId")
    suspend fun getGameStateById(gameStateId: Long): GameState
    @Query("SELECT gameStateId FROM GameState WHERE gameId = :gameId")
    suspend fun getGameStateIdsByGameId(gameId: Long): List<Long>

    @Transaction
    @Query("SELECT * FROM Move WHERE gameStateId = :gameStateId ORDER BY position DESC")
    suspend fun getMovesByGameStateId(gameStateId: Long): List<MoveWithActions>
    @Transaction
    @Query("SELECT * FROM Move WHERE moveId = :moveId")
    suspend fun getMove(moveId: Long): MoveWithActions

    @Query("DELETE FROM Move WHERE moveId = :moveId")
    suspend fun deleteMove(moveId: Long)
    @Query("DELETE FROM Move WHERE moveId IN (:moveIds)")
    suspend fun deleteMoves(moveIds: List<Long>)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMove(move: Move): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertActions(actions: List<Action>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBoard(board: Board): Long
    @Transaction
    @Query("SELECT * FROM Board WHERE gameStateId = :gameStateId")
    suspend fun getBoardByGameStateId(gameStateId: Long): Board

    @Query("""
        SELECT Cell.* FROM Cell
        INNER JOIN BoardCellCrossRef ON Cell.cellId = BoardCellCrossRef.cellId
        WHERE BoardCellCrossRef.boardId = :boardId
        ORDER BY BoardCellCrossRef.cellPosition
    """)
    suspend fun getBoardCells(boardId: Long): List<Cell>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCell(cell: Cell): Long
    @Update
    suspend fun updateCell(cell: Cell)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBoardCellCrossRef(boardCellCrossRef: BoardCellCrossRef): Long
}