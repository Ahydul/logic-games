package com.example.tfg.data

import com.example.tfg.common.entities.Game

class OfflineGameRepository(private val gameDao: GameDao) : GameRepository {
    override suspend fun insertGame(game: Game) = gameDao.insert(game)

    override suspend fun updateGame(game: Game) = gameDao.update(game)

    override suspend fun deleteGame(game: Game) = gameDao.delete(game)

    override suspend fun getGameById(id: Int) = gameDao.getById(id)

    override suspend fun getAllGames() = gameDao.getAllGames()
}