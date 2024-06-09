package com.example.tfg.state

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfg.common.GameFactory
import com.example.tfg.data.LimitedGameDao
import com.example.tfg.data.StatsDao


@Suppress("UNCHECKED_CAST")
class CustomMainViewModelFactory(
    private val gameDao: LimitedGameDao,
    private val statsDao: StatsDao,
    private val gameFactory: GameFactory,
    private val configurationPrefs: SharedPreferences
) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(gameDao, statsDao, gameFactory, configurationPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}