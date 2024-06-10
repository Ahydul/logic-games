package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.common.utils.Utils
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CustomIconButton
import com.example.tfg.ui.components.common.CustomText

@Composable
fun TopSection(
    viewModel: ActiveGameViewModel,
    onConfigurationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("TAG", "TOP currentRecomposeScope $currentRecomposeScope")

    Column(modifier = modifier.padding(bottom = 8.dp)) {
        val context = LocalContext.current

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
        ) {
            CustomIconButton(
                onClick = {
                    viewModel.setSnapshot(null) // To avoid snapshot
                    Utils.startHomeActivity(context)
                },
                painter = painterResource(id = R.drawable.back_arrow),
                contentDescription = stringResource(id = R.string.go_back)
            )
            val shouldBeVisible = viewModel.getNumberOfGameStates() > 1
            if (shouldBeVisible) {
                CustomIconButton(
                    onClick = { viewModel.checkErrors() },
                    painter = painterResource(id = R.drawable.check),
                    enabled = viewModel.buttonShouldBeEnabled(),
                    contentDescription = "Check errors",
                )
            }
            CustomIconButton(
                onClick = onConfigurationClick,
                painter = painterResource(id = R.drawable.gear),
                enabled = viewModel.buttonShouldBeEnabled(),
                contentDescription = "Game configuration",
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = modifier
        ) {
            val difficultyText = stringResource(id = R.string.difficulty)
            CustomText(
                mainText = viewModel.getDifficulty(context),
                secondaryText = difficultyText,
                reverse = true,
                textColor = MaterialTheme.colorScheme.onPrimary
            )

            val errorsText = stringResource(id = R.string.errors)
            CustomText(
                mainText = viewModel.getNumErrors().toString(),
                secondaryText = errorsText,
                reverse = true,
                textColor = MaterialTheme.colorScheme.onPrimary
            )

            val iconModifier = Modifier
                .size(30.dp)
                //.clip(CircleShape)
                //.border(shape = CircleShape, color = MaterialTheme.colorScheme.outlineVariant, width = 1.dp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                val cluesText = stringResource(id = R.string.clues)
                CustomText(
                    mainText = "${viewModel.getNumClues()}/${viewModel.getMaxNumCluesAllowed()}",
                    secondaryText = cluesText,
                    reverse = true,
                    textColor = MaterialTheme.colorScheme.onPrimary
                )

                CustomIconButton(
                    onClick = { viewModel.giveClue() },
                    painter = painterResource(R.drawable.question_mark_24px),
                    contentDescription = stringResource(id = R.string.pause_game),
                    enabled = viewModel.buttonShouldBeEnabled(),
                    modifier = iconModifier
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val timeText = stringResource(id = R.string.time)
                CustomText(
                    mainText = viewModel.getTime(),
                    secondaryText = timeText,
                    reverse = true,
                    textColor = MaterialTheme.colorScheme.onPrimary
                )

                CustomIconButton(
                    onClick = { viewModel.pauseGame() },
                    painter = painterResource(R.drawable.pause),
                    contentDescription = stringResource(id = R.string.pause_game),
                    enabled = viewModel.buttonShouldBeEnabled(),
                    modifier = iconModifier
                )
            }
        }
    }
}
