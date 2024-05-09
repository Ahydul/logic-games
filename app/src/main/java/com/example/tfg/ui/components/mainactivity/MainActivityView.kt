package com.example.tfg.ui.components.mainactivity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tfg.ui.components.common.MainHeader
import com.example.tfg.ui.components.common.NavigationBar

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    //viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    Column(modifier = modifier
        .fillMaxHeight()
        .padding(top = 5.dp)
    ) {
        MainHeader(modifier = modifier.weight(1.3f))

        NavHost(
            navController = navController,
            startDestination = MainActivity.Home.name,
            modifier = modifier.weight(17f)
        ) {
            composable(route = MainActivity.Home.name) {
                HomeScreen(modifier = modifier)
            }
            composable(route = MainActivity.Games.name) {
                GamesScreen(modifier = modifier)
            }
            composable(route = MainActivity.Stats.name) {
                StatsScreen(modifier = modifier)
            }
        }

        NavigationBar(
            modifier = modifier.weight(2f),
            navController = navController
        )
    }
}
