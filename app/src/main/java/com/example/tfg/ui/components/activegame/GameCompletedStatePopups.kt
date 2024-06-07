package com.example.tfg.ui.components.activegame

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.tfg.R
import com.example.tfg.common.utils.Utils
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CustomButton2
import com.example.tfg.ui.components.common.CustomPopup
import com.example.tfg.ui.components.common.CustomText

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
        backgroundColor = colorResource(id = R.color.board_grid2),
        offset = IntOffset(0,-140),
        onDismissRequest = {
            if (gameCompleted){
                expandedStates.targetState = false
                viewModel.setSnapshot(null) // To avoid snapshot
            } else {
                expandedStates.targetState = false
                viewModel.resumeGame()
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
    //TODO: Make this more pretty
    Column {
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