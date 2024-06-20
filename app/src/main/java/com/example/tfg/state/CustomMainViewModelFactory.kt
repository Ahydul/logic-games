package com.example.tfg.state

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfg.common.GameFactory
import com.example.tfg.data.LimitedGameDao
import com.example.tfg.data.StatsDao
import java.io.File


@Suppress("UNCHECKED_CAST")
class CustomMainViewModelFactory(
    private val gameDao: LimitedGameDao,
    private val statsDao: StatsDao,
    private val gameFactory: GameFactory,
    private val dataStore: DataStore<Preferences>,
    private val filesDir: File
) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(gameDao, statsDao, gameFactory, dataStore, filesDir) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}