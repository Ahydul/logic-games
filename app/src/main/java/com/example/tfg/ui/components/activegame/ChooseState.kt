package com.example.tfg.ui.components.activegame

import android.graphics.Bitmap
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CustomButton
import com.example.tfg.ui.components.common.CustomButton2
import com.example.tfg.ui.components.common.CustomText
import com.example.tfg.ui.components.common.defaultBitmap

@Composable
fun ChooseState(
    expandedStates: MutableTransitionState<Boolean>,
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    val textColor = colorResource(id = R.color.primary_color)
    val actualGameStateID = viewModel.getActualGameStatePosition()
    val selectedGameState = remember { mutableIntStateOf(actualGameStateID) }
    val states = viewModel.getGameStatesBitmapFromDB()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 45.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
    ) {
        val scrollState = rememberScrollState()
        Row (
            modifier = modifier.horizontalScroll(scrollState)
        ){
            states.forEach { (gameStatePosition, bitmap) ->
                StateBoard(
                    gameStatePosition = gameStatePosition,
                    bitmap = bitmap,
                    selectedGameState = selectedGameState,
                    textColor = textColor,
                    modifier = modifier
                )
            }
        }
        Row(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
            val buttonModifier = modifier
                .weight(1f)
                .padding(2.5.dp)
            val selectedIsActual = selectedGameState.intValue == actualGameStateID
            ChooseStateButton(
                onClick = {
                    if (selectedIsActual) viewModel.setActualState(0)
                    viewModel.deleteGameState(selectedGameState.intValue)
                    states.remove(selectedGameState.intValue)
                },
                color = colorResource(id = R.color.cell_value_error),
                text = stringResource(id = R.string.delete),
                modifier = buttonModifier,
                enabled = selectedGameState.intValue != 0
            )

            ChooseStateButton(
                onClick = {
                    expandedStates.targetState = false
                    viewModel.newGameState()
                    viewModel.resumeGame()
                },
                text = stringResource(id = R.string.clone),
                modifier = buttonModifier
            )
            ChooseStateButton(
                onClick = {
                    if (!selectedIsActual) {
                        expandedStates.targetState = false
                        viewModel.setActualState(selectedGameState.intValue)
                        viewModel.resumeGame()
                    }
                },
                text = stringResource(id = R.string.select),
                modifier = buttonModifier
            )
        }
    }
}

@Composable
fun ChooseStateButton(
    onClick: () -> Unit,
    text: String,
    color: Color = colorResource(id = R.color.board_grid),
    enabled: Boolean = true,
    modifier: Modifier
) {
    CustomButton2(
        onClick = onClick,
        color = color,
        enabled = enabled,
        modifier = modifier
    ){
        CustomText(mainText = text)
    }
}
@Composable
fun StateBoard(
    gameStatePosition: Int,
    bitmap: Bitmap?,
    textColor: Color,
    selectedGameState: MutableIntState,
    modifier: Modifier = Modifier
) {
    val default = defaultBitmap()
    val imageModifier = modifier
        .size(120.dp)
        .border(color = Color.Black, width = 1.dp)
        .background(Color.Black)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 15.dp, horizontal = 5.dp)
    ){
        val isMainGameState = gameStatePosition == 0
        val isSelected = selectedGameState.intValue == gameStatePosition
        Text(
            text = "${if (isMainGameState) stringResource(id = R.string.main_board) 
                else stringResource(id = R.string.board)} ${gameStatePosition+1}",
            color = textColor
        )
        CustomButton(
            onClick = { selectedGameState.intValue = gameStatePosition },
        ) {
            Image(
                bitmap = (bitmap ?: default).asImageBitmap(),
                contentDescription = "gameState $gameStatePosition",
                modifier = if (!isSelected) imageModifier
                else imageModifier
                    .alpha(0.7f)
                    .border(color = Color.Black, width = 2.dp)
            )
        }
    }
}