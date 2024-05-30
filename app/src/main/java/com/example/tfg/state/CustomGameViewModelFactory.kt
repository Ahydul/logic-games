package com.example.tfg.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfg.data.GameDao

@Suppress("UNCHECKED_CAST")
class CustomGameViewModelFactory(private val gameId: Long, private val gameDao: GameDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveGameViewModel::class.java)) {
            return ActiveGameViewModel(gameId, gameDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}