package com.example.tfg

import android.annotation.SuppressLint
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
import com.example.tfg.data.GameDatabase
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.state.CustomGameViewModelFactory
import com.example.tfg.ui.components.activegame.ActiveGameScreen
import com.example.tfg.ui.theme.TFGTheme
import kotlinx.coroutines.runBlocking

class ActiveGameView : ComponentActivity() {

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var gameId = intent.getLongExtra("gameId", -1)
        val database = GameDatabase.getDatabase(this)
        val dao = database.gameDao()

        database.clearAllTables() //TMP TO TEST
        runBlocking { dao.deletePrimaryKeys() } //TMP TO TEST

        if (gameId == -1L) {
            gameId = runBlocking { GameFactory(dao).exampleHakyuuToDB() }
        }

        val viewModel: ActiveGameViewModel by viewModels{ CustomGameViewModelFactory(gameId, dao) }

        setContent {
            TFGTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TFGTheme {
                        ActiveGameScreen(
                            viewModel = viewModel,
                            modifier = Modifier
                                .background(colorResource(id = R.color.primary_background))
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

