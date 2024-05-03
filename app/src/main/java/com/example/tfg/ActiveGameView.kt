package com.example.tfg

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tfg.common.Game
import com.example.tfg.ui.components.activegame.Board
import com.example.tfg.ui.components.activegame.BottomActionRow
import com.example.tfg.ui.components.activegame.TopActionRow
import com.example.tfg.games.Games
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.activegame.ActiveGameScreen
import com.example.tfg.ui.components.activegame.TopSection
import com.example.tfg.ui.theme.TFGTheme

class ActiveGameView : ComponentActivity() {
    val viewModel: ActiveGameViewModel = ActiveGameViewModel(Game.example())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

val addDebugBorder = Modifier.border(
    width = 0.5.dp,
    color = Color.Red,
)

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

