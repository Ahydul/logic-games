package com.example.tfg.data

import com.example.tfg.common.entities.Game

interface GameRepository {
    suspend fun insertGame(game: Game)

    suspend fun updateGame(game: Game)

    suspend fun deleteGame(game: Game)

    suspend fun getGameById(id: Int) : Game?

    suspend fun getAllGames() : List<Game>
}