package com.example.tfg.ui.components.mainactivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.tfg.R
import com.example.tfg.common.utils.Timer
import com.example.tfg.common.utils.Utils
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.common.CustomFilledButton

@Composable
fun HomeButtons(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    goGamesScreen: () -> Unit,
    goOnGoingGames: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (viewModel.noActiveGames()){
            val newGameLabel = stringResource(id = R.string.new_game)
            val noActiveGamesFound = stringResource(id = R.string.no_active_games_found)
            HomeButton(
                onClick = goGamesScreen,
                mainText = newGameLabel,
                secondaryText = noActiveGamesFound
            )
        }
        else {
            val continueLabel = stringResource(id = R.string._continue)
            val game = viewModel.getLastPlayedGame()

            if (game!= null) {
                val context = LocalContext.current
                val secondaryText =
                    game.gameTypeEntity.type.title + " - " +
                    Timer.formatTime(game.timer) + " - " +
                    game.difficulty.toString(context = context)

                HomeButton(
                    onClick = { Utils.startActiveGameActivity(context, game.gameId) },
                    mainText = continueLabel,
                    secondaryText = secondaryText
                )
            }

            val otherGamesInProgressLabel = stringResource(id = R.string.other_games_in_progress)
            HomeButton(
                onClick = goOnGoingGames,
                mainText = otherGamesInProgressLabel
            )
        }
    }
}

@Composable
fun HomeButton(onClick: () -> Unit, mainText: String, secondaryText: String? = null) {
    val buttonModifier = Modifier.fillMaxWidth(0.8f)
    val textModifier = Modifier.fillMaxWidth(0.7f)
    CustomFilledButton(
        onClick = onClick,
        color = colorResource(id = R.color.board_grid),
        borderColor = colorResource(id = R.color.board_grid2),
        textColor = colorResource(id = R.color.primary_color),
        mainText = mainText,
        secondaryText = secondaryText,
        fontSize = 22.sp,
        modifier = buttonModifier,
        textModifier = textModifier
    )
}
