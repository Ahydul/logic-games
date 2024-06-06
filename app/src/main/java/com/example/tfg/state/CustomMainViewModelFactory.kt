package com.example.tfg.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfg.data.GameDao


@Suppress("UNCHECKED_CAST")
class CustomMainViewModelFactory(
    private val gameDao: GameDao,
    private val lastPlayedGame: Long,
    private val preview: Boolean = false
) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(gameDao, lastPlayedGame, preview) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}