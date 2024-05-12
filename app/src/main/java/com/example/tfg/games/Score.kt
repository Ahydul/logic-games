package com.example.tfg.games

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class Score(
    protected var score: Int = 0
) : Parcelable {
    fun get(): Int {
        return this.score
    }

    fun reset() {
        score = 0
    }

    fun add(num: Int) {
        score += num
    }

    fun add(s: Score) {
        score += s.get()
    }
}