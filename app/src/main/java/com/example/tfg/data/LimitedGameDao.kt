package com.example.tfg.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.tfg.common.GameLowerInfo
import com.example.tfg.games.common.Games

@Dao
interface LimitedGameDao {

    @Query("SELECT gameId, type, difficulty, startDate, numClues, timer, numErrors from Game WHERE gameId = :id")
    suspend fun getGameById(id: Long): GameLowerInfo

    @Query("SELECT EXISTS(SELECT 1 FROM Game WHERE endDate IS NULL)")
    suspend fun existsOnGoingGame(): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM Game WHERE gameId = :id and endDate IS NULL)")
    suspend fun existsOnGoingGameById(id: Long): Boolean

    @Query("SELECT gameId, type, difficulty, startDate, numClues, timer, numErrors  FROM Game WHERE endDate IS NULL and type = :type ORDER BY startDate DESC")
    suspend fun getOnGoingGamesByType(type: Games): List<GameLowerInfo>

    @Query("SELECT gameId, type, difficulty, startDate, numClues, timer, numErrors FROM Game WHERE endDate IS NULL ORDER BY startDate DESC")
    suspend fun getOnGoingGames(): List<GameLowerInfo>

    @Transaction
    @Query("""
        SELECT snapshotFilePath FROM GameStateSnapshot 
        WHERE gameStateId = (SELECT gameStateId FROM gamestate WHERE gameId = :gameId AND position = 0)
    """)
    suspend fun getMainSnapshotFileByGameId(gameId: Long): String?

}