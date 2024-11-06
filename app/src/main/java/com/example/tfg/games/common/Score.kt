package com.example.tfg.games.common

import androidx.room.Ignore
import com.example.tfg.games.hakyuu.HakyuuScore
import com.example.tfg.games.kendoku.KendokuScore
import com.google.gson.JsonElement

class DifficultyValues(
    val minBeginner: Int,
    val maxBeginner: Int,

    val minEasy: Int,
    val maxEasy: Int,

    val minMedium: Int,
    val maxMedium: Int,

    val minHard: Int,
    val maxHard: Int,

    val minExpert: Int,
    val maxExpert: Int,
    val maxExpertBruteForces: Int,

    val minMaster: Int,
    val maxMaster: Int,
    val maxMasterBruteForces: Int
)


abstract class Score(
    val game: Games,
    var score: Int = 0,
    val strategies: MutableMap<String, Int>,
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

    fun isTooLowForDifficulty(difficulty: Difficulty): Boolean {
        return when(difficulty){
            Difficulty.BEGINNER -> this.get() < difficultyValues.minBeginner
            Difficulty.EASY -> this.get() < difficultyValues.minEasy
            Difficulty.MEDIUM -> this.get() < difficultyValues.minMedium
            Difficulty.HARD -> this.get() < difficultyValues.minHard
            Difficulty.EXPERT -> this.get() < difficultyValues.minExpert
            Difficulty.MASTER -> this.get() < difficultyValues.minMaster
        }
    }

    fun isTooHighForDifficulty(difficulty: Difficulty): Boolean {
        return when(difficulty){
            Difficulty.BEGINNER -> this.get() > difficultyValues.maxBeginner
            Difficulty.EASY -> this.get() > difficultyValues.maxEasy
            Difficulty.MEDIUM -> this.get() > difficultyValues.maxMedium
            Difficulty.HARD -> this.get() > difficultyValues.maxHard || this.bruteForce > 0
            Difficulty.EXPERT -> this.get() > difficultyValues.maxExpert || this.bruteForce > difficultyValues.maxExpertBruteForces
            Difficulty.MASTER -> this.get() > difficultyValues.maxMaster || this.bruteForce > difficultyValues.maxMasterBruteForces
        }
    }
    fun getMaxBruteForceValue(difficulty: Difficulty): Int {
        return when(difficulty) {
            Difficulty.EXPERT -> difficultyValues.maxExpertBruteForces
            Difficulty.MASTER -> difficultyValues.maxMasterBruteForces
            else -> 0
        }
    }

    fun getDifficulty(): Difficulty {
        return when (this.get()) {
            in (difficultyValues.minBeginner .. difficultyValues.maxBeginner) -> Difficulty.BEGINNER
            in (difficultyValues.minEasy..difficultyValues.maxEasy) -> Difficulty.EASY
            in (difficultyValues.minMedium..difficultyValues.maxMedium) -> Difficulty.MEDIUM
            in (difficultyValues.minHard..difficultyValues.maxHard) -> Difficulty.HARD
            in (difficultyValues.minExpert..difficultyValues.maxExpert) -> Difficulty.EXPERT
            in (difficultyValues.minMaster..difficultyValues.maxMaster) -> Difficulty.MASTER
            else -> throw Error("Invalid score $this used to get the difficulty")
        }

    }

    abstract fun serialize(): JsonElement

    companion object {
        fun create(gameType: Games): Score {
            return when(gameType){
                Games.HAKYUU -> HakyuuScore()
                Games.KENDOKU, Games.FACTORS -> KendokuScore()
            }
        }
    }
}