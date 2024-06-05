package com.example.tfg.ui.components.mainactivity

import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.tfg.R
import com.example.tfg.common.Difficulty
import com.example.tfg.common.utils.Utils
import com.example.tfg.games.Games
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.common.CustomButton
import com.example.tfg.ui.components.common.CustomFilledButton
import com.example.tfg.ui.components.common.CustomIconButton
import com.example.tfg.ui.components.common.CustomPopup
import com.example.tfg.ui.components.common.CustomTextField
import com.example.tfg.ui.components.common.InTransitionDuration
import com.example.tfg.ui.components.common.LabeledIconButton
import com.example.tfg.ui.components.common.OutTransitionDuration
import com.example.tfg.ui.components.common.animateBlur

@Composable
private fun ChooseGameButton(
    modifier: Modifier = Modifier,
    game: Games,
    expandedStates: MutableTransitionState<Boolean>,
    changeChosenGame: (Games) -> Unit
) {
    CustomButton(
        onClick = {
            expandedStates.targetState = true
            changeChosenGame(game)
        },
        paddingValues = PaddingValues(12.dp, 12.dp, 0.dp, 12.dp),
        shape = RoundedCornerShape(8.dp),
        borderStroke = BorderStroke(0.5.dp, color = colorResource(id = R.color.board_grid2)),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.hakyuu_dark),
            contentDescription = "",
            modifier = Modifier.weight(3f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .weight(4f)
                .fillMaxHeight()
        ) {
            Text(text = "${game.title}", fontSize = 25.sp, color = colorResource(id = R.color.primary_color))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val mod = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                val shape = RoundedCornerShape(20.dp)
                val fontSize = 11.sp
                val iconPadding = 14.dp

                val rulesLabel = stringResource(id = R.string.rules)
                LabeledIconButton(
                    onClick = { Log.d("button", "REGLAS") },
                    imageVector = ImageVector.vectorResource(id = R.drawable.question_mark_24px),
                    iconColor = colorResource(id = R.color.primary_color),
                    label = rulesLabel,
                    labelColor = colorResource(id = R.color.primary_color),
                    fontSize = fontSize,
                    shape = shape,
                    iconPadding = iconPadding,
                    modifier = mod
                )

                val inProgressLabel = stringResource(id = R.string.in_progress2)
                LabeledIconButton(
                    onClick = { Log.d("button", "EN CURSO") },
                    imageVector = ImageVector.vectorResource(id = R.drawable.hourglass),
                    iconColor = colorResource(id = R.color.primary_color),
                    label = inProgressLabel,
                    labelColor = colorResource(id = R.color.primary_color),
                    fontSize = fontSize,
                    shape = shape,
                    iconPadding = iconPadding,
                    modifier = mod
                )

                val statsLabel = stringResource(id = R.string.stats)
                LabeledIconButton(
                    onClick = { Log.d("button", "STATS") },
                    imageVector = ImageVector.vectorResource(id = R.drawable.graphs),
                    iconColor = colorResource(id = R.color.primary_color),
                    label = statsLabel,
                    labelColor = colorResource(id = R.color.primary_color),
                    fontSize = fontSize,
                    shape = shape,
                    iconPadding = iconPadding,
                    modifier = mod
                )
            }
        }
    }
}

@Composable
fun TextFields(
    modifier: Modifier = Modifier,
    textColor: Color,
    chosenGame: Games,
    viewModel: MainViewModel
) {
    val textFieldModifier = modifier
        .border(
            1.dp,
            color = colorResource(id = R.color.board_grid2),
            shape = RoundedCornerShape(10.dp)
        )
    val context = LocalContext.current
    val difficultyRange = Difficulty.entries.map { it.toString(context) }
    val difficulty = remember { mutableStateOf(Difficulty.EASY.toString(context)) }
    val initialValue = "6"
    val numColumns = remember { mutableStateOf(initialValue) }
    val numRows = remember { mutableStateOf(initialValue) }
    val maxValue = 13
    val minValue = 3
    val range = (minValue..maxValue).map { it.toString() }
    val seed = remember { mutableStateOf("") }

    val difficultyLabel = stringResource(id = R.string.difficulty)
    CustomTextField(
        state = difficulty,
        range = difficultyRange,
        color = textColor,
        label = { Text(text = difficultyLabel, color = textColor) },
        modifier = textFieldModifier
    )

    val numColumnsLabel = stringResource(id = R.string.num_columns)
    CustomTextField(
        state = numColumns,
        range = range,
        color = textColor,
        label = { Text(text = numColumnsLabel, color = textColor) },
        modifier = textFieldModifier
    )

    val numRowsLabel = stringResource(id = R.string.num_rows)
    CustomTextField(
        state = numRows,
        range = range,
        color = textColor,
        label = { Text(text = numRowsLabel, color = textColor) },
        modifier = textFieldModifier
    )

    val seedLabel = stringResource(id = R.string.seed)
    CustomTextField(
        state = seed,
        color = textColor,
        label = { Text(text = seedLabel, color = textColor) },
        modifier = textFieldModifier
    )

    Row(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
        val customBoardText = stringResource(id = R.string.custom_board)
        CustomFilledButton(
            onClick = { /*TODO: Custom board*/ },
            color = colorResource(id = R.color.board_grid),
            borderColor = colorResource(id = R.color.board_grid2),
            textColor = textColor,
            mainText = customBoardText,
            modifier = modifier.weight(1.5f)
        )

        val createText = stringResource(id = R.string.create)
        CustomFilledButton(
            onClick = {
                val rows = numRows.value.toInt()
                val cols = numColumns.value.toInt()
                val diff = Difficulty.get(difficulty.value)
                val gameId = viewModel.createGame(chosenGame, rows, cols, diff)

                Utils.startActiveGameActivity(context, gameId)
            },
            color = colorResource(id = R.color.board_grid),
            borderColor = colorResource(id = R.color.board_grid2),
            textColor = textColor,
            mainText = createText,
            modifier = modifier.weight(1f)
        )
    }
}

@Composable
fun ChosenGame(
    expandedStates: MutableTransitionState<Boolean>,
    chosenGame: Games,
    viewModel: MainViewModel
) {
    val color = colorResource(id = R.color.pearl_white)
    CustomPopup(expandedStates = expandedStates ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(25.dp)
        ) {
            val modifier = Modifier.padding(top = 15.dp)
            Text(text = "${chosenGame.title}", fontSize = 25.sp, color = color)
            TextFields(
                textColor = color,
                modifier = modifier,
                chosenGame = chosenGame,
                viewModel = viewModel
            )
        }
    }
}


@Composable
fun GamesScreen(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    val chosenGame = remember { mutableStateOf(Games.HAKYUU) }
    val expandedStates = remember { MutableTransitionState(false) }
    val animatedBlur by animateBlur(expandedStates)

    Column(
        modifier = modifier
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
            .blur(animatedBlur)
    ) {
        ChooseGameButton(game = Games.HAKYUU, expandedStates = expandedStates, changeChosenGame = { newGame: Games ->
            chosenGame.value = newGame
        })
        Spacer(modifier = Modifier.height(10.dp))

    }

    ChosenGame(
        expandedStates = expandedStates,
        chosenGame = chosenGame.value,
        viewModel = viewModel
    )
}
