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

class KendokuScore(
    private var nakedPairs: Int = 0,
    private var nakedTriples: Int = 0,
    private var hiddenSingles: Int = 0,
    private var hiddenPairs: Int = 0,
    private var hiddenTriples: Int = 0,
    private var mainCombinationReduces: Int = 0,
    private var cageUnitOverlapsType1: Int = 0,
    private var cageUnitOverlapsType2: Int = 0,
    private var biValueAttacks: Int = 0,
    private var inniesOuties: Int = 0,
    private var xWings: Int = 0,
    private var simpleColoring: Int = 0,
    score: Int = 0,
    bruteForce: Int = 0,
) : Score(score, bruteForce, difficultyValues) {

    override fun toString(): String {
        return "$nakedPairs, $nakedTriples, $hiddenSingles, $hiddenPairs, $hiddenTriples, $mainCombinationReduces, $cageUnitOverlapsType1, $cageUnitOverlapsType2, $biValueAttacks, $inniesOuties, $xWings, $simpleColoring"
    }

    override fun add(s: Score?) {
        super.add(s)
        if (s == null) return
        val s = (s as KendokuScore)
        nakedPairs += s.nakedPairs
        nakedTriples += s.nakedTriples
        hiddenSingles += s.hiddenSingles
        hiddenPairs += s.hiddenPairs
        hiddenTriples += s.hiddenTriples
        cageUnitOverlapsType1 += s.cageUnitOverlapsType1
        cageUnitOverlapsType2 += s.cageUnitOverlapsType2
        mainCombinationReduces += s.mainCombinationReduces
        biValueAttacks += s.biValueAttacks
        inniesOuties += s.inniesOuties
        simpleColoring += s.simpleColoring
    }

    //TODO: Complete score
    fun addNakedPairs(numPairs: Int) {
        score += numPairs
        nakedPairs += numPairs
    }

    fun addNakedTriples(numTriples: Int) {
        score += numTriples
        nakedTriples += numTriples
    }

    fun addHiddenSPT(numSPT: IntArray) {
        numSPT.forEach { score += it }
        hiddenSingles += numSPT[0]
        hiddenPairs += numSPT[1]
        hiddenTriples += numSPT[2]
    }

    fun addCombinations(numValuesRemoved: Int) {
        score += numValuesRemoved
        mainCombinationReduces += numValuesRemoved
    }

    fun addCageUnitOverlapType2(numCUO: Int) {
        score += numCUO
        cageUnitOverlapsType2 += numCUO
    }

    fun addBiValueAttack(numChanges: Int) {
        score += numChanges
        biValueAttacks += numChanges
    }

    fun addInniesOuties(numInniesOuties: Int) {
        score += numInniesOuties
        inniesOuties += numInniesOuties
    }

    fun addXWings(numXWings: Int) {
        score += numXWings
        xWings += numXWings
    }

    fun addColoring(numColoring: Int) {
        score += numColoring
        simpleColoring += numColoring
    }

    fun addCageUnitOverlapType1(numCUO: Int) {
        score += numCUO
        cageUnitOverlapsType1 += numCUO
    }

    override fun serialize(): JsonElement {
        return Gson().toJsonTree(this).asJsonObject
    }
}