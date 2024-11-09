package com.example.tfg.games.kendoku

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

enum class KendokuStrategy {
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

    //TODO: Complete score
    fun addNakedPairs(numPairs: Int) {
        score += numPairs
        addToStrategies(KendokuStrategy.NAKED_PAIRS.name, numPairs)
    }

    fun addNakedTriples(numTriples: Int) {
        score += numTriples
        addToStrategies(KendokuStrategy.NAKED_TRIPLES.name, numTriples)
    }

    fun addHiddenSPT(numSPT: IntArray) {
        numSPT.forEach { score += it }
        addToStrategies(KendokuStrategy.HIDDEN_SINGLE.name, numSPT[0])
        addToStrategies(KendokuStrategy.HIDDEN_PAIRS.name, numSPT[1])
        addToStrategies(KendokuStrategy.HIDDEN_TRIPLES.name, numSPT[2])
    }

    fun addCombinations(numValuesRemoved: Int) {
        score += numValuesRemoved
        addToStrategies(KendokuStrategy.MAIN_COMBINATION_REDUCE.name, numValuesRemoved)
    }

    fun addCageUnitOverlapType2(numCUO: Int) {
        score += numCUO
        addToStrategies(KendokuStrategy.CAGE_UNIT_OVERLAPS_2.name, numCUO)
    }

    fun addBiValueAttack(numChanges: Int) {
        score += numChanges
        addToStrategies(KendokuStrategy.BI_VALUE_ATTACK.name, numChanges)
    }

    fun addInniesOuties(numInniesOuties: Int) {
        score += numInniesOuties
        addToStrategies(KendokuStrategy.INNIES_OUTIES.name, numInniesOuties)
    }

    fun addXWings(numXWings: Int) {
        score += numXWings
        addToStrategies(KendokuStrategy.X_WING.name, numXWings)
    }

    fun addColoring(numColoring: Int) {
        score += numColoring
        addToStrategies(KendokuStrategy.SIMPLE_COLORING.name, numColoring)
    }

    fun addCageUnitOverlapType1(numCUO: Int) {
        score += numCUO
        addToStrategies(KendokuStrategy.CAGE_UNIT_OVERLAPS_1.name, numCUO)
    }

    override fun serialize(): JsonElement {
        return Gson().toJsonTree(this).asJsonObject
    }
}