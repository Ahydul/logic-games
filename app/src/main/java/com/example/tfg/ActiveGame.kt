package com.example.tfg

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.common.GameInstance
import com.example.tfg.common.IdGenerator
import com.example.tfg.data.DataStoreManager.dataStore
import com.example.tfg.data.DataStorePreferences
import com.example.tfg.data.FakeContext
import com.example.tfg.data.GameDatabase
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.state.CustomGameViewModelFactory
import com.example.tfg.ui.components.activegame.ActiveGameScreen
import com.example.tfg.ui.theme.TFGTheme
import com.example.tfg.ui.theme.Theme
import kotlinx.coroutines.launch

class ActiveGameView : ComponentActivity() {
    private var viewModel: ActiveGameViewModel? = null

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameId = intent.getLongExtra("gameId", -1)
        val database = GameDatabase.getDatabase(this)
        val dao = database.gameDao()

        lifecycleScope.launch {
            dataStore.edit { preferences ->
                preferences[DataStorePreferences.LAST_PLAYED_GAME] = gameId
            }
        }

        val gameInstance = GameInstance.create(gameId, dao)
        val vm: ActiveGameViewModel by viewModels{ CustomGameViewModelFactory(gameInstance, dao, dataStore, applicationContext.filesDir) }
        viewModel = vm

        setContent {
            val initial = if (isSystemInDarkTheme()) Theme.DARK_MODE else Theme.LIGHT_MODE
            val theme by vm.themeUserSetting.collectAsState(initial = initial)
            TFGTheme(theme = theme) {
                ActiveGameScreen(
                    viewModel = vm,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel?.takeSnapshot()
        viewModel?.pauseGame()
    }
}


@Preview(showBackground = true)
@Composable
fun ActiveGameScreenPreview() {
    IdGenerator.initialize(FakeContext())
    val database = GameDatabase.getInMemoryDatabase(LocalContext.current)
    val gameInstance = GameInstance.example()
    val viewModel: ActiveGameViewModel = viewModel(factory = CustomGameViewModelFactory(gameInstance, database.gameDao()))

    TFGTheme {
        ActiveGameScreen(
            viewModel = viewModel,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
        )
    }
}


@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
fun ActiveGameScreenPreview2() {
    IdGenerator.initialize(FakeContext())
    val database = GameDatabase.getInMemoryDatabase(LocalContext.current)
    val gameInstance = GameInstance.example()
    val viewModel: ActiveGameViewModel = viewModel(factory = CustomGameViewModelFactory(gameInstance, database.gameDao()))

    TFGTheme {
        ActiveGameScreen(
            viewModel = viewModel,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, device = Devices.FOLDABLE)
@Composable
fun ActiveGameScreenPreview3() {
    IdGenerator.initialize(FakeContext())
    val database = GameDatabase.getInMemoryDatabase(LocalContext.current)
    val gameInstance = GameInstance.example()
    val viewModel: ActiveGameViewModel = viewModel(factory = CustomGameViewModelFactory(gameInstance, database.gameDao()))

    TFGTheme {
        ActiveGameScreen(
            viewModel = viewModel,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, device = Devices.NEXUS_10)
@Composable
fun ActiveGameScreenPreview4() {
    IdGenerator.initialize(FakeContext())
    val database = GameDatabase.getInMemoryDatabase(LocalContext.current)
    val gameInstance = GameInstance.example()
    val viewModel: ActiveGameViewModel = viewModel(factory = CustomGameViewModelFactory(gameInstance, database.gameDao()))

    TFGTheme {
        ActiveGameScreen(
            viewModel = viewModel,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
        )
    }
}
