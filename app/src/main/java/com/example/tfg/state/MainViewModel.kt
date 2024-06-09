package com.example.tfg.state

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.tfg.common.GameFactory
import com.example.tfg.common.GameLowerInfo
import com.example.tfg.common.utils.Utils
import com.example.tfg.data.LimitedGameDao
import com.example.tfg.data.StatsDao
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.ui.theme.Theme
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class MainViewModel(
    private val gameDao: LimitedGameDao,
    private val statsDao: StatsDao,
    private val gameFactory: GameFactory,
    private val configurationPrefs: SharedPreferences
) : ViewModel() {

    private val themeUserSetting = mutableStateOf(
        Theme.from(configurationPrefs.getString("theme", "LIGHT_MODE") ?: Theme.LIGHT_MODE.name)
    )

    private var lastPlayedGame = getLastPlayedGame()

//  Main functions

    private fun getLastPlayedGame(): Long {
        var lastPlayedGame = configurationPrefs.getLong("lastPlayedGame", -1L)
        if (lastPlayedGame != -1L && runBlocking { !gameDao.existsOnGoingGameById(lastPlayedGame) }) {
            //Game was completed or doesn't exist
            lastPlayedGame = -1L
            with(configurationPrefs.edit()) {
                putLong("lastPlayedGame", -1)
                apply()
            }
        }
        return lastPlayedGame
    }

    fun setLastPlayedGame(id: Long) {
        with (configurationPrefs.edit()) {
            putLong("lastPlayedGame", id)
            apply() //asynchronous
        }
    }

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

    fun getTheme() = themeUserSetting.value

    fun setConfigurationTheme(theme: Theme) {
        with (configurationPrefs.edit()) {
            putString("theme", theme.name)
            apply()
        }
    }

    fun setTheme() {
        when(getTheme()){
            Theme.DARK_MODE -> {
                themeUserSetting.value = Theme.LIGHT_MODE
                setConfigurationTheme(Theme.LIGHT_MODE)
            }
            Theme.LIGHT_MODE -> {
                themeUserSetting.value = Theme.DARK_MODE
                setConfigurationTheme(Theme.DARK_MODE)
            }
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

    fun getLastPlayedGameInfo(): GameLowerInfo? {
        return if (lastPlayedGame == -1L) null
            else getGameByIdFromBb(lastPlayedGame)
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