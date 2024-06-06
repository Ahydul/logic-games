package com.example.tfg.ui.components.activegame

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.tfg.R

@Composable
fun GameCompleted(
    expandedStates: MutableTransitionState<Boolean>,
    modifier: Modifier = Modifier
) {
    //TODO: Make this more pretty
    Text(text = stringResource(R.string.game_completed))
}