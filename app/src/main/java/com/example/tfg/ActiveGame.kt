package com.example.tfg

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.common.GameInstance
import com.example.tfg.common.IdGenerator
import com.example.tfg.data.GameDatabase
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.state.CustomGameViewModelFactory
import com.example.tfg.state.DarkTheme
import com.example.tfg.state.LocalTheme
import com.example.tfg.ui.components.activegame.ActiveGameScreen
import com.example.tfg.ui.theme.TFGTheme
import com.example.tfg.ui.theme.Theme

class ActiveGameView : ComponentActivity() {
    private var viewModel: ActiveGameViewModel? = null

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameId = intent.getLongExtra("gameId", -1)
        val database = GameDatabase.getDatabase(this)
        val dao = database.gameDao()

        val sharedPref = getSharedPreferences("Configuration", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putLong("lastPlayedGame", gameId)
            apply() //asynchronous
        }
        val snapshotsAllowed = sharedPref.getBoolean("snapshot", false)

        val gameInstance = GameInstance.create(gameId, dao)
        val vm: ActiveGameViewModel by viewModels{ CustomGameViewModelFactory(gameInstance, dao, snapshotsAllowed) }
        vm.setFilesDirectory(applicationContext.filesDir)
        viewModel = vm

        setContent {
            val darkTheme = when (Theme.from(sharedPref.getString("theme", "LIGHT_MODE") ?: Theme.LIGHT_MODE.name)) {
                Theme.LIGHT_MODE -> DarkTheme(false)
                Theme.DARK_MODE -> DarkTheme(true)
            }
            CompositionLocalProvider(LocalTheme provides darkTheme) {
                TFGTheme(darkTheme = LocalTheme.current.isDark) {
                    ActiveGameScreen(
                        viewModel = vm,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            viewModel?.takeSnapshot()
            viewModel?.pauseGame()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel?.pauseGame()
    }
}


@Preview(showBackground = true)
@Composable
fun ActiveGameScreenPreview() {
    IdGenerator.initialize(LocalContext.current)
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
    IdGenerator.initialize(LocalContext.current)
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
    IdGenerator.initialize(LocalContext.current)
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
    IdGenerator.initialize(LocalContext.current)
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
