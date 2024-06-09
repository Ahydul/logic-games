package com.example.tfg.ui.components.activegame

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CustomButton2
import com.example.tfg.ui.components.common.CustomPopup


@Composable
fun ConfigurationPopup(
    modifier: Modifier = Modifier,
    expandedStates: MutableTransitionState<Boolean>,
    viewModel: ActiveGameViewModel
) {
    CustomPopup(
        modifier = modifier.fillMaxSize(),
        expandedStates = expandedStates,
        backgroundColor = colorResource(id = R.color.background),
        startScale = 0f,
        onDismissRequest = {
            expandedStates.targetState = false
            viewModel.resumeGame()
        }
    ) {
        val backgroundColor2 = colorResource(id = R.color.primary)
        val textColor = colorResource(id = R.color.text)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .background(backgroundColor2)
            ) {
                Text(
                    text = stringResource(R.string.options),
                    textAlign = TextAlign.Center,
                    color = textColor,
                    modifier = modifier.padding(20.dp)
                )
            }

            solveBoardButton(
                modifier = modifier,
                viewModel = viewModel,
                textColor = textColor,
                expandedStates = expandedStates
            )

            Text(text = "More configuration options coming soon...", color = textColor)
            //Switch(checked = , onCheckedChange = )
        }

    }
}

@Composable
private fun solveBoardButton(
    modifier: Modifier = Modifier,
    viewModel: ActiveGameViewModel,
    textColor: Color,
    expandedStates: MutableTransitionState<Boolean>
) {
    var showDialog by remember { mutableStateOf(false) }
    CustomButton2(onClick = { showDialog = true }) {
        Text(
            text = stringResource(R.string.solve_the_board),
            color = textColor,
        )
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        expandedStates.targetState = false
                        viewModel.resumeGame()
                        viewModel.completeTheBoard()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(text = stringResource(R.string.solve_board)) },
            text = { Text(stringResource(R.string.solve_board_confirmation_text)) },
        )
    }

}
