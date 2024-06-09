package com.example.tfg.ui.components.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.tfg.R
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.theme.Theme


@Composable
fun MainHeader(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Spacer(modifier = Modifier.weight(1f))
        CustomIconButton(
            onClick = { viewModel.setTheme() },
            painter =  painterResource(id = when (viewModel.getTheme()){
                Theme.DARK_MODE -> R.drawable.sun
                Theme.LIGHT_MODE -> R.drawable.moon
            }),
            iconColor = MaterialTheme.colorScheme.onBackground,
            contentDescription = "Set theme",
        )
        CustomIconButton(
            onClick = { /*TODO: Configuration menu*/ },
            painter =  painterResource(id = R.drawable.gear),
            iconColor = MaterialTheme.colorScheme.onBackground,
            contentDescription = "Click me for menu",
        )
    }
}