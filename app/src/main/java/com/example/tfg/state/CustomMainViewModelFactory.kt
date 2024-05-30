package com.example.tfg.state

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tfg.data.GameDao


@Suppress("UNCHECKED_CAST")
class CustomMainViewModelFactory(
    private val gameDao: GameDao,
    private val sharedPref: SharedPreferences,
    private val preview: Boolean = false
) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(gameDao, sharedPref, preview) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}