package com.example.tfg.ui.components.activegame

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tfg.state.ActiveGameViewModel


@Composable
fun ActiveGameScreen(viewModel: ActiveGameViewModel, modifier: Modifier = Modifier) {

    val shape = RoundedCornerShape(8.dp)
    //val game = remember { Game.example() } // Doesnt update on recomposition

    Column(modifier = modifier) {
        TopSection(
            viewModel = viewModel,
            modifier = modifier

                .weight(1f)
        )
        MiddleSection(
            viewModel = viewModel,
            modifier = modifier

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
            modifier = modifier
                .weight(3f)
        )
    }
}
