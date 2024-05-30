package com.example.tfg.state

import androidx.lifecycle.ViewModel
import com.example.tfg.common.Difficulty
import com.example.tfg.common.GameFactory
import com.example.tfg.data.GameDao
import com.example.tfg.games.Games
import kotlinx.coroutines.runBlocking

class MainViewModel(private val gameDao: GameDao) : ViewModel() {
    fun createGame(chosenGame: Games, numRows: Int, numColumns: Int, difficulty: Difficulty): Long {
        return runBlocking {
            GameFactory(gameDao).createGame(
                chosenGame = chosenGame,
                numRows = numRows,
                numColumns = numColumns,
                difficulty = difficulty
            )
        }
    }
}