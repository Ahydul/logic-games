package com.example.tfg.state

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.tfg.common.Difficulty
import com.example.tfg.common.GameFactory
import com.example.tfg.common.entities.Game
import com.example.tfg.data.GameDao
import com.example.tfg.games.Games
import kotlinx.coroutines.runBlocking

class MainViewModel(private val gameDao: GameDao, private val sharedPref: SharedPreferences, private val preview: Boolean = false) : ViewModel() {
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

    fun noActiveGames(): Boolean {
        if (preview) return false
        return !runBlocking { gameDao.existsAGame()}
    }

    private fun getGameByIdFromBb(gameId: Long): Game {
        return runBlocking { gameDao.getGameById(gameId) }
    }

    fun getLastPlayedGame(): Game? {
        val gameId = sharedPref.getLong("lastPlayedGame", -1)
        return if (gameId == -1L) null
        else getGameByIdFromBb(gameId)
    }

}