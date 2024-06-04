package com.example.tfg.games

import com.example.tfg.common.Difficulty
import com.google.gson.JsonElement

abstract class Score {
    abstract fun get(): Int
    abstract fun reset()
    abstract fun add(s: Score?)
    abstract fun isTooLowForDifficulty(difficulty: Difficulty): Boolean
    abstract fun isTooHighForDifficulty(difficulty: Difficulty): Boolean
    abstract fun getDifficulty(): Difficulty
    abstract fun serialize(): JsonElement
}