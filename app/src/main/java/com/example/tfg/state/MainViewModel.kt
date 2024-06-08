package com.example.tfg.state

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.tfg.games.common.Difficulty
import com.example.tfg.common.GameFactory
import com.example.tfg.common.GameLowerInfo
import com.example.tfg.common.utils.Utils
import com.example.tfg.data.LimitedGameDao
import com.example.tfg.data.StatsDao
import com.example.tfg.games.common.Games
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class MainViewModel(
    private val gameDao: LimitedGameDao,
    private val statsDao: StatsDao,
    private val gameFactory: GameFactory,
    private var lastPlayedGame: Long
) : ViewModel() {

//  Main functions

    fun createGame(chosenGame: Games, numRows: Int, numColumns: Int, difficulty: Difficulty): Long {
        return runBlocking {
            gameFactory.createGame(
                chosenGame = chosenGame,
                numRows = numRows,
                numColumns = numColumns,
                difficulty = difficulty
            )
        }
    }

    fun getOnGoingGamesByType(type: Games): List<GameLowerInfo> {
        return runBlocking { gameDao.getOnGoingGamesByType(type) }
    }

    fun getOnGoingGames(): List<GameLowerInfo> {
        return runBlocking { gameDao.getOnGoingGames() }
    }


    fun getMainSnapshotFileByGameId(gameId: Long): Bitmap? {
        return runBlocking {
            Utils.getBitmapFromFile(gameDao.getMainSnapshotFileByGameId(gameId))
        }
    }

    fun noActiveGames(): Boolean {
        return !runBlocking { gameDao.existsOnGoingGame() }
    }

    private fun getGameByIdFromBb(gameId: Long): GameLowerInfo {
        return runBlocking { gameDao.getGameById(gameId) }
    }

    fun getLastPlayedGame(): GameLowerInfo? {
        return if (lastPlayedGame == -1L) null
            else getGameByIdFromBb(lastPlayedGame)
    }

    fun setLastPlayedGame(lastPlayedGame: Long){
        this.lastPlayedGame = lastPlayedGame
    }


// Stats functions

    fun getNumberGamesStarted(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime? = null): Int {
        return runBlocking {
            statsDao.getNumberOfGamesStarted(
                type = type,
                difficulty = difficulty,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    fun getNumberGamesEnded(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime = LocalDateTime.now()): Int {
        return runBlocking {
            statsDao.getNumberOfGamesEnded(
                type = type,
                difficulty = difficulty,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    fun getNumberGamesWon(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime = LocalDateTime.now()): Int {
        return runBlocking {
            statsDao.getNumberOfGames(
                type = type,
                difficulty = difficulty,
                startDate = startDate,
                endDate = endDate,
                playerWon = true
            )
        }
    }

    fun getWinRate(gamesWon: Int, games: Int): Double {
        return if (games == 0) 0.0 else(gamesWon/games.toDouble()) * 100
    }


    fun getAbsoluteTotalTime(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?): Int? {
        return runBlocking {
            statsDao.getAbsoluteTotalTime(
                type = type,
                difficulty = difficulty,
                startDate = startDate
            )
        }
    }

    fun getTotalTime(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime? = LocalDateTime.now(), playerWon: Boolean): Int? {
        return runBlocking {
            statsDao.getTotalTime(
                type = type,
                difficulty = difficulty,
                startDate = startDate,
                endDate = endDate,
                playerWon = playerWon
            )
        }
    }

    fun getBestTime(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime? = LocalDateTime.now()): Int? {
        return runBlocking {
            statsDao.getBestTime(
                type = type,
                difficulty = difficulty,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    fun getMeanTime(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime? = LocalDateTime.now()): Double? {
        return runBlocking {
            statsDao.getMeanTime(
                type = type,
                difficulty = difficulty,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    fun getErrorCount(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime? = LocalDateTime.now()): Int? {
        return runBlocking {
            statsDao.getTotalErrorCount(
                type = type,
                difficulty = difficulty,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    fun getErrorMeanCount(type: Games?, difficulty: Difficulty?, startDate: LocalDateTime?, endDate: LocalDateTime? = LocalDateTime.now()): Double? {
        return runBlocking {
            statsDao.getMeanErrorCount(
                type = type,
                difficulty = difficulty,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    fun getHighestWinningStreak(type: Games?, startDate: LocalDateTime?): Int? {
        return runBlocking {
            if (type == null) statsDao.getHighestGeneralWinningStreakValue(
                startDate = startDate
            )
            else statsDao.getHighestWinningStreakValue(
                startDate = startDate,
                gameEnum = type
            )
        }
    }

    fun getActualWinningStreak(type: Games?): Int? {
        return runBlocking {
            if (type == null) statsDao.getActualGeneralWinningStreak()
            else statsDao.getActualWinningStreak(
                gameEnum = type
            )
        }
    }

}