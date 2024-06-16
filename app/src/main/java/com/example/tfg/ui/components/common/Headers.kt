package com.example.tfg.ui.components.common

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.tfg.R
import com.example.tfg.state.MainViewModel
import com.example.tfg.ui.theme.Theme
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer


@Composable
fun MainHeader(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val expandedStates = remember { MutableTransitionState(false) }
    val theme by viewModel.themeUserSetting.collectAsState(initial = if (isSystemInDarkTheme()) Theme.DARK_MODE else Theme.LIGHT_MODE)

    Row(modifier = modifier) {
        Spacer(modifier = Modifier.weight(1f))
        CustomIconButton(
            onClick = { viewModel.setTheme() },
            painter =  painterResource(id = when (theme){
                Theme.DARK_MODE -> R.drawable.sun
                Theme.LIGHT_MODE -> R.drawable.moon
            }),
            iconColor = MaterialTheme.colorScheme.onBackground,
            contentDescription = "Set theme",
        )
        CustomIconButton(
            onClick = { expandedStates.targetState = true },
            painter =  painterResource(id = R.drawable.gear),
            iconColor = MaterialTheme.colorScheme.onBackground,
            contentDescription = "Click me for menu",
        )
    }
    CustomPopup(expandedStates = expandedStates) {
        LibrariesContainer(
            Modifier.fillMaxSize()
        )
    }
}