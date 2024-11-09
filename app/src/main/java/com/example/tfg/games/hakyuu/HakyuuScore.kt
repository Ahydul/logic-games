package com.example.tfg.games.hakyuu

import com.example.tfg.games.common.DifficultyValues
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.Score


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

enum class HakyuuStrategy(val scoreValue: Int) {
    RULE_2(4),
    RULE_3(4),
    NAKED_PAIRS(5),
    NAKED_TRIPLES(10),
    HIDDEN_SINGLE(20),
    HIDDEN_PAIRS(25),
    HIDDEN_TRIPLES(30);

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
            addToStrategies(strategy.name, s.strategies[strategy.name]!!)
        }
    }

    private fun add(strategy: HakyuuStrategy, num: Int = 1) {
        score += strategy.scoreValue * num
        addToStrategies(strategy.name)
    }

    fun addScoreRule3() {
        add(HakyuuStrategy.RULE_3)
    }

    fun addScoreRule2() {
        add(HakyuuStrategy.RULE_2)
    }

    fun addHiddenSPT(numSPT: IntArray) {
        add(HakyuuStrategy.HIDDEN_SINGLE, numSPT[0])
        add(HakyuuStrategy.HIDDEN_PAIRS, numSPT[1])
        add(HakyuuStrategy.HIDDEN_TRIPLES, numSPT[2])
    }

    fun addNakedPairs(numFound: Int) {
        add(HakyuuStrategy.NAKED_PAIRS, numFound)
    }

    fun addNakedTriples(numFound: Int) {
        add(HakyuuStrategy.NAKED_TRIPLES, numFound)
    }
}