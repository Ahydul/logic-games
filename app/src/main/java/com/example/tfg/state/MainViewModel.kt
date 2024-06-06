package com.example.tfg.state

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.tfg.common.Difficulty
import com.example.tfg.common.GameFactory
import com.example.tfg.common.entities.Game
import com.example.tfg.common.utils.Utils
import com.example.tfg.data.GameDao
import com.example.tfg.games.Games
import kotlinx.coroutines.runBlocking

class MainViewModel(
    private val gameDao: GameDao,
    private var lastPlayedGame: Long,
    private val preview: Boolean
) : ViewModel() {

    fun createGame(chosenGame: Games, numRows: Int, numColumns: Int, difficulty: Difficulty): Long {
        return runBlocking {
            GameFactory(gameDao).createGame(
                chosenGame = chosenGame,
                numRows = numRows,
                numColumns = numColumns,
                difficulty = difficulty
            )
        }
    }
/*
    fun getOnGoingGamesByType(type: Games): List<Game> {
        return runBlocking { gameDao.getOnGoingGameByType(type) }
    }

 */

    fun getMainSnapshotFileByGameId(gameId: Long): Bitmap? {
        return runBlocking {
            Utils.getBitmapFromFile(gameDao.getMainSnapshotFileByGameId(gameId))
        }
    }


    fun noActiveGames(): Boolean {
        if (preview) return false
        return !runBlocking { gameDao.existsOnGoingGame() }
    }

    private fun getGameByIdFromBb(gameId: Long): Game {
        return runBlocking { gameDao.getGameById(gameId) }
    }

    fun getLastPlayedGame(): Game? {
        return if (lastPlayedGame == -1L) null
            else getGameByIdFromBb(lastPlayedGame)
    }

    fun setLastPlayedGame(lastPlayedGame: Long){
        this.lastPlayedGame = lastPlayedGame
    }

}