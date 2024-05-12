package com.example.tfg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.tfg.common.Game
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.state.CustomGameViewModelFactory
import com.example.tfg.ui.components.activegame.ActiveGameScreen
import com.example.tfg.ui.theme.TFGTheme

class ActiveGameView : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val game = intent.getParcelableExtra("game")?: Game.example()
        val viewModel: ActiveGameViewModel by viewModels{ CustomGameViewModelFactory(game) }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TFGTheme {
        ActiveGameScreen(
            viewModel = ActiveGameViewModel(Game.example()),
            modifier = Modifier
                .background(colorResource(id = R.color.primary_background))
                .fillMaxWidth()
        )
    }
}
