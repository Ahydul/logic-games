package com.example.tfg.games

abstract class Score(
    protected var score: Int = 0
) {
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