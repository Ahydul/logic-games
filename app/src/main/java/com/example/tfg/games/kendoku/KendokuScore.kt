package com.example.tfg.games.kendoku

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

//TODO: Add values
enum class KendokuStrategy(val scoreValue: Int = 1) {
    NAKED_PAIRS,
    NAKED_TRIPLES,
    HIDDEN_SINGLE,
    HIDDEN_PAIRS,
    HIDDEN_TRIPLES,
    MAIN_COMBINATION_REDUCE,
    CAGE_UNIT_OVERLAPS_1,
    CAGE_UNIT_OVERLAPS_2,
    BI_VALUE_ATTACK,
    INNIES_OUTIES,
    X_WING,
    SIMPLE_COLORING;

    companion object {
        fun fromString(str: String?): KendokuStrategy? {
            return entries.find { it.name == str }
        }
    }
}

class KendokuScore(
    score: Int = 0,
    bruteForce: Int = 0,
    strategies: MutableMap<String, Int> = KendokuStrategy.entries.associate { it.name to 0 }.toMutableMap(),
) : Score(Games.KENDOKU, score, strategies, bruteForce, difficultyValues) {

    override fun toString(): String {
        return KendokuStrategy.entries.joinToString(", ") { strategies[it.name].toString() }
    }

    override fun add(s: Score?) {
        super.add(s)
        if (s == null) return
        val s = (s as KendokuScore)

        KendokuStrategy.entries.forEach { strategy ->
            addToStrategies(strategy.name, s.strategies[strategy.name]!!)
        }
    }

    private fun add(strategy: KendokuStrategy, num: Int = 1) {
        score += strategy.scoreValue * num
        addToStrategies(strategy.name)
    }

    fun addNakedPairs(numPairs: Int) {
        add(KendokuStrategy.NAKED_PAIRS, numPairs)
    }

    fun addNakedTriples(numTriples: Int) {
        add(KendokuStrategy.NAKED_TRIPLES, numTriples)
    }

    fun addHiddenSPT(numSPT: IntArray) {
        add(KendokuStrategy.HIDDEN_SINGLE, numSPT[0])
        add(KendokuStrategy.HIDDEN_PAIRS, numSPT[1])
        add(KendokuStrategy.HIDDEN_TRIPLES, numSPT[2])
    }

    fun addCombinations(numValuesRemoved: Int) {
        add(KendokuStrategy.MAIN_COMBINATION_REDUCE, numValuesRemoved)
    }

    fun addCageUnitOverlapType2(numCUO: Int) {
        add(KendokuStrategy.CAGE_UNIT_OVERLAPS_2, numCUO)
    }

    fun addBiValueAttack(numChanges: Int) {
        add(KendokuStrategy.BI_VALUE_ATTACK, numChanges)
    }

    fun addInniesOuties(numInniesOuties: Int) {
        add(KendokuStrategy.INNIES_OUTIES, numInniesOuties)
    }

    fun addXWings(numXWings: Int) {
        add(KendokuStrategy.X_WING, numXWings)
    }

    fun addColoring(numColoring: Int) {
        add(KendokuStrategy.SIMPLE_COLORING, numColoring)
    }

    fun addCageUnitOverlapType1(numCUO: Int) {
        add(KendokuStrategy.CAGE_UNIT_OVERLAPS_1, numCUO)
    }
}