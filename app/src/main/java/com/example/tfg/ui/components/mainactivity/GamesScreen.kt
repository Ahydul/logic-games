package com.example.tfg.ui.components.mainactivity

import android.content.Context
import android.content.Intent
import android.graphics.ColorSpace
import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
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
import com.example.tfg.ActiveGameView
import com.example.tfg.R
import com.example.tfg.common.Difficulty
import com.example.tfg.common.Game
import com.example.tfg.games.Games
import com.example.tfg.games.hakyuu.Hakyuu
import com.example.tfg.ui.components.common.ClippedRectangleShape
import com.example.tfg.ui.components.common.CustomButton
import com.example.tfg.ui.components.common.CustomFilledButton
import com.example.tfg.ui.components.common.CustomIconButton
import com.example.tfg.ui.components.common.CustomTextField
import com.example.tfg.ui.components.common.InTransitionDuration
import com.example.tfg.ui.components.common.LabeledIconButton
import com.example.tfg.ui.components.common.OutTransitionDuration
import com.example.tfg.ui.components.common.SlowOutFastInEasing
import kotlin.random.Random

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

                val inProgressLabel = stringResource(id = R.string.in_progress)
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
fun TextFields(modifier: Modifier = Modifier, textColor: Color, chosenGame: Games) {
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
            onClick = { /*TODO*/ },
            color = colorResource(id = R.color.board_grid),
            borderColor = colorResource(id = R.color.board_grid2),
            textColor = textColor,
            mainText = customBoardText,
            buttonModifier = modifier.weight(1.5f)
        )

        val createText = stringResource(id = R.string.create)
        CustomFilledButton(
            onClick = {
                val rows = numRows.value.toInt()
                val cols = numColumns.value.toInt()
                val diff = Difficulty.get(difficulty.value)
                val game = if (seed.value.isEmpty()) Game.create(chosenGame = chosenGame, difficulty = diff, numRows = rows, numColumns = cols)
                else Game.create(chosenGame = chosenGame, difficulty = diff, numRows = rows, numColumns = cols, seed = seed.value.hashCode().toLong())

                startActiveGameActivity(context, game)
            },
            color = colorResource(id = R.color.board_grid),
            borderColor = colorResource(id = R.color.board_grid2),
            textColor = textColor,
            mainText = createText,
            buttonModifier = modifier.weight(1f)
        )
    }
}

private fun startActiveGameActivity(context: Context, game: Game) {
    val intent = Intent(context, ActiveGameView::class.java)
    intent.putExtra("game", game)
    context.startActivity(intent)
}


@Composable
fun ChosenGame(expandedStates: MutableTransitionState<Boolean>, onDismissRequest: (() -> Unit), chosenGame: Games) {
    val transition = updateTransition(expandedStates, "DropDownMenu")
    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(
                    durationMillis = InTransitionDuration,
                    easing = LinearOutSlowInEasing
                )
            } else {
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration
                )
            }
        }
    ) { if (it) 1f else 0.8f }

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
        offset = IntOffset(0,-140)
    ) {
        val bgColor = colorResource(id = R.color.board_grid)
        val color = colorResource(id = R.color.pearl_white)

        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth(0.8f)
                .background(bgColor)
                .padding(3.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.padding(25.dp)
                ) {
                    val modifier = Modifier.padding(top = 15.dp)
                    Text(text = "${chosenGame.title}", fontSize = 25.sp, color = color)
                    TextFields(textColor = color, modifier = modifier, chosenGame = chosenGame)
                }
            }
            CustomIconButton(
                onClick = { expandedStates.targetState = false },
                imageVector = ImageVector.vectorResource(id = R.drawable.outline_close_24),
                contentDescription = "",
            )
        }
    }
}


@Composable
fun GamesScreen(modifier: Modifier = Modifier) {
    val chosenGame = remember { mutableStateOf(Games.HAKYUU) }
    val expandedStates = remember { MutableTransitionState(false) }

    val animatedBlur by animateDpAsState(
        targetValue = if (expandedStates.currentState || !expandedStates.currentState && expandedStates.targetState) 2.5.dp else 0.dp,
        animationSpec =
        if (expandedStates.currentState || !expandedStates.currentState && expandedStates.targetState)
            tween(
                durationMillis = InTransitionDuration,
                easing = LinearOutSlowInEasing
            )
        else
            tween(
                durationMillis = OutTransitionDuration,
                easing = LinearOutSlowInEasing
            ), label = "AnimateBlur"
    )

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

    val onDismissRequest = { expandedStates.targetState = false }

    if (expandedStates.targetState || expandedStates.currentState) {
        ChosenGame(expandedStates = expandedStates, onDismissRequest = onDismissRequest, chosenGame = chosenGame.value)
    }
}
