package com.example.tfg.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tfg.common.entities.Game

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(game: Game)

    @Update
    suspend fun update(game: Game)

    @Delete
    suspend fun delete(game: Game)

    @Query("SELECT * from game WHERE id = :id")
    suspend fun getById(id: Int): Game // Flow<Game>

    @Query("SELECT * from game ORDER BY startDate ASC")
    suspend fun getAllGames(): List<Game>
}