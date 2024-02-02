package com.example.tfg.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tfg.common.Board

class ActiveGameState : ViewModel() {
    private val _board = MutableLiveData(null)
}