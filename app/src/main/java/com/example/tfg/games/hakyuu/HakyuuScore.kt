package com.example.tfg.games.hakyuu

import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.Score
import com.google.gson.Gson
import com.google.gson.JsonElement

class HakyuuScore(score: Int = 0, bruteForce: Int = 0) : Score(Games.HAKYUU, score, bruteForce) {

    override fun add(s: Score?) {
        super.add(s)
        bruteForce += (s as HakyuuScore).bruteForce
    }

    override fun isTooHighForDifficulty(difficulty: Difficulty): Boolean {
        return when(difficulty){
            Difficulty.BEGINNER -> this.get() > MAX_BEGINNER
            Difficulty.EASY -> this.get() > MAX_EASY
            Difficulty.MEDIUM -> this.get() > MAX_MEDIUM
            Difficulty.HARD -> this.get() > MAX_HARD || this.bruteForce > 0
            Difficulty.EXPERT -> this.get() > MAX_EXPERT || this.bruteForce > MAX_EXPERT_BRUTE_FORCES
            Difficulty.MASTER -> this.get() > MAX_MASTER || this.bruteForce > MAX_MASTER_BRUTE_FORCES
        }
    }

    override fun getMaxBruteForceValue(difficulty: Difficulty): Int {
        return when(difficulty) {
            Difficulty.EXPERT -> MAX_EXPERT_BRUTE_FORCES
            Difficulty.MASTER -> MAX_MASTER_BRUTE_FORCES
            else -> 0
        }
    }

    override fun getDifficulty(): Difficulty {
        return when (this.get()) {
            in (MIN_BEGINNER..MAX_BEGINNER) -> Difficulty.BEGINNER
            in (MIN_EASY..MAX_EASY) -> Difficulty.EASY
            in (MIN_MEDIUM..MAX_MEDIUM) -> Difficulty.MEDIUM
            in (MIN_HARD..MAX_HARD) -> Difficulty.HARD
            in (MIN_EXPERT..MAX_EXPERT) -> Difficulty.EXPERT
            in (MIN_MASTER..MAX_MASTER) -> Difficulty.MASTER
            else -> throw Error("Invalid score $this used to get the difficulty")
        }
    }

    override fun serialize(): JsonElement {
        return Gson().toJsonTree(this).asJsonObject
    }

    override fun isTooLowForDifficulty(difficulty: Difficulty): Boolean {
        return when(difficulty){
            Difficulty.BEGINNER -> this.get() < MIN_BEGINNER
            Difficulty.EASY -> this.get() < MIN_EASY
            Difficulty.MEDIUM -> this.get() < MIN_MEDIUM
            Difficulty.HARD -> this.get() < MIN_HARD
            Difficulty.EXPERT -> this.get() < MIN_EXPERT
            Difficulty.MASTER -> this.get() < MIN_MASTER
        }
    }

    fun addScoreRule3() {
        score += 4
    }

    fun addScoreRule2() {
        score += 4
    }

    fun addScoreHiddenSingle(numFound: Int) {
        score += numFound*20
    }

    fun addScoreHiddenPairs(numFound: Int) {
        score += numFound*25
    }

    fun addScoreHiddenTriples(numFound: Int) {
        score += numFound*30
    }

    fun addScoreObviousPairs(numFound: Int) {
        score += numFound*5
    }

    fun addScoreObviousTriples(numFound: Int) {
        score += numFound*10
    }


    companion object {
        const val MIN_BEGINNER = 1
        const val MAX_BEGINNER = 100

        const val MIN_EASY = 100
        const val MAX_EASY = 300

        const val MIN_MEDIUM = 300
        const val MAX_MEDIUM = 600

        const val MIN_HARD = 600
        const val MAX_HARD = 1400

        const val MIN_EXPERT = 1100
        const val MAX_EXPERT = 3000
        const val MAX_EXPERT_BRUTE_FORCES = 1

        const val MIN_MASTER = 1900
        const val MAX_MASTER = 5000
        const val MAX_MASTER_BRUTE_FORCES = 3

        fun getMaxBruteForceValue(difficulty: Difficulty): Int {
            return when(difficulty) {
                Difficulty.EXPERT -> MAX_EXPERT_BRUTE_FORCES
                Difficulty.MASTER -> MAX_MASTER_BRUTE_FORCES
                else -> 0
            }
        }
    }
}