package com.example.tfg.ui.components.mainactivity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.common.MainHeader

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    goGamesScreen: () -> Unit,
    goOnGoingGames: () -> Unit
) {
    val mod = Modifier.fillMaxWidth()
    Column(modifier = modifier
        .fillMaxHeight()
        .padding(top = 8.dp, start = 6.dp, end = 6.dp)
    ) {
        Box(modifier = modifier.weight(3f)) {
            MainHeader(viewModel = viewModel, modifier = modifier)
        }
        Spacer(modifier = mod.weight(16f))
        HomeButtons(
            modifier = mod.weight(10f),
            viewModel = viewModel,
            goGamesScreen = goGamesScreen,
            goOnGoingGames = goOnGoingGames
        )
        Spacer(modifier = mod.weight(8f))
    }
}
