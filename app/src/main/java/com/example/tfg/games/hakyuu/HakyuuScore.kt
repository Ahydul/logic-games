package com.example.tfg.games.hakyuu

import com.example.tfg.games.common.DifficultyValues
import com.example.tfg.games.common.Score
import com.google.gson.Gson
import com.google.gson.JsonElement


private val difficultyValues = DifficultyValues(
    MIN_BEGINNER = 1,
    MAX_BEGINNER = 100,

    MIN_EASY = 100,
    MAX_EASY = 300,

    MIN_MEDIUM = 300,
    MAX_MEDIUM = 600,

    MIN_HARD = 600,
    MAX_HARD = 1400,

    MIN_EXPERT = 1100,
    MAX_EXPERT = 3000,
    MAX_EXPERT_BRUTE_FORCES = 1,

    MIN_MASTER = 1900,
    MAX_MASTER = 5000,
    MAX_MASTER_BRUTE_FORCES = 3,
)

class HakyuuScore(score: Int = 0, bruteForce: Int = 0) : Score(score, bruteForce, difficultyValues) {

    override fun add(s: Score?) {
        super.add(s)
        bruteForce += (s as HakyuuScore).bruteForce
    }

    override fun serialize(): JsonElement {
        return Gson().toJsonTree(this).asJsonObject
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
}