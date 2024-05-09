package com.example.tfg.ui.components.mainactivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.tfg.R
import com.example.tfg.ui.components.common.MainFilledButton

@Composable
fun ContinueButtons(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainFilledButton(
            onClick = {},
            color = colorResource(id = R.color.board_grid),
            borderColor = colorResource(id = R.color.board_grid2),
            textColor = colorResource(id = R.color.primary_color),
            mainText = "Continuar",
            secondaryText = "Hakyuu - 11:00 - fácil"
        )
        MainFilledButton(
            onClick = {},
            color = colorResource(id = R.color.board_grid),
            borderColor = colorResource(id = R.color.board_grid2),
            textColor = colorResource(id = R.color.primary_color),
            mainText = "Otras partidas en curso"
        )
    }
}