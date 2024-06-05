package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.tfg.R
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.LabeledIconButton

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
        if (viewModel.timerPaused()) ResumeGame(viewModel)
        else Board(viewModel=viewModel, modifier = Modifier)
    }
}

@Composable
fun ResumeGame(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    LabeledIconButton(
        modifier = modifier.fillMaxSize(0.5f),
        onClick = { viewModel.resumeGame() },
        imageVector = ImageVector.vectorResource(R.drawable.resume),
        label = "Resume game"
    )
}