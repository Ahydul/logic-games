package com.example.tfg.ui.components.activegame

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.animateBlur


@Composable
fun ActiveGameScreen(viewModel: ActiveGameViewModel, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    val firstBorderColor = Color.Black
    val secondBorderColor = colorResource(id = R.color.section_border)

    val expandedStates = remember { MutableTransitionState(false) }
    val animatedBlur by animateBlur(expandedStates)

    if (!viewModel.completedPopupWasShown && viewModel.gameIsCompleted() && !expandedStates.currentState && !expandedStates.targetState) {
        expandedStates.targetState = true
        viewModel.completedPopupWasShown = true
    }

    val configurationExpandedStates = remember { MutableTransitionState(false) }

    Column(modifier = modifier.blur(animatedBlur)) {
        TopSection(
            viewModel = viewModel,
            onConfigurationClick = { configurationExpandedStates.targetState = true },
            modifier = modifier
                .weight(1.1f)
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

    ConfigurationPopup(expandedStates = configurationExpandedStates, viewModel = viewModel)
    StateAndGameCompletedPopup(modifier = modifier, expandedStates = expandedStates, viewModel = viewModel)
}
