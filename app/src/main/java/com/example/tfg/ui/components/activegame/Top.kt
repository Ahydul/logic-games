package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R
import com.example.tfg.common.utils.Utils
import com.example.tfg.state.ActiveGameViewModel
import com.example.tfg.ui.components.common.CustomIconButton
import com.example.tfg.ui.components.common.CustomText

@Composable
fun TopSection(viewModel: ActiveGameViewModel, modifier: Modifier = Modifier) {
    Log.d("TAG", "TOP currentRecomposeScope $currentRecomposeScope")

    Column(modifier = modifier.padding(bottom = 8.dp)) {
        val context = LocalContext.current

        Row {
            CustomIconButton(
                onClick = { Utils.startHomeActivity(context)},
                imageVector = ImageVector.vectorResource(id = R.drawable.back_arrow),
                contentDescription = stringResource(id = R.string.go_back)
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = modifier
        ) {
            val difficultyText = stringResource(id = R.string.difficulty)
            CustomText(mainText = difficultyText, secondaryText = viewModel.getDifficulty(context), textColor = colorResource(R.color.primary_color))

            val errorsText = stringResource(id = R.string.errors)
            CustomText(mainText = errorsText, secondaryText = "${viewModel.getNumErrors()}", textColor = colorResource(R.color.primary_color))

            val cluesText = stringResource(id = R.string.clues)
            CustomText(mainText = cluesText, secondaryText = "${viewModel.getNumClues()}", textColor = colorResource(R.color.primary_color))

            val timeText = stringResource(id = R.string.time)
            CustomText(mainText = timeText, secondaryText = viewModel.getTime(), textColor = colorResource(R.color.primary_color))

            CustomIconButton(
                onClick = { viewModel.pauseGame() },
                imageVector = ImageVector.vectorResource(R.drawable.pause),
                contentDescription = stringResource(id = R.string.pause_game),
                enabled = !viewModel.timerPaused()
            )
        }
    }


}
