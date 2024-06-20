package com.example.tfg.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.tfg.common.GameLowerInfo
import com.example.tfg.games.common.Games
import kotlinx.coroutines.flow.Flow

@Dao
interface LimitedGameDao {

    @Query("SELECT gameId, type, difficulty, startDate, numClues, timer, numErrors, seed from Game WHERE gameId = :id")
    suspend fun getGameById(id: Long): GameLowerInfo

    @Query("SELECT EXISTS(SELECT 1 FROM Game WHERE endDate IS NULL)")
    suspend fun existsOnGoingGame(): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM Game WHERE gameId = :id and endDate IS NULL)")
    suspend fun existsOnGoingGameById(id: Long): Boolean

    @Query("SELECT gameId, type, difficulty, startDate, numClues, timer, numErrors, seed  FROM Game WHERE endDate IS NULL and type = :type ORDER BY startDate DESC")
    fun getOnGoingGamesByType(type: Games): Flow<List<GameLowerInfo>>

    @Query("SELECT gameId, type, difficulty, startDate, numClues, timer, numErrors, seed FROM Game WHERE endDate IS NULL ORDER BY startDate DESC")
    fun getOnGoingGames(): Flow<List<GameLowerInfo>>

    @Query("SELECT gameId, type, difficulty, startDate, numClues, timer, numErrors, seed FROM Game WHERE endDate IS NOT NULL and type = :type ORDER BY endDate DESC")
    fun getCompletedGamesByType(type: Games): Flow<List<GameLowerInfo>>

    @Transaction
    @Query("""
        SELECT snapshotFilePath FROM GameStateSnapshot 
        WHERE gameStateId = (SELECT gameStateId FROM gamestate WHERE gameId = :gameId AND position = 0)
    """)
    suspend fun getMainSnapshotFileByGameId(gameId: Long): String?

}