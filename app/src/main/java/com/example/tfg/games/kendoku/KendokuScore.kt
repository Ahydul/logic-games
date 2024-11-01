package com.example.tfg.games.kendoku

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

class KendokuScore(score: Int = 0, bruteForce: Int = 0) : Score(score, bruteForce, difficultyValues) {

    //TODO: Complete score
    fun addNakedPairs(numPairs: Int) {
        score += numPairs
    }

    fun addNakedTriples(numTriples: Int) {
        score += numTriples
    }

    fun addHiddenSPT(numSPT: IntArray) {
        numSPT.forEach { score += it }
    }

    fun addCombinations(numValuesRemoved: Int) {
        score += numValuesRemoved
    }

    fun addCageUnitOverlapType2(numCUO: Int) {
        score += numCUO
    }

    fun addBiValueAttack(numChanges: Int) {
        score += numChanges
    }

    fun addInniesOuties(numInniesOuties: Int) {
        score += numInniesOuties
    }

    fun addXWings(numXWings: Int) {
        score += numXWings
    }

    fun addColoring(numColoring: Int) {
        score += numColoring
    }

    fun addCageUnitOverlapType1(numCUO: Int) {
        score += numCUO
    }

    override fun serialize(): JsonElement {
        return Gson().toJsonTree(this).asJsonObject
    }
}