package com.example.tfg.ui.components.mainactivity

import android.graphics.Bitmap
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import com.example.tfg.games.common.Games.*
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.common.CopyableText
import com.example.tfg.ui.components.common.CustomButton
import com.example.tfg.ui.components.common.CustomButton2
import com.example.tfg.ui.components.common.CustomClickableText
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
    COMPLETED,
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
    val chosenGame = remember { mutableStateOf(HAKYUU) }
    val chosenGameAction = remember { mutableStateOf(Action.CREATE) }
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
        val mod = Modifier

        Games.entries.forEach { game ->
            ChooseGameButton(
                game = game,
                goStatsScreen = { goStatsScreen(game) },
                onClickButton = { action: Action ->
                    chosenGame.value = game
                    chosenGameAction.value = action
                    expandedStates.targetState = true
                },
                viewModel = viewModel,
                modifier = mod
            )
        }

        Spacer(modifier = mod.height(20.dp))

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
    chosenGame: MutableState<Games>,
    chosenGameAction: MutableState<Action>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onPrimary
    CustomPopup(
        expandedStates = expandedStates,
        onDismissRequest = {
            viewModel.cancelCreateGame()
            expandedStates.targetState = false
        },
        offset = IntOffset(0,-100),
        modifier = modifier.fillMaxWidth(0.9f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            if (chosenGameAction.value != Action.IN_PROGRESS_NO_GAME)
                Text(text = "${chosenGame.value.title}", fontSize = 25.sp, color = color, modifier = modifier.padding(top = 16.dp))
            when(chosenGameAction.value){
                Action.CREATE -> {
                    TextFields(
                        textColor = color,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        modifier = modifier.padding(vertical = 8.dp),
                        chosenGame = chosenGame.value,
                        viewModel = viewModel
                    )
                }
                //TODO: Someday implement LazyColumn somehow
                Action.IN_PROGRESS -> BoardsInfo(modifier = modifier, chosenGame = chosenGame.value, viewModel = viewModel)
                Action.IN_PROGRESS_NO_GAME -> BoardsInfo(modifier = modifier, viewModel = viewModel)
                Action.RULES -> Rules(modifier = modifier, chosenGame = chosenGame.value)
                Action.COMPLETED -> BoardsInfo(modifier = modifier, isCompleted = true, chosenGame = chosenGame.value, viewModel = viewModel)
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

        val context = LocalContext.current
        val annotatedString = chosenGame.getRules(context = context)
        CustomClickableText(annotatedString = annotatedString, context = context)
    }
}

@Composable
private fun BoardsInfo(
    modifier: Modifier = Modifier,
    chosenGame: Games? = null,
    isCompleted: Boolean = false,
    viewModel: MainViewModel
) {
    val scrollState = rememberScrollState()

    val items = if (chosenGame == null) viewModel.getOnGoingGames().collectAsState(initial = emptyList())
    else if (isCompleted) viewModel.getCompletedGames(chosenGame).collectAsState(initial = emptyList())
    else viewModel.getOnGoingGamesByType(chosenGame).collectAsState(initial = emptyList())

    LazyColumn(
        modifier
            .fillMaxHeight(0.7f)
            .padding(15.dp)
            .horizontalScroll(scrollState)
    ) {
        items(items.value) {game ->
            val bitmap = if (isCompleted) viewModel.getFinalSnapshotFile(game = chosenGame!!, gameId = game.gameId)
                else viewModel.getMainSnapshotFileByGameId(game.gameId)
            BoardInfo(game = game, isCompleted = isCompleted, bitmap = bitmap, modifier = modifier)
        }
    }
}

@Composable
private fun BoardInfo(
    game: GameLowerInfo,
    isCompleted: Boolean,
    bitmap: Bitmap?,
    modifier: Modifier,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val bitmap = (bitmap ?: defaultBitmap()).asImageBitmap()
    val context = LocalContext.current
    val onClick = if (isCompleted) {
        { }
    } else {
        { Utils.startActiveGameActivity(context, game.gameId) }
    }

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier.padding(vertical = 15.dp)
    ) {
        Column {
            Text(color = textColor ,text = "${game.startDate.format(dateFormatter)}")
            CustomButton(
                onClick = onClick,
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "game",
                    modifier = modifier.size(120.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(start = 18.dp)) {
            Text(color = textColor,
                text = "${stringResource(id = R.string.type)}: " +
                        "${game.gameType}")
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(color = textColor,
                    text = "${stringResource(id = R.string.seed)}: ")
                CopyableText(text = "${game.seed}", description = "Copy board seed: ${game.seed}", context = context)
            }
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
    val maxValue = chosenGame.maxSize
    val minValue = chosenGame.minSize
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

    when {
        chosenGame.isKendokuType() -> {
            val sizeLabel = stringResource(id = R.string.size)
            CustomTextField(
                state = numColumns,
                range = range,
                color = textColor,
                backgroundColor = backgroundColor,
                label = { Text(text = sizeLabel, color = textColor) },
                modifier = modifier
            )
        }
        else -> {
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
        }
    }

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
                viewModel.createGame(chosenGame, rows, cols, diff, seed.value, context)
                // For debug
                //viewModel.createJankoGame(chosenGame, seed.value.toIntOrNull(), context)
            },
            enabled = !viewModel.isLoading(),
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
    viewModel: MainViewModel,
    game: Games,
    onClickButton: (Action) -> Unit,
    goStatsScreen: () -> Unit,
) {
    val theme by viewModel.themeUserSetting.collectAsState(initial = if (isSystemInDarkTheme()) Theme.DARK_MODE else Theme.LIGHT_MODE)
    val imageID = if (theme.equals(Theme.DARK_MODE)) when (game) {
            HAKYUU -> R.drawable.hakyuu_dark
            KENDOKU -> R.drawable.kendoku_dark
            FACTORS -> R.drawable.factors_dark
            SUMDOKU -> R.drawable.sumdoku_dark
        }
        else when (game) {
            HAKYUU -> R.drawable.hakyuu_light
            KENDOKU -> R.drawable.kendoku_light
            FACTORS -> R.drawable.factors_light
            SUMDOKU -> R.drawable.sumdoku_light
        }

    CustomButton(
        onClick = { onClickButton(Action.CREATE) },
        paddingValues = PaddingValues(12.dp, 12.dp, 0.dp, 12.dp),
        shape = RoundedCornerShape(8.dp),
        borderStroke = BorderStroke(0.5.dp, color = MaterialTheme.colorScheme.outline),
        modifier = modifier.padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Image(
            painter = painterResource(id = imageID),
            contentDescription = "",
            modifier = modifier
                .weight(3f)
                .padding(vertical = 10.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.weight(4f)
        ) {
            Text(text = "${game.title}", fontSize = 25.sp, color = MaterialTheme.colorScheme.onPrimary)
            val rowModifier = modifier
                .fillMaxWidth(0.8f)
                .height(IntrinsicSize.Min)
            val buttonModifier = modifier
                .weight(3f)
                .height(40.dp)
            val spacerModifier = modifier.weight(1f)
            val iconTextHeightProportion = 0.6f

            Row(modifier = rowModifier.padding(bottom = 10.dp)) {
                LabeledIconButton(
                    onClick = { onClickButton(Action.RULES) },
                    imageVector = ImageVector.vectorResource(id = R.drawable.question_mark),
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    label = stringResource(id = R.string.rules),
                    labelColor = MaterialTheme.colorScheme.onPrimary,
                    iconTextHeightProportion = iconTextHeightProportion,
                    modifier = buttonModifier
                )

                Spacer(modifier = spacerModifier)

                LabeledIconButton(
                    onClick = { onClickButton(Action.IN_PROGRESS) },
                    imageVector = ImageVector.vectorResource(id = R.drawable.hourglass),
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    label = stringResource(id = R.string.in_progress2),
                    labelColor = MaterialTheme.colorScheme.onPrimary,
                    iconTextHeightProportion = iconTextHeightProportion,
                    modifier = buttonModifier
                )
            }

            Row(modifier = rowModifier) {
                LabeledIconButton(
                    onClick = { onClickButton(Action.COMPLETED) },
                    imageVector = ImageVector.vectorResource(id = R.drawable.checks_list),
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    label = stringResource(R.string.completed),
                    labelColor = MaterialTheme.colorScheme.onPrimary,
                    iconTextHeightProportion = iconTextHeightProportion,
                    modifier = buttonModifier
                )

                Spacer(modifier = spacerModifier)

                LabeledIconButton(
                    onClick = goStatsScreen,
                    imageVector = ImageVector.vectorResource(id = R.drawable.graphs),
                    iconColor = MaterialTheme.colorScheme.onPrimary,
                    label = stringResource(id = R.string.stats),
                    labelColor = MaterialTheme.colorScheme.onPrimary,
                    iconTextHeightProportion = iconTextHeightProportion,
                    modifier = buttonModifier
                )
            }
        }
    }
}