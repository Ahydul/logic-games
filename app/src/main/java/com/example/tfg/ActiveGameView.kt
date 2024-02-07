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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tfg.common.Board
import com.example.tfg.common.Game
import com.example.tfg.components.activegame.Board
import com.example.tfg.components.activegame.BottomActionRow
import com.example.tfg.components.activegame.TopActionRow
import com.example.tfg.games.Games
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.theme.TFGTheme

class ActiveGameView : ComponentActivity() {
    val viewModel: ActiveGameViewModel = ActiveGameViewModel()

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

@Composable
fun ActiveGameScreen(viewModel: ActiveGameViewModel, modifier: Modifier = Modifier) {

    val shape = RoundedCornerShape(8.dp)
    val game = remember { Game.example() } // Doesnt update on recomposition

    Column(modifier = modifier) {
        TopSection(
            viewModel = viewModel,
            modifier = modifier
                .then(addDebugBorder)
                .weight(1f)
        )
        MiddleSection(
            viewModel = viewModel,
            modifier = modifier
                .then(addDebugBorder)
                .border(
                    width = 1.dp,
                    color = Color.Black,
                )
                .aspectRatio(ratio = 1f)
                .padding(4.dp)
                .clip(shape)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = shape
                )
                .weight(4f)
        )
        BottomSection(
            viewModel = viewModel,
            gameType = game.gameType.type,
            modifier = modifier
                .then(addDebugBorder)
                .weight(3f)
        )
    }
}

@Composable
fun TopSection(viewModel: ActiveGameViewModel, modifier: Modifier = Modifier) {
    Log.d("TAG", "TOPcurrentRecomposeScope $currentRecomposeScope")

    Column(modifier = modifier) {
        Row {
            Text(
                text = ""
            )
        }
        Row {
            Text(
                text = "Dificultad"
            )
            Text(
                text = "Errores"
            )
            Text(
                text = "Pistas"
            )
            Text(
                text = "Tiempo"
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomSection(
    viewModel: ActiveGameViewModel,
    gameType: Games,
    modifier: Modifier = Modifier)
{
    Log.d("TAG", "BOTTOMcurrentRecomposeScope $currentRecomposeScope")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Actions
        TopActionRow(
            viewModel = viewModel,
            modifier = Modifier
                .weight(2f)
                .padding(2.dp)
                .then(addDebugBorder)
        )
        BottomActionRow(
            viewModel = viewModel,
            gameType = gameType,
            modifier = Modifier
                .padding(4.dp)
                .weight(5f)
                .then(addDebugBorder)
        )
        Spacer(modifier = Modifier.weight(2f))
    }
}

@Composable
fun MiddleSection(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    Log.d("TAG", "MIDDLEcurrentRecomposeScope $currentRecomposeScope")

    Box (
        contentAlignment = Alignment.Center,
        modifier = modifier
    ){
        Board(viewModel=viewModel, modifier = Modifier)
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
            viewModel = ActiveGameViewModel(),
            modifier = Modifier
            .background(colorResource(id = R.color.primary_background))
            .fillMaxWidth()
        )
    }
}

