package com.example.tfg.games.kendoku

import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.Score
import com.google.gson.JsonElement

class KendokuScore(game: Games = Games.HAKYUU) : Score(game) {

    fun addScoreRule2() {
        score += 4
    }

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

    fun addCageUnitOverlap(numCUO: Int) {
        score += numCUO
    }

    fun addBiValueAttack(numChanges: Int) {
        score += numChanges
    }

    fun addInniesOuties(numInniesOuties: Int) {
        score += numInniesOuties
    }

    override fun isTooLowForDifficulty(difficulty: Difficulty): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTooHighForDifficulty(difficulty: Difficulty): Boolean {
        TODO("Not yet implemented")
    }

    override fun getMaxBruteForceValue(difficulty: Difficulty): Int {
        TODO("Not yet implemented")
    }

    override fun getDifficulty(): Difficulty {
        TODO("Not yet implemented")
    }

    override fun serialize(): JsonElement {
        TODO("Not yet implemented")
    }
}