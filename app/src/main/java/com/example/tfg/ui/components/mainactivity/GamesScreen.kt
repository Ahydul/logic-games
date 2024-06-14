package com.example.tfg.ui.components.mainactivity

import android.graphics.Bitmap
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.R
import com.example.tfg.games.common.Difficulty
import com.example.tfg.common.GameLowerInfo
import com.example.tfg.common.utils.Timer
import com.example.tfg.common.utils.Utils
import com.example.tfg.common.utils.dateFormatter
import com.example.tfg.games.common.Games
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.common.CustomButton
import com.example.tfg.ui.components.common.CustomButton2
import com.example.tfg.ui.components.common.CustomPopup
import com.example.tfg.ui.components.common.CustomText
import com.example.tfg.ui.components.common.CustomTextField
import com.example.tfg.ui.components.common.LabeledIconButton
import com.example.tfg.ui.components.common.MainHeader
import com.example.tfg.ui.components.common.animateBlur
import com.example.tfg.ui.components.common.defaultBitmap
import com.example.tfg.ui.theme.Theme

private enum class Action {
    RULES,
    CREATE,
    IN_PROGRESS,
    IN_PROGRESS_NO_GAME
}

@Composable
fun GamesScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onGoing: Boolean = false,
    goStatsScreen: (Games) -> Unit
) {
    var onGoing = remember { mutableStateOf(onGoing) }
    var chosenGame =  Games.HAKYUU
    var chosenGameAction = remember { mutableStateOf(Action.CREATE) }
    val expandedStates = remember { MutableTransitionState(false) }
    val animatedBlur by animateBlur(expandedStates)

    if (onGoing.value){
        chosenGameAction.value = Action.IN_PROGRESS_NO_GAME
        expandedStates.targetState = true
        onGoing.value = false
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .blur(animatedBlur)
            .padding(top = 8.dp, start = 6.dp, end = 6.dp)
    ) {
        MainHeader(viewModel = viewModel, modifier = modifier)
        val mod = Modifier.padding(vertical = 10.dp)
        val gameHakyuu = Games.HAKYUU
        val imageID = if (viewModel.getTheme().equals(Theme.DARK_MODE)) R.drawable.hakyuu_dark
        else R.drawable.hakyuu_light
        ChooseGameButton(
            game = gameHakyuu,
            imageID = imageID,
            goStatsScreen = goStatsScreen,
            onClickRules = {
                chosenGame = gameHakyuu
                chosenGameAction.value = Action.RULES
                expandedStates.targetState = true
            },
            onClickInProgress = {
                chosenGame = gameHakyuu
                chosenGameAction.value = Action.IN_PROGRESS
                expandedStates.targetState = true
            },
            onChooseGame = {
                chosenGame = gameHakyuu
                chosenGameAction.value = Action.CREATE
                expandedStates.targetState = true
            },
            modifier = mod
        )

        Spacer(modifier = mod.height(10.dp))

    }

    ChosenGame(
        expandedStates = expandedStates,
        chosenGame = chosenGame,
        chosenGameAction = chosenGameAction,
        viewModel = viewModel
    )
}

@Composable
private fun ChosenGame(
    expandedStates: MutableTransitionState<Boolean>,
    chosenGame: Games,
    chosenGameAction: MutableState<Action>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onPrimary
    CustomPopup(
        expandedStates = expandedStates,
        offset = IntOffset(0,-100),
        modifier = modifier.fillMaxWidth(0.9f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            if (chosenGameAction.value != Action.IN_PROGRESS_NO_GAME)
                Text(text = "${chosenGame.title}", fontSize = 25.sp, color = color, modifier = modifier.padding(top = 16.dp))
            when(chosenGameAction.value){
                Action.CREATE -> {
                    TextFields(
                        textColor = color,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        modifier = modifier.padding(vertical = 8.dp),
                        chosenGame = chosenGame,
                        viewModel = viewModel
                    )
                }
                //TODO: Someday implement LazyColumn somehow
                Action.IN_PROGRESS -> InProgress(modifier = modifier, chosenGame = chosenGame, viewModel = viewModel)
                Action.IN_PROGRESS_NO_GAME -> InProgress(modifier = modifier, viewModel = viewModel)
                Action.RULES -> Rules(modifier = modifier, chosenGame = chosenGame)
            }

        }
    }
}

@Composable
private fun Rules(
    modifier: Modifier = Modifier,
    chosenGame: Games,
) {
    Column(modifier.padding(15.dp)) {

        val annotatedString = chosenGame.getRules()
        val context = LocalContext.current
        ClickableText(text = annotatedString, style = TextStyle(color = MaterialTheme.colorScheme.onPrimary) ) { offset ->
            annotatedString.getStringAnnotations(tag = "web", start = offset, end = offset).firstOrNull()?.let {
                Utils.goToWebPage(it.item, context = context)
            }
        }
    }
}


@Composable
private fun InProgress(
    modifier: Modifier = Modifier,
    chosenGame: Games? = null,
    viewModel: MainViewModel
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxHeight(0.7f)
            .verticalScroll(scrollState)
    ) {
        if (chosenGame != null) {
            viewModel.getOnGoingGamesByType(chosenGame).forEach { game ->
                val bitmap = viewModel.getMainSnapshotFileByGameId(game.gameId)
                OnGoingBoard(game = game, bitmap = bitmap, modifier = modifier)
            }
        }
        else {
            viewModel.getOnGoingGames().forEach { game ->
                val bitmap = viewModel.getMainSnapshotFileByGameId(game.gameId)
                OnGoingBoard(game = game, bitmap = bitmap, modifier = modifier)
            }
        }
    }
}

@Composable
private fun OnGoingBoard(
    game: GameLowerInfo,
    bitmap: Bitmap?,
    modifier: Modifier,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val bitmap = (bitmap ?: defaultBitmap()).asImageBitmap()
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier.padding(vertical = 15.dp)
    ) {
        CustomButton(
            onClick = { Utils.startActiveGameActivity(context, game.gameId) },
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "game",
                modifier = modifier.size(120.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 18.dp)) {
            Text(color = textColor ,text = "${game.startDate.format(dateFormatter)}")
            Text(color = textColor,
                text = "${stringResource(id = R.string.type)}: " +
                        "${game.type}")
            Text(color = textColor,
                text = "${stringResource(id = R.string.difficulty)}: " +
                        "${game.difficulty.toString(context)}")
            Text(color = textColor,
                text = "${stringResource(id = R.string.timer)}: " +
                    "${Timer.formatTime(game.timer)}")
            Text(color = textColor,
                text = "${stringResource(id = R.string.errors)}: " +
                    "${game.numErrors}")
            Text(color = textColor,
                text = "${stringResource(id = R.string.clues)}: " +
                    "${game.numClues}")
        }
    }

}


@Composable
private fun TextFields(
    modifier: Modifier = Modifier,
    textColor: Color,
    backgroundColor: Color,
    chosenGame: Games,
    viewModel: MainViewModel
) {
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
        backgroundColor = backgroundColor,
        label = { Text(text = difficultyLabel, color = textColor) },
        modifier = modifier.padding(top = 10.dp)
    )

    val numColumnsLabel = stringResource(id = R.string.num_columns)
    CustomTextField(
        state = numColumns,
        range = range,
        color = textColor,
        backgroundColor = backgroundColor,
        label = { Text(text = numColumnsLabel, color = textColor) },
        modifier = modifier
    )

    val numRowsLabel = stringResource(id = R.string.num_rows)
    CustomTextField(
        state = numRows,
        range = range,
        color = textColor,
        backgroundColor = backgroundColor,
        label = { Text(text = numRowsLabel, color = textColor) },
        modifier = modifier
    )

    val seedLabel = stringResource(id = R.string.seed)
    CustomTextField(
        state = seed,
        color = textColor,
        backgroundColor = backgroundColor,
        label = { Text(text = seedLabel, color = textColor) },
        modifier = modifier
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 15.dp)
    ) {
/*
        val customBoardText = stringResource(id = R.string.custom_board)
        CustomFilledButton(
            onClick = { /*TODO: Custom board*/ },
            color = colorResource(id = R.color.board_grid),
            borderColor = colorResource(id = R.color.board_grid2),
            textColor = textColor,
            mainText = customBoardText,
            modifier = modifier.weight(1.5f),
            enabled = false
        )
 */
        val mod = modifier.weight(1f)
        Spacer(modifier = mod)

        LoadingIcon(viewModel = viewModel, modifier = mod)

        val createText = stringResource(id = R.string.create)
        CustomButton2(
            onClick = {
                val rows = numRows.value.toInt()
                val cols = numColumns.value.toInt()
                val diff = Difficulty.get(difficulty.value)
                viewModel.createGame(chosenGame, rows, cols, diff, context)
            },
            modifier = mod
        ){
            CustomText(mainText = createText, textColor = textColor, modifier = modifier)
        }
    }
}

@Composable
private fun LoadingIcon(viewModel: MainViewModel, modifier: Modifier) {
    if (viewModel.isLoading()) Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        CircularProgressIndicator()
    }
    else Spacer(modifier = modifier)
}

@Composable
private fun ChooseGameButton(
    modifier: Modifier = Modifier,
    imageID: Int,
    game: Games,
    onClickRules: () -> Unit,
    onClickInProgress: () -> Unit,
    goStatsScreen: (Games) -> Unit,
    onChooseGame: () -> Unit,
) {
    CustomButton(
        onClick = onChooseGame,
        paddingValues = PaddingValues(12.dp, 12.dp, 0.dp, 12.dp),
        shape = RoundedCornerShape(8.dp),
        borderStroke = BorderStroke(0.5.dp, color = MaterialTheme.colorScheme.outline),
        modifier = modifier.padding(horizontal = 10.dp)
    ) {
        Image(
            painter = painterResource(id = imageID),
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
            Text(text = "${game.title}", fontSize = 25.sp, color = MaterialTheme.colorScheme.onPrimary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val mod = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                val fontSize = 11.sp
                val iconPadding = 14.dp

                val rulesLabel = stringResource(id = R.string.rules)
                LabeledIconButton(
                    onClick = onClickRules,
                    imageVector = ImageVector.vectorResource(id = R.drawable.question_mark),
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    label = rulesLabel,
                    labelColor = MaterialTheme.colorScheme.onPrimary,
                    fontSize = fontSize,
                    iconPadding = iconPadding,
                    modifier = mod
                )

                val inProgressLabel = stringResource(id = R.string.in_progress2)
                LabeledIconButton(
                    onClick = onClickInProgress,
                    imageVector = ImageVector.vectorResource(id = R.drawable.hourglass),
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    label = inProgressLabel,
                    labelColor = MaterialTheme.colorScheme.onPrimary,
                    fontSize = fontSize,
                    iconPadding = iconPadding,
                    modifier = mod
                )

                val statsLabel = stringResource(id = R.string.stats)
                LabeledIconButton(
                    onClick = { goStatsScreen(game) },
                    imageVector = ImageVector.vectorResource(id = R.drawable.graphs),
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    label = statsLabel,
                    labelColor = MaterialTheme.colorScheme.onPrimary,
                    fontSize = fontSize,
                    iconPadding = iconPadding,
                    modifier = mod
                )
            }
        }
    }
}