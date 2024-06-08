package com.example.tfg.ui.components.mainactivity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tfg.common.Games2
import com.example.tfg.games.Games
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.common.NavigationBar

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    Column(modifier = modifier
        .fillMaxHeight()
        .padding(top = 5.dp)
    ) {
        NavHost(
            navController = navController,
            startDestination = MainActivity.Home.name,
            modifier = modifier.weight(17f)
        ) {
            composable(route = MainActivity.Home.name) {
            HomeScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    goGamesScreen = { navController.navigate("${MainActivity.Games.name}/false") },
                    goOnGoingGames = { navController.navigate("${MainActivity.Games.name}/true") }
                )
            }
            composable(
                route = "${MainActivity.Games.name}/{onGoing}",
                arguments = listOf(navArgument("onGoing") { type = NavType.BoolType })
            ) { backStackEntry ->
                GamesScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    onGoing = backStackEntry.arguments?.getBoolean("onGoing") ?: false,
                    goStatsScreen = { game: Games -> navController.navigate("${MainActivity.Stats.name}/${game.toGames2().name}") }
                )
            }
            composable(
                route = "${MainActivity.Stats.name}/{chosenGame}",
                arguments = listOf(navArgument("chosenGame") { type = NavType.StringType })
            ) { backStackEntry ->
                StatsScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    chosenGame = Games2.fromString(backStackEntry.arguments?.getString("chosenGame"))
                )
            }
        }

        NavigationBar(
            modifier = modifier.weight(2f),
            navController = navController
        )
    }
}
