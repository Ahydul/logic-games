package com.example.tfg.ui.components.mainactivity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.R
import com.example.tfg.common.enums.Difficulty2
import com.example.tfg.common.enums.Games2
import com.example.tfg.common.enums.Selection
import com.example.tfg.common.enums.Times
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.common.CustomButton
import com.example.tfg.ui.components.common.CustomButton2
import com.example.tfg.ui.components.common.Divider
import com.example.tfg.ui.components.common.DropdownMenu
import com.example.tfg.ui.components.common.DropdownMenuButton

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    chosenGame: Games2?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxHeight()
    ) {

        val difficulties = Difficulty2.entries
        val selectedDifficulty = remember { mutableStateOf<Selection>(difficulties.first()) }
        ButtonRow(values = difficulties, selected = selectedDifficulty)

        val times = Times.entries
        val selectedTime = remember { mutableStateOf<Selection>(times.first()) }
        ButtonRow(values = times, selected = selectedTime)

        val gameOptions = Games2.entries
        val selectedGame = remember { mutableStateOf<Selection>(chosenGame ?: gameOptions.first()) }
        val mod = Modifier
        Dropdown(
            selectedGame = selectedGame,
            gameOptions = gameOptions,
            modifier = mod
        )

        Stats(
            selectedTime = selectedTime.value,
            selectedDifficulty = selectedDifficulty.value,
            selectedGame = selectedGame.value,
            viewModel = viewModel,
            modifier = mod,
        )
    }
}

@Composable
private fun Stats(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    selectedGame: Selection,
    selectedDifficulty: Selection,
    selectedTime: Selection
) {
    val color = colorResource(id = R.color.primary)
    val scrollState = rememberScrollState()

    val type = (selectedGame as Games2).toGames()
    val difficulty = (selectedDifficulty as Difficulty2).toDifficulty()
    val startDate = (selectedTime as Times).toLocalDateTimes()

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth(0.9f)
            .verticalScroll(scrollState)
    ) {
        val mod = modifier.padding(vertical = 10.dp)

        val gamesStarted = viewModel.getNumberGamesStarted(type = type, difficulty = difficulty, startDate = startDate)
        val gamesEnded = viewModel.getNumberGamesEnded(type = type, difficulty = difficulty, startDate = startDate)
        val gamesWon = viewModel.getNumberGamesWon(type = type, difficulty = difficulty, startDate = startDate)
        val winRate = viewModel.getWinRate(games = gamesEnded, gamesWon = gamesWon)
        val endRate = viewModel.getWinRate(games = gamesStarted, gamesWon = gamesWon)
        Games(modifier = mod, buttonModifier = modifier, color = color, gamesStarted = gamesStarted, gamesEnded = gamesEnded, gamesWon = gamesWon, winRate = winRate, endRate = endRate)

        val absoluteTotalTime = viewModel.getAbsoluteTotalTime(type = type, difficulty = difficulty, startDate = startDate)
        val totalTimeOnGoing = viewModel.getTotalTime(type = type, difficulty = difficulty, startDate = startDate, playerWon = false, endDate = null)
        val totalTimeGamesWon = viewModel.getTotalTime(type = type, difficulty = difficulty, startDate = startDate, playerWon = true)
        val bestTime = viewModel.getBestTime(type = type, difficulty = difficulty, startDate = startDate)
        val meanTime = viewModel.getMeanTime(type = type, difficulty = difficulty, startDate = startDate)
        Times(modifier = mod, buttonModifier = modifier, color = color, totalTimeOnGoing = totalTimeOnGoing, absoluteTotalTime = absoluteTotalTime, totalTimeGamesWon = totalTimeGamesWon, bestTime = bestTime, meanTime = meanTime)

        val errorMean = viewModel.getErrorMeanCount(type = type, difficulty = difficulty, startDate = startDate)
        val errorCount = viewModel.getErrorCount(type = type, difficulty = difficulty, startDate = startDate)
        Errors(modifier = mod, buttonModifier = modifier, color = color, errorCount = errorCount, errorMean = errorMean)

        val actualWinningStreak = viewModel.getActualWinningStreak(type = type)
        val bestWinningStreak = viewModel.getHighestWinningStreak(type = type, startDate = startDate)
        Streak(modifier = mod, buttonModifier = modifier, color = color, actualWinningStreak = actualWinningStreak, bestWinningStreak = bestWinningStreak)
    }
}

@Composable
private fun Streak(
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    actualWinningStreak: Int?,
    bestWinningStreak: Int?,
    color: Color
) {
    Column(modifier = modifier) {
        Text(text = "Winning streak", color = color)
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.actual_winning_streak),
            text2 = "${actualWinningStreak ?: "-"}",
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.best_winning_streak),
            text2 = "${bestWinningStreak ?: "-"}",
            onClick = {  }
        )
    }
}

@Composable
private fun Errors(
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    errorCount: Int?,
    errorMean: Double?,
    color: Color
) {
    Column(modifier = modifier) {
        Text(text = "Errors", color = color)
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.total_error_count),
            text2 = formatNumber(errorCount),
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.mean_error_count),
            text2 = formatNumber(errorMean),
            onClick = {  }
        )
    }
}

@Composable
private fun Times(
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    absoluteTotalTime: Int?,
    totalTimeOnGoing: Int?,
    totalTimeGamesWon: Int?,
    bestTime: Int?,
    meanTime: Double?,
    color: Color
) {
    Column(modifier = modifier) {
        Text(text = "Times", color = color)
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Total time (all games)",
            text2 = formatNumber(absoluteTotalTime),
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Total time (on going games)",
            text2 = formatNumber(totalTimeOnGoing),
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.total_time_games_won),
            text2 = formatNumber(totalTimeGamesWon),
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.best_time),
            text2 = formatNumber(bestTime),
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.mean_time),
            text2 = formatNumber(meanTime),
            onClick = {  }
        )
    }
}

@Composable
private fun Games(
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    gamesStarted: Int,
    gamesEnded: Int,
    gamesWon: Int,
    winRate: Double,
    endRate: Double,
    color: Color
) {
    Column(modifier = modifier) {
        Text(text = "Games", color = color)
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.games_started),
            text2 = "$gamesStarted",
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.games_ended),
            text2 = "$gamesEnded",
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.games_won),
            text2 = "$gamesWon",
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.win_rate),
            text2 = formatNumber(winRate) + "%",
            onClick = {  }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = stringResource(R.string.end_rate),
            text2 = formatNumber(endRate) + "%",
            onClick = {  }
        )

        String.format("%.0f", winRate)
    }
}

private fun formatNumber(number: Int?): String {
    return number?.toString() ?: "-"
}

private fun formatNumber(number: Double?): String {
    if (number == null) return "-"
    return if (number % 1 == 0.0) String.format("%.0f", number) else String.format("%.2f", number)
}


@Composable
private fun CustomButtonStats(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text1: String,
    text2: String,
) {
    val color = colorResource(id = R.color.primary)
    CustomButton2(
        onClick = onClick,
        horizontalArrangement = Arrangement.SpaceBetween,
        enabled = false, //TODO: Stats graphs
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = text1, color = color)
        Text(text = text2, color = color)
    }
}

@Composable
private fun Dropdown(
    modifier: Modifier = Modifier,
    selectedGame: MutableState<Selection>,
    gameOptions: List<Selection>
) {
    val color = colorResource(id = R.color.primary)
    val context = LocalContext.current
    DropdownMenu(
        modifier = modifier,
        selected = selectedGame.value.toString(context),
        numDropdownMenuButtons = gameOptions.size - 1
    ){
        gameOptions.filter { it != selectedGame.value }.forEach {
            DropdownMenuButton(
                onClick = { selectedGame.value = it },
                color = color,
                text = it.toString(context)
            )
        }
    }
}

@Composable
private fun ButtonRow(
    modifier: Modifier = Modifier,
    selected: MutableState<Selection>,
    values: Collection<Selection>
) {
    val color = colorResource(id = R.color.primary)
    val selectedColor = colorResource(id = R.color.cell_value)
    val context = LocalContext.current
    Divider()

    val scrollState = rememberScrollState()
    Row(modifier = modifier
        .horizontalScroll(scrollState)
        .width(IntrinsicSize.Min)
    ) {
        values.forEach {
            val isSelected = selected.value == it
            CustomButton(
                onClick = { selected.value = it },
                modifier = modifier
                    .width(IntrinsicSize.Max)
                    .weight(1f)
            ) {
                Text(
                    text = it.toString(context),
                    fontSize = 15.sp,
                    color = if (isSelected) selectedColor else color,
                    modifier = modifier.padding(horizontal = 14.dp)
                )
            }
        }
    }

    Divider(modifier = modifier.padding(bottom = 6.dp))
}
