package com.example.tfg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.tfg.common.GameFactory
import com.example.tfg.common.IdGenerator
import com.example.tfg.data.DataStoreManager.dataStore
import com.example.tfg.data.GameDatabase
import com.example.tfg.state.CustomMainViewModelFactory
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.mainactivity.MainScreen
import com.example.tfg.ui.theme.TFGTheme
import com.example.tfg.ui.theme.Theme

class MainActivity : ComponentActivity() {

    var viewModel: MainViewModel? = null

    override fun onRestart() {
        viewModel?.setLastPlayedGame(-1)
        super.onRestart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        IdGenerator.initialize(this)

        val database = GameDatabase.getDatabase(this)
        val limitedGameDao = database.limitedGameDao()
        val statsDao = database.statsDao()
        val gameFactory = GameFactory(database.gameDao())

        val vm: MainViewModel by viewModels{
            CustomMainViewModelFactory(limitedGameDao, statsDao, gameFactory, dataStore, applicationContext.filesDir)
        }
        viewModel = vm

        super.onCreate(savedInstanceState)
        setContent {
            val initial = if (isSystemInDarkTheme()) Theme.DARK_MODE else Theme.LIGHT_MODE
            val theme by vm.themeUserSetting.collectAsState(initial = initial)
            TFGTheme(theme = theme) {
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