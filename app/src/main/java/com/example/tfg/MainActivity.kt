package com.example.tfg

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.tfg.common.GameFactory
import com.example.tfg.common.IdGenerator
import com.example.tfg.data.GameDatabase
import com.example.tfg.data.LimitedGameDao
import com.example.tfg.state.CustomMainViewModelFactory
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.mainactivity.MainScreen
import com.example.tfg.ui.theme.TFGTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    var viewModel: MainViewModel? = null

    private fun initializeConfiguration(configurationPrefs: SharedPreferences) {
        with (configurationPrefs.edit()) {
            putBoolean("snapshot", true)
            apply() //asynchronous
        }
    }

    private fun getLastPlayedGame(dao: LimitedGameDao, configurationPrefs: SharedPreferences): Long {
        var lastPlayedGame = configurationPrefs.getLong("lastPlayedGame", -1L)
        if (lastPlayedGame != -1L && runBlocking { !dao.existsOnGoingGameById(lastPlayedGame) }) {
            //Game was completed or doesn't exist
            lastPlayedGame = -1L
            with(configurationPrefs.edit()) {
                putLong("lastPlayedGame", -1)
                apply()
            }
        }
        return lastPlayedGame
    }

    override fun onRestart() {
        val dao = GameDatabase.getDatabase(this).limitedGameDao()
        val configurationPrefs = getSharedPreferences("Configuration", Context.MODE_PRIVATE)
        val lastPlayedGame = getLastPlayedGame(dao, configurationPrefs)
        viewModel?.setLastPlayedGame(lastPlayedGame)
        super.onRestart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        IdGenerator.initialize(this)
        val configurationPrefs = getSharedPreferences("Configuration", Context.MODE_PRIVATE)
        initializeConfiguration(configurationPrefs)

        val database = GameDatabase.getDatabase(this)
        val limitedGameDao = database.limitedGameDao()
        val statsDao = database.statsDao()
        val lastPlayedGame = getLastPlayedGame(limitedGameDao, configurationPrefs)
        val gameFactory = GameFactory(database.gameDao())
        val vm: MainViewModel by viewModels{ CustomMainViewModelFactory(limitedGameDao, statsDao, gameFactory, lastPlayedGame) }
        viewModel = vm

        super.onCreate(savedInstanceState)
        setContent {
            TFGTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TFGTheme {
                        MainScreen(
                            viewModel = vm,
                            modifier = Modifier
                                .background(colorResource(id = R.color.background))
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val database = GameDatabase.getInMemoryDatabase(LocalContext.current)
    val viewModel: MainViewModel = viewModel(factory = CustomMainViewModelFactory(database.limitedGameDao(), -1, true))

    TFGTheme {
        MainScreen(
            viewModel = viewModel,
            modifier = Modifier
                .background(colorResource(id = R.color.primary_background))
                .fillMaxWidth()
        )
    }
}
 */



