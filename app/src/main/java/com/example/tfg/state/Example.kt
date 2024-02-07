package com.example.tfg.state

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class Example: ViewModel()  {
    private val _color = MutableStateFlow(0xFFFFFFFF)
    val color = _color.asStateFlow()

    var composeColor by mutableStateOf(0xFFFFFFFF)
        private set

    fun generateColor(){
        val color = Random.nextLong(0xFFFFFFFF)
        Log.d("","${color}")
        _color.value = color
        composeColor = color
    }

}