package com.example.tfg.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.R
import com.example.tfg.ui.components.mainactivity.MainActivity

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val bgColor = colorResource(id = R.color.board_grid)
    Divider()
    Row(modifier = modifier
        .background(bgColor)
    ) {
        val mod = Modifier
            .weight(1f)
            .fillMaxSize()
        val fontSize = 12.sp
        val iconPadding = 15.dp

        val homeLabel = stringResource(id = R.string.home)
        LabeledIconButton(
            onClick = { navController.navigate(MainActivity.Home.name) },
            imageVector = ImageVector.vectorResource(id = R.drawable.house),
            iconColor = colorResource(id = R.color.primary_color),
            labelColor = colorResource(id = R.color.primary_color),
            label = homeLabel,
            fontSize = fontSize,
            iconPadding = iconPadding,
            modifier = mod
        )

        val gamesLabel = stringResource(id = R.string.games)
        LabeledIconButton(
            onClick = { navController.navigate("${MainActivity.Games.name}/false") },
            imageVector = ImageVector.vectorResource(id = R.drawable.controller_game),
            iconColor = colorResource(id = R.color.primary_color),
            labelColor = colorResource(id = R.color.primary_color),
            label = gamesLabel,
            fontSize = fontSize,
            iconPadding = iconPadding,
            modifier = mod
        )

        val statsLabel = stringResource(id = R.string.stats)
        LabeledIconButton(
            onClick = { navController.navigate("${MainActivity.Stats.name}/ALL_GAMES") },
            imageVector = ImageVector.vectorResource(id = R.drawable.graphs),
            iconColor = colorResource(id = R.color.primary_color),
            labelColor = colorResource(id = R.color.primary_color),
            label = statsLabel,
            fontSize = fontSize,
            iconPadding = iconPadding,
            modifier = mod
        )
    }
}
