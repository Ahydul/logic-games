package com.example.tfg.games.common

import androidx.room.Ignore
import com.example.tfg.games.kendoku.KendokuScore
import com.google.gson.JsonElement

class DifficultyValues(
    val MIN_BEGINNER: Int,
    val MAX_BEGINNER: Int,

    val MIN_EASY: Int,
    val MAX_EASY: Int,

    val MIN_MEDIUM: Int,
    val MAX_MEDIUM: Int,

    val MIN_HARD: Int,
    val MAX_HARD: Int,

    val MIN_EXPERT: Int,
    val MAX_EXPERT: Int,
    val MAX_EXPERT_BRUTE_FORCES: Int,

    val MIN_MASTER: Int,
    val MAX_MASTER: Int,
    val MAX_MASTER_BRUTE_FORCES: Int
)


abstract class Score(
    var score: Int = 0,
    var bruteForce: Int = 0,
    @Ignore
    val difficultyValues: DifficultyValues
) {

    fun get(): Int {
        return score
    }

    fun getBruteForceValue(): Int {
        return bruteForce
    }

    open fun add(s: Score?) {
        score += s?.score ?: 0
        bruteForce += s?.bruteForce ?: 0
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

    fun isTooLowForDifficulty(difficulty: Difficulty): Boolean {
        return when(difficulty){
            Difficulty.BEGINNER -> this.get() < difficultyValues.MIN_BEGINNER
            Difficulty.EASY -> this.get() < difficultyValues.MIN_EASY
            Difficulty.MEDIUM -> this.get() < difficultyValues.MIN_MEDIUM
            Difficulty.HARD -> this.get() < difficultyValues.MIN_HARD
            Difficulty.EXPERT -> this.get() < difficultyValues.MIN_EXPERT
            Difficulty.MASTER -> this.get() < difficultyValues.MIN_MASTER
        }
    }

    fun isTooHighForDifficulty(difficulty: Difficulty): Boolean {
        return when(difficulty){
            Difficulty.BEGINNER -> this.get() > difficultyValues.MAX_BEGINNER
            Difficulty.EASY -> this.get() > difficultyValues.MAX_EASY
            Difficulty.MEDIUM -> this.get() > difficultyValues.MAX_MEDIUM
            Difficulty.HARD -> this.get() > difficultyValues.MAX_HARD || this.bruteForce > 0
            Difficulty.EXPERT -> this.get() > difficultyValues.MAX_EXPERT || this.bruteForce > difficultyValues.MAX_EXPERT_BRUTE_FORCES
            Difficulty.MASTER -> this.get() > difficultyValues.MAX_MASTER || this.bruteForce > difficultyValues.MAX_MASTER_BRUTE_FORCES
        }
    }
    fun getMaxBruteForceValue(difficulty: Difficulty): Int {
        return when(difficulty) {
            Difficulty.EXPERT -> difficultyValues.MAX_EXPERT_BRUTE_FORCES
            Difficulty.MASTER -> difficultyValues.MAX_MASTER_BRUTE_FORCES
            else -> 0
        }
    }

    fun getDifficulty(): Difficulty {
        return when (this.get()) {
            in (difficultyValues.MIN_BEGINNER .. difficultyValues.MAX_BEGINNER) -> Difficulty.BEGINNER
            in (difficultyValues.MIN_EASY..difficultyValues.MAX_EASY) -> Difficulty.EASY
            in (difficultyValues.MIN_MEDIUM..difficultyValues.MAX_MEDIUM) -> Difficulty.MEDIUM
            in (difficultyValues.MIN_HARD..difficultyValues.MAX_HARD) -> Difficulty.HARD
            in (difficultyValues.MIN_EXPERT..difficultyValues.MAX_EXPERT) -> Difficulty.EXPERT
            in (difficultyValues.MIN_MASTER..difficultyValues.MAX_MASTER) -> Difficulty.MASTER
            else -> throw Error("Invalid score $this used to get the difficulty")
        }

    }

    abstract fun serialize(): JsonElement

    companion object {
        fun create(gameType: Games): Score {
            return when(gameType){
                Games.HAKYUU -> KendokuScore()
            }
        }
    }
}