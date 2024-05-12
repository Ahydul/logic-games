package com.example.tfg.ui.components.activegame

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tfg.state.ActiveGameViewModel

@Composable
fun MiddleSection(
    viewModel: ActiveGameViewModel,
    modifier: Modifier = Modifier
) {
    Log.d("recomposition", "MIDDLE recomposition")

    Box (
        contentAlignment = Alignment.Center,
        modifier = modifier
    ){
        Board2(viewModel=viewModel, modifier = Modifier)
    }
}
