package com.example.tfg

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.tfg.common.GameFactory
import com.example.tfg.common.IdGenerator
import com.example.tfg.data.GameDatabase
import com.example.tfg.data.LimitedGameDao
import com.example.tfg.state.CustomMainViewModelFactory
import com.example.tfg.state.DarkTheme
import com.example.tfg.state.LocalTheme
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.mainactivity.MainScreen
import com.example.tfg.ui.theme.TFGTheme
import com.example.tfg.ui.theme.Theme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    var viewModel: MainViewModel? = null

    private fun initializeConfiguration(configurationPrefs: SharedPreferences) {
        with (configurationPrefs.edit()) {
            putBoolean("snapshot", true)
            apply() //asynchronous
        }
    }

    override fun onRestart() {
        viewModel?.setLastPlayedGame(-1)
        super.onRestart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        IdGenerator.initialize(this)
        val configurationPrefs = getSharedPreferences("Configuration", Context.MODE_PRIVATE)
        initializeConfiguration(configurationPrefs)

        val database = GameDatabase.getDatabase(this)
        val limitedGameDao = database.limitedGameDao()
        val statsDao = database.statsDao()
        val gameFactory = GameFactory(database.gameDao())

        val vm: MainViewModel by viewModels{ CustomMainViewModelFactory(limitedGameDao, statsDao, gameFactory, configurationPrefs) }
        viewModel = vm

        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme = when (vm.getTheme()) {
                Theme.LIGHT_MODE -> DarkTheme(false)
                Theme.DARK_MODE -> DarkTheme(true)
            }
            CompositionLocalProvider(LocalTheme provides darkTheme) {
                TFGTheme(darkTheme = LocalTheme.current.isDark) {
                    // A surface container using the 'background' color from the theme
                    MainScreen(
                        viewModel = vm,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                    )
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



