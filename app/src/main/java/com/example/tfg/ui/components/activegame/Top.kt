package com.example.tfg.ui.components.activegame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
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

    Column(modifier = modifier) {
        val context = LocalContext.current

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier.padding(top = 8.dp)
        ) {
            CustomIconButton(
                onClick = {
                    //viewModel.setSnapshotNull() // To avoid snapshot
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
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
        ) {
            val difficultyText = stringResource(id = R.string.difficulty)
            CustomText(
                mainText = "${viewModel.getDifficulty(context)} (${viewModel.getScoreValue()})",
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

            val iconModifier = Modifier.size(30.dp)
            Clues(viewModel = viewModel, modifier = iconModifier)
            Timer(viewModel = viewModel, modifier = iconModifier)
        }
    }
}
@Composable
fun Clues(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    TextNextToButton(
        mainText = "${viewModel.getNumClues()}/${viewModel.getMaxNumCluesAllowed()}",
        secondaryText = stringResource(id = R.string.clues),
        icon = painterResource(R.drawable.question_mark),
        contentDescription = stringResource(id = R.string.give_clue),
        enabled = viewModel.buttonShouldBeEnabled(),
        buttonModifier = modifier,
        onClick = { viewModel.giveClue() }
    )
}

@Composable
fun Timer(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    TextNextToButton(
        mainText = viewModel.getTime(),
        secondaryText = stringResource(id = R.string.time),
        icon = painterResource(R.drawable.pause),
        contentDescription = stringResource(id = R.string.pause_game),
        enabled = viewModel.buttonShouldBeEnabled(),
        buttonModifier = modifier,
        onClick = { viewModel.pauseGame() }
    )
}

@Composable
private fun TextNextToButton(
    mainText: String,
    secondaryText: String,
    enabled: Boolean,
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    buttonModifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CustomText(
            mainText = mainText,
            secondaryText = secondaryText,
            reverse = true,
            textColor = MaterialTheme.colorScheme.onPrimary
        )

        CustomIconButton(
            onClick = onClick,
            painter = icon,
            contentDescription = contentDescription,
            enabled = enabled,
            modifier = buttonModifier
        )
    }
}