package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import com.example.tfg.R
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CaptureBitmap
import com.example.tfg.ui.components.common.LabeledIconButton
import com.example.tfg.ui.components.common.animateScale

@Composable
fun MiddleSection(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    Log.d("recomposition", "MIDDLE recomposition")

    Box (
        contentAlignment = Alignment.Center,
        modifier = modifier
    ){
        val expandedStates = remember { viewModel.getTimerState() }

        if (!expandedStates.currentState){
            if (viewModel.snapshotsAllowed) {
                val snapshot = CaptureBitmap{ Board(viewModel = viewModel) }
                viewModel.setSnapshot2(snapshot)
            }
            else Board(viewModel = viewModel)
        }

        val transition = updateTransition(expandedStates, "DropDownMenu")
        val scaleBoard by animateScale(transition)
        val mod = Modifier.graphicsLayer {
            scaleX = scaleBoard
            scaleY = scaleBoard
        }
        ResumeGame(viewModel, modifier = mod)
    }
}

@Composable
fun ResumeGame(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.primary_background))
    ) {
        LabeledIconButton(
            modifier = Modifier.fillMaxSize(0.5f),
            onClick = { viewModel.resumeGame() },
            imageVector = ImageVector.vectorResource(R.drawable.resume),
            label = "Resume game"
        )
    }
}