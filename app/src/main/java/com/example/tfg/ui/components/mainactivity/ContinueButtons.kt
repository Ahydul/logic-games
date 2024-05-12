package com.example.tfg.ui.components.mainactivity

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.tfg.R
import com.example.tfg.ui.components.common.CustomFilledButton

@Composable
fun ContinueButtons(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val buttonModifier = Modifier.fillMaxWidth(0.8f)
        val textModifier = Modifier.fillMaxWidth(0.7f)

        val continueLabel = stringResource(id = R.string._continue)
        CustomFilledButton(
            onClick = { Log.d("navigation","CONTINUE") },
            color = colorResource(id = R.color.board_grid),
            borderColor = colorResource(id = R.color.board_grid2),
            textColor = colorResource(id = R.color.primary_color),
            mainText = continueLabel,
            secondaryText = "Hakyuu - 11:00 - fácil",
            fontSize = 22.sp,
            buttonModifier = buttonModifier,
            textModifier = textModifier
        )

        val otherGamesInProgressLabel = stringResource(id = R.string.other_games_in_progress)
        CustomFilledButton(
            onClick = { Log.d("navigation","OTHER GAMES") },
            color = colorResource(id = R.color.board_grid),
            borderColor = colorResource(id = R.color.board_grid2),
            textColor = colorResource(id = R.color.primary_color),
            mainText = otherGamesInProgressLabel,
            fontSize = 22.sp,
            buttonModifier = buttonModifier,
            textModifier = textModifier
            )
    }
}