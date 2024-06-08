package com.example.tfg.common.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Timer private constructor(
    var passedSeconds: MutableState<Int> = mutableIntStateOf(0),
    var paused: MutableState<Boolean> = mutableStateOf(false),
    private var timerJob: Job? = null,
    private var timerStopped: Boolean = false
) {

    fun startTimer(viewModelScope: CoroutineScope) {
        if (timerStopped) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                passedSeconds.value++
            }
        }
        paused.value = false
    }

    // When stopped this is class is no longer usable
    fun stopTimer() {
        timerJob?.cancel()
        timerStopped = true
    }

    fun pauseTimer() {
        timerJob?.cancel()
        paused.value = true
    }

    companion object {
        fun create(passedSeconds: Int, viewModelScope: CoroutineScope): Timer {
            val res = Timer(passedSeconds = mutableIntStateOf(passedSeconds))
            res.startTimer(viewModelScope)
            return res
        }

        fun formatTime(time: Int): String {
            val mins = time / 60
            val hours = mins / 60
            val displaySecs = time % 60
            val displayMins = mins % 60
            return if (hours == 0) String.format("%02d:%02d", displayMins, displaySecs)
                else String.format("%02d:%02d:%02d", hours % 24, displayMins, displaySecs)
        }
    }
}