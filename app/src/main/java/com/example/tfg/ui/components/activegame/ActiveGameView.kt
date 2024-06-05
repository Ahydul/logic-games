package com.example.tfg.ui.components.activegame

import android.graphics.Bitmap
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CustomButton
import com.example.tfg.ui.components.common.CustomFilledButton
import com.example.tfg.ui.components.common.CustomPopup
import com.example.tfg.ui.components.common.animateBlur


@Composable
fun ActiveGameScreen(viewModel: ActiveGameViewModel, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    val firstBorderColor = Color.Black
    val secondBorderColor = Color.Black

    val expandedStates = remember { MutableTransitionState(true) }
    val animatedBlur by animateBlur(expandedStates)

    Column(modifier = modifier.blur(animatedBlur)) {
        TopSection(
            viewModel = viewModel,
            modifier = modifier
                .weight(1f)
        )
        MiddleSection(
            viewModel = viewModel,
            modifier = modifier
                .border(width = 1.dp, color = firstBorderColor)
                .aspectRatio(ratio = 1f)
                .padding(4.dp)
                .clip(shape)
                .border(width = 2.dp, color = secondBorderColor, shape = shape)
                .weight(4f)
        )
        BottomSection(
            viewModel = viewModel,
            onStateClick = {
                expandedStates.targetState = true
                viewModel.takeSnapshot()
                viewModel.pauseGame()
            },
            modifier = modifier
                .weight(3f)
        )
    }

    ChooseState(expandedStates = expandedStates, viewModel = viewModel)
}

@Composable
fun ChooseState(
    expandedStates: MutableTransitionState<Boolean>,
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    CustomPopup(
        expandedStates = expandedStates,
        backgroundColor = colorResource(id = R.color.board_grid2)
    ) {
        val default = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        default.eraseColor(Color.Gray.toArgb()) // Fill the entire bitmap with black color
        val textColor = colorResource(id = R.color.primary_color)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 45.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
        ) {
            val scrollState = rememberScrollState()
            Row (
                modifier = modifier.horizontalScroll(scrollState)
            ){
                val actualGameStateID = viewModel.getActualGameStatePosition()

                viewModel.getGameStateBitmapFromDB().forEach { (gameStatePosition, bitmap) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = modifier
                            .padding(vertical = 15.dp, horizontal = 5.dp)
                    ){
                        val isActualGameState = actualGameStateID == gameStatePosition
                        Text(
                            text = "${if (isActualGameState)"Actual board" else "Board"} ${gameStatePosition+1}",
                            color = textColor
                        )
                        CustomButton(
                            onClick = {
                                expandedStates.targetState = false
                                viewModel.setActualState(gameStatePosition)
                                viewModel.resumeGame()
                            },
                            enabled = !isActualGameState
                        ) {
                            //TODO: fix modifier of image to show at least 2
                            val imageModifier = modifier
                                .aspectRatio(1f)
                                .border(color = Color.Black, width = 1.dp)
                                .alpha(if (isActualGameState) 0.6f else 1f)
                            Image(
                                bitmap = (bitmap ?: default).asImageBitmap(),
                                contentDescription = "gameState $gameStatePosition",
                                modifier = imageModifier
                            )
                        }
                    }
                }
            }
            val cloneText = stringResource(id = R.string.clone)
            CustomFilledButton(
                onClick = {
                    expandedStates.targetState = false
                    viewModel.newGameState()
                    viewModel.resumeGame()
                },
                color = colorResource(id = R.color.board_grid),
                borderColor = colorResource(id = R.color.board_grid2),
                textColor = textColor,
                mainText = cloneText,
                modifier = Modifier.fillMaxWidth(0.5f)
            )
        }
    }
}

