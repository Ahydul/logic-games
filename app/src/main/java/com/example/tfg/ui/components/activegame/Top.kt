package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.ui.Modifier
import com.example.tfg.state.ActiveGameViewModel

@Composable
fun TopSection(viewModel: ActiveGameViewModel, modifier: Modifier = Modifier) {
    Log.d("TAG", "TOPcurrentRecomposeScope $currentRecomposeScope")
    /*
        Column(modifier = modifier) {
            Row {
                Text(
                    text = ""
                )
            }
            Row {
                Text(
                    text = "Dificultad"
                )
                Text(
                    text = "Errores"
                )
                Text(
                    text = "Pistas"
                )
                Text(
                    text = "Tiempo"
                )
            }
        }

     */
}
