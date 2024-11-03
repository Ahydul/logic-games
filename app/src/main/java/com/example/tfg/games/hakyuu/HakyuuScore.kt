package com.example.tfg.games.hakyuu

import com.example.tfg.games.common.DifficultyValues
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.Score
import com.google.gson.Gson
import com.google.gson.JsonElement


private val difficultyValues = DifficultyValues(
    minBeginner = 1,
    maxBeginner = 100,

    minEasy = 100,
    maxEasy = 300,

    minMedium = 300,
    maxMedium = 600,

    minHard = 600,
    maxHard = 1400,

    minExpert = 1100,
    maxExpert = 3000,
    maxExpertBruteForces = 1,

    minMaster = 1900,
    maxMaster = 5000,
    maxMasterBruteForces = 3,
)

class HakyuuScore(score: Int = 0, bruteForce: Int = 0) : Score(Games.HAKYUU, score, mutableMapOf(), bruteForce, difficultyValues) {

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