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

enum class HakyuuStrategy {
    NAKED_PAIRS,
    NAKED_TRIPLES,
    HIDDEN_SINGLE,
    HIDDEN_PAIRS,
    HIDDEN_TRIPLES;

    companion object {
        fun fromString(str: String?): HakyuuStrategy? {
            return entries.find { it.name == str }
        }
    }
}

class HakyuuScore(
    score: Int = 0,
    bruteForce: Int = 0,
    strategies: MutableMap<String, Int> = HakyuuStrategy.entries.associate { it.name to 0 }.toMutableMap(),
) : Score(Games.HAKYUU, score, strategies, bruteForce, difficultyValues) {

    override fun toString(): String {
        return HakyuuStrategy.entries.joinToString(", ") { strategies[it.name].toString() }
    }

    override fun add(s: Score?) {
        super.add(s)
        if (s == null) return
        val s = (s as HakyuuScore)

        HakyuuStrategy.entries.forEach { strategy ->
            strategies[strategy.name] = strategies[strategy.name]!! + s.strategies[strategy.name]!!
        }
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

    fun addHiddenSPT(numSPT: IntArray) {
        score += numSPT[0]*20
        score += numSPT[1]*25
        score += numSPT[2]*30

        numSPT.forEach { score += it }
        strategies[HakyuuStrategy.HIDDEN_SINGLE.name] = strategies[HakyuuStrategy.HIDDEN_SINGLE.name]!! + numSPT[0]
        strategies[HakyuuStrategy.HIDDEN_PAIRS.name] = strategies[HakyuuStrategy.HIDDEN_PAIRS.name]!! + numSPT[1]
        strategies[HakyuuStrategy.HIDDEN_TRIPLES.name] = strategies[HakyuuStrategy.HIDDEN_TRIPLES.name]!! + numSPT[2]
    }

    fun addNakedPairs(numFound: Int) {
        score += numFound*5
        strategies[HakyuuStrategy.NAKED_PAIRS.name] = strategies[HakyuuStrategy.NAKED_PAIRS.name]!! + numFound
    }

    fun addNakedTriples(numFound: Int) {
        score += numFound*10
        strategies[HakyuuStrategy.NAKED_TRIPLES.name] = strategies[HakyuuStrategy.NAKED_TRIPLES.name]!! + numFound
    }
}