package com.example.tfg.ui.components.activegame

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.R
import com.example.tfg.common.utils.Utils
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CustomButton2
import com.example.tfg.ui.components.common.CustomPopup
import com.example.tfg.ui.components.common.CustomText
import com.example.tfg.ui.components.common.OutTransitionDuration

@Composable
fun StateAndGameCompletedPopup(
    modifier: Modifier = Modifier,
    expandedStates: MutableTransitionState<Boolean>,
    viewModel: ActiveGameViewModel
) {
    val gameCompleted = viewModel.gameIsCompleted()
    CustomPopup(
        modifier = modifier.fillMaxWidth(0.8f),
        expandedStates = expandedStates,
        offset = IntOffset(0,-140),
        onDismissRequest = {
            if (gameCompleted){
                expandedStates.targetState = false
                viewModel.setSnapshotNull() // To avoid snapshot
            } else {
                expandedStates.targetState = false
                Utils.runFunctionWithDelay((OutTransitionDuration).toLong()) {
                    viewModel.resumeGame()
                }

            }
        }
    ) {
        if (gameCompleted) GameCompleted(expandedStates = expandedStates, modifier = modifier)
        else ChooseState(expandedStates = expandedStates, viewModel = viewModel)
    }
}

@Composable
fun GameCompleted(
    expandedStates: MutableTransitionState<Boolean>,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(20.dp)
    ) {
        CustomText(
            mainText = stringResource(R.string.game_completed),
            mainFontSize = 22.sp
        )

        val context = LocalContext.current
        CustomButton2(onClick = { Utils.startHomeActivity(context)}) {
            CustomText(mainText = "Go home")
        }
    }
}