package com.example.tfg.ui.components.mainactivity

import androidx.annotation.StringRes
import com.example.tfg.R

enum class MainActivity (@StringRes val title: Int) {
    Home(title = R.string.home_screen),
    Games(title = R.string.games_screen),
    Stats(title = R.string.stats_screen),
}