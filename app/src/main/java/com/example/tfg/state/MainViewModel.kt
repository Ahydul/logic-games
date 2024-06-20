package com.example.tfg.state

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.common.GameFactory
import com.example.tfg.common.GameLowerInfo
import com.example.tfg.common.utils.Utils
import com.example.tfg.data.DataStorePreferences
import com.example.tfg.data.LimitedGameDao
import com.example.tfg.data.StatsDao
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.ui.theme.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.LocalDateTime

class MainViewModel(
    private val gameDao: LimitedGameDao,
    private val statsDao: StatsDao,
    private val gameFactory: GameFactory,
    private val dataStore: DataStore<Preferences>,
    private val filesDir: File
) : ViewModel() {

    val themeUserSetting: Flow<Theme> = dataStore.data.map { preferences ->
        Theme.from(preferences[DataStorePreferences.THEME])
    }

    private var lastPlayedGame = getLastPlayedGame()

    private val isLoading = mutableStateOf(false)

//  Main functions

    private fun getLastPlayedGame(): Long? {
        val lastPlayedGame = runBlocking {
            dataStore.data.map { it[DataStorePreferences.LAST_PLAYED_GAME]}.first()
        }
        if (lastPlayedGame != null && runBlocking { !gameDao.existsOnGoingGameById(lastPlayedGame) }) {
            //Game was completed or doesn't exist
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences.remove(DataStorePreferences.LAST_PLAYED_GAME)
                }
            }
        }
        return lastPlayedGame
    }

    fun setLastPlayedGame(id: Long) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DataStorePreferences.LAST_PLAYED_GAME] = id
            }
        }
    }

    private fun showLoading() {
        isLoading.value = true
    }

    private fun hideLoading() {
        isLoading.value = false
    }

    fun isLoading() = isLoading.value

    fun createGame(chosenGame: Games, numRows: Int, numColumns: Int, difficulty: Difficulty, seed: String, context: Context) {
        showLoading()
        val seed = if (seed == "") null else seed.toLongOrNull() ?: seed.hashCode().toLong()
        viewModelScope.launch(Dispatchers.IO) {
            val gameId = gameFactory.createGame(
                chosenGame = chosenGame,
                numRows = numRows,
                numColumns = numColumns,
                difficulty = difficulty,
                seed = seed
            )
            hideLoading()

            Utils.startActiveGameActivity(context, gameId)
        }
    }

    fun setTheme() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val newTheme = when(Theme.from(preferences[DataStorePreferences.THEME])){
                    Theme.DARK_MODE -> Theme.LIGHT_MODE.name
                    Theme.LIGHT_MODE -> Theme.DARK_MODE.name
                }
                preferences[DataStorePreferences.THEME] = newTheme
            }
        }
    }

    fun getOnGoingGamesByType(type: Games): Flow<List<GameLowerInfo>> {
        return gameDao.getOnGoingGamesByType(type)
    }

    fun getOnGoingGames(): Flow<List<GameLowerInfo>> {
        return gameDao.getOnGoingGames()
    }


    fun getMainSnapshotFileByGameId(gameId: Long): Bitmap? {
        return runBlocking {
            Utils.getBitmapFromFile(gameDao.getMainSnapshotFileByGameId(gameId))
        }
    }

    fun getFinalSnapshotFile(gameId: Long, game: Games): Bitmap? {
        return runBlocking {
            Utils.getFinalBitmapFromFile(
                filesDir = filesDir,
                gameId = gameId,
                game = game
            )
        }
    }


    fun noActiveGames(): Boolean {
        return !runBlocking { gameDao.existsOnGoingGame() }
    }

    private fun getGameByIdFromBb(gameId: Long): GameLowerInfo {
        return runBlocking { gameDao.getGameById(gameId) }
    }

    fun getLastPlayedGameInfo(): GameLowerInfo? {
        return lastPlayedGame?.let { getGameByIdFromBb(it) }
    }

    fun getCompletedGames(gameType: Games): Flow<List<GameLowerInfo>> {
        return gameDao.getCompletedGamesByType(type = gameType)
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

    fun getHighestWinningStreak(type: Games?, startDate: LocalDateTime?, difficulty: Difficulty?): Int? {
        return runBlocking {
            if (type == null) statsDao.getHighestGeneralWinningStreakValue(
                difficulty = difficulty,
                startDate = startDate
            )
            else statsDao.getHighestWinningStreakValue(
                startDate = startDate,
                difficulty = difficulty,
                gameEnum = type
            )
        }
    }

    fun getActualWinningStreak(type: Games?, difficulty: Difficulty?): Int? {
        return runBlocking {
            if (type == null) statsDao.getActualGeneralWinningStreak(difficulty)
            else statsDao.getActualWinningStreak(
                difficulty = difficulty,
                gameEnum = type
            )
        }
    }

}