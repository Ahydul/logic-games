package com.example.tfg.games.common

import com.google.gson.JsonElement

abstract class Score(
    private val game: Games,
    var score: Int = 0,
    var bruteForce: Int = 0
) {

    fun get(): Int {
        return score
    }

    fun getBruteForceValue(): Int {
        return bruteForce
    }

    open fun add(s: Score?) {
        score += s?.score ?: 0
    }

    fun addScoreNewValue() {
        score += 1
    }

    fun addScoreBruteForce() {
        bruteForce += 1
        score += 1000
    }

    open fun reset() {
        score = 0
        bruteForce = 0
    }

    abstract fun isTooLowForDifficulty(difficulty: Difficulty): Boolean
    abstract fun isTooHighForDifficulty(difficulty: Difficulty): Boolean
    abstract fun getMaxBruteForceValue(difficulty: Difficulty): Int
    abstract fun getDifficulty(): Difficulty
    abstract fun serialize(): JsonElement
}