package com.example.tfg.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.tfg.R

@Composable
fun NavigationBar(modifier: Modifier = Modifier) {
    val bgColor = colorResource(id = R.color.board_grid)
    Divider (
        color = colorResource(id = R.color.board_grid2),
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
    )
    Row(modifier = modifier
        .background(bgColor)
    ) {
        val mod = Modifier.weight(1f)

        LabeledIconButton(
            onClick = { /*TODO*/ },
            imageVector = ImageVector.vectorResource(id = R.drawable.house),
            iconColor = colorResource(id = R.color.primary_color),
            labelColor = colorResource(id = R.color.primary_color),
            label = "Inicio",
            modifier = mod
        )
        LabeledIconButton(
            onClick = { /*TODO*/ },
            imageVector = ImageVector.vectorResource(id = R.drawable.controller_game),
            iconColor = colorResource(id = R.color.primary_color),
            labelColor = colorResource(id = R.color.primary_color),
            label = "Juegos",
            modifier = mod
        )
        LabeledIconButton(
            onClick = { /*TODO*/ },
            imageVector = ImageVector.vectorResource(id = R.drawable.graphs),
            iconColor = colorResource(id = R.color.primary_color),
            labelColor = colorResource(id = R.color.primary_color),
            label = "Estadísticas",
            modifier = mod
        )

    }
}
