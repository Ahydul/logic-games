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
import com.example.tfg.common.Difficulty
import com.example.tfg.common.Times
import com.example.tfg.games.Games
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.common.CustomButton
import com.example.tfg.ui.components.common.CustomButton2
import com.example.tfg.ui.components.common.Divider
import com.example.tfg.ui.components.common.DropdownMenu
import com.example.tfg.ui.components.common.DropdownMenuButton

@Composable
fun StatsScreen(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxHeight()
    ) {
        val context = LocalContext.current

        val difficulties = Difficulty.entries.map { it.toString(context) }
        val selectedDifficulty = remember { mutableStateOf(difficulties.first()) }
        ButtonRow(values = difficulties, selected = selectedDifficulty)

        val times = Times.entries.map { it.toString(context) }
        val selectedTime = remember { mutableStateOf(times.first()) }
        ButtonRow(values = times, selected = selectedTime)

        val gameOptions = Games.entries.map { it.toString() }.toMutableList()
        val allGamesString = stringResource(id = R.string.all_games)
        gameOptions.add(0, allGamesString)
        val selectedGame = remember { mutableStateOf(allGamesString) }

        val mod = Modifier
        Dropdown(
            selectedGame = selectedGame,
            gameOptions = gameOptions,
            modifier = mod
        )

        Pitos(
            selectedTime = selectedTime.value,
            selectedDifficulty = selectedDifficulty.value,
            selectedGame = selectedGame.value,
            viewModel = viewModel,
            modifier = mod,
        )
    }
}

@Composable
private fun Pitos(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    selectedGame: String,
    selectedDifficulty: String,
    selectedTime: String
) {
    val color = colorResource(id = R.color.primary_color)
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth(0.9f)
            .verticalScroll(scrollState)
    ) {
        val mod = modifier.padding(vertical = 10.dp)
        Games(modifier = mod, buttonModifier = modifier, color = color)
        Times(modifier = mod, buttonModifier = modifier, color = color)
        Errors(modifier = mod, buttonModifier = modifier, color = color)
        Streak(modifier = mod, buttonModifier = modifier, color = color)
    }
}

@Composable
fun Streak(
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    color: Color
) {
    Column(modifier = modifier) {
        Text(text = "Winning streak", color = color)
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Actual winning streak",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Best winning streak",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
    }
}

@Composable
fun Errors(
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    color: Color
) {
    Column(modifier = modifier) {
        Text(text = "Errors", color = color)
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Total error count",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Mean error count",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Median error count",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
    }
}

@Composable
fun Times(
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    color: Color
) {
    Column(modifier = modifier) {
        Text(text = "Times", color = color)
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Total time",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Best time",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Mean time",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Median time",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
    }
}

@Composable
fun Games(
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    color: Color
) {
    Column(modifier = modifier) {
        Text(text = "Games", color = color)
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Games started",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Games won",
            text2 = "0",
            onClick = { /*TODO*/ }
        )
        CustomButtonStats(
            modifier = buttonModifier,
            text1 = "Win rate",
            text2 = "0%",
            onClick = { /*TODO*/ }
        )
    }
}

@Composable
fun CustomButtonStats(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text1: String,
    text2: String,
) {
    val color = colorResource(id = R.color.primary_color)
    CustomButton2(
        onClick = onClick,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = text1, color = color)
        Text(text = text2, color = color)
    }
}

@Composable
private fun Dropdown(
    modifier: Modifier = Modifier,
    selectedGame: MutableState<String>,
    gameOptions: List<String>
) {
    val color = colorResource(id = R.color.primary_color)

    DropdownMenu(
        modifier = modifier,
        selected = selectedGame.value,
        numDropdownMenuButtons = gameOptions.size - 1
    ){
        gameOptions.filter { it != selectedGame.value }.forEach {
            DropdownMenuButton(
                onClick = { selectedGame.value = it },
                color = color,
                text = it
            )
        }
    }
}

@Composable
private fun ButtonRow(
    modifier: Modifier = Modifier,
    selected: MutableState<String>,
    values: Collection<String>
) {
    val color = colorResource(id = R.color.primary_color)
    val selectedColor = colorResource(id = R.color.cell_value)

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
                    text = it,
                    fontSize = 15.sp,
                    color = if (isSelected) selectedColor else color,
                    modifier = modifier.padding(horizontal = 14.dp)
                )
            }
        }
    }

    Divider(modifier = modifier.padding(bottom = 6.dp))
}
