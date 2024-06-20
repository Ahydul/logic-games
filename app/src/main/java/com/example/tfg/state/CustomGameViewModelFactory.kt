package com.example.tfg.state

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfg.common.GameInstance
import com.example.tfg.data.GameDao
import java.io.File

@Suppress("UNCHECKED_CAST")
class CustomGameViewModelFactory(
    private val gameInstance: GameInstance,
    private val gameDao: GameDao,
    private val dataStore: DataStore<Preferences>? = null,
    private val filesDirectory: File? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveGameViewModel::class.java)) {
            return ActiveGameViewModel(gameInstance, gameDao, dataStore, filesDirectory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}