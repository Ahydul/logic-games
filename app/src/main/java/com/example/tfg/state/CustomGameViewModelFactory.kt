package com.example.tfg.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfg.common.entities.Game

class CustomGameViewModelFactory(private val game: Game) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveGameViewModel::class.java)) {
            return ActiveGameViewModel(game) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}