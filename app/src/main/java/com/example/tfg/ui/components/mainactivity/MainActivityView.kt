package com.example.tfg.ui.components.mainactivity

import android.util.Log
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
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.components.common.MainHeader
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
        MainHeader(modifier = modifier.weight(1.3f))

        NavHost(
            navController = navController,
            startDestination = MainActivity.Home.name,
            modifier = modifier.weight(17f)
        ) {
            composable(route = MainActivity.Home.name) {
            HomeScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    goGamesScreen = { navController.navigate(MainActivity.Games.name) },
                    goOnGoingGames = {
                        Log.d("as","asdasdasda")
                        navController.navigate("${MainActivity.Games.name}/true")
                    }
                )
            }
            composable(
                route = "${MainActivity.Games.name}/{onGoing}",
                arguments = listOf(navArgument("onGoing") { type = NavType.BoolType }
            )
            ) { backStackEntry ->
                GamesScreen(
                    modifier = modifier,
                    viewModel = viewModel,
                    onGoing = backStackEntry.arguments?.getBoolean("onGoing") ?: false
                )
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
