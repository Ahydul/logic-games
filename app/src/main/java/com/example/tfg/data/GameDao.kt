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
import com.example.tfg.common.entities.WinningStreak
import com.example.tfg.common.entities.relations.BoardCellCrossRef
import com.example.tfg.common.entities.relations.GameStateSnapshot
import com.example.tfg.common.entities.relations.MoveWithActions
import com.example.tfg.games.common.Games
import java.time.LocalDateTime

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
    suspend fun insertGameState(gameState: GameState)
    @Update
    suspend fun updateGameState(gameState: GameState)
    @Delete
    suspend fun deleteGameState(gameState: GameState)
    @Query("DELETE FROM GameState WHERE gameStateId = :gameStateId")
    suspend fun deleteGameStateById(gameStateId: Long)
    @Query("SELECT * FROM GameState WHERE gameStateId = :gameStateId")
    suspend fun getGameStateById(gameStateId: Long): GameState
    @Query("SELECT gameStateId FROM GameState WHERE gameId = :gameId")
    suspend fun getGameStateIdsByGameId(gameId: Long): MutableList<Long>

    @Transaction
    @Query("SELECT * FROM Move WHERE gameStateId = :gameStateId ORDER BY position ASC")
    suspend fun getMovesByGameStateId(gameStateId: Long): MutableList<MoveWithActions>

    @Query("DELETE FROM Move WHERE moveId IN (:movesId)")
    suspend fun deleteMoves(movesId: List<Long>)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMove(move: Move)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertActions(actions: List<Action>)
    @Query("DELETE FROM `Action` WHERE moveId IN (:movesId)")
    suspend fun deleteActionsByMovesPosition(movesId: List<Long>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBoard(board: Board)
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

    @Query("""
        DELETE FROM sqlite_sequence 
        WHERE name = 'Game' 
        or name = 'GameState'
        or name = 'Action'
        or name = 'BoardCellCrossRef'
        or name = 'Cell'
        or name = 'Move'
        or name = 'Board'
    """)
    suspend fun deletePrimaryKeys()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameStateSnapshot(snapshot: GameStateSnapshot)
    @Query("SELECT * FROM GameStateSnapshot WHERE gameStateId = :gameStateId")
    suspend fun getGameStateSnapshotByGameStateId(gameStateId: Long): GameStateSnapshot?
    @Query("SELECT * FROM GameStateSnapshot WHERE gameStateId IN (:gameStateIds)")
    suspend fun getGameStateSnapshotsByGameStateIds(gameStateIds: List<Long>): List<GameStateSnapshot>

    @Insert
    suspend fun insertWinningStreak(winningStreak: WinningStreak)
    @Query("UPDATE winningstreak SET wins = wins + 1 WHERE endDate IS NULL AND gameEnum = :gameEnum")
    suspend fun addOneToActualWinningStreak(gameEnum: Games?): Int
    @Query("UPDATE winningstreak SET endDate = :endDate WHERE endDate IS NULL AND gameEnum = :gameEnum")
    suspend fun endActualWinningStreak(endDate: LocalDateTime, gameEnum: Games)

    @Query("UPDATE winningstreak SET wins = wins + 1 WHERE endDate IS NULL AND gameEnum IS NULL")
    suspend fun addOneToActualGeneralWinningStreak(): Int
    @Query("UPDATE winningstreak SET endDate = :endDate WHERE endDate IS NULL AND gameEnum IS NULL")
    suspend fun endActualGeneralWinningStreak(endDate: LocalDateTime)

}