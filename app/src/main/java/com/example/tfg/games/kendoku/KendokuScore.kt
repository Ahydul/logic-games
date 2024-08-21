package com.example.tfg.games.kendoku

import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.Score
import com.google.gson.JsonElement

class KendokuScore(game: Games = Games.HAKYUU) : Score(game) {

    fun addScoreRule2() {
        score += 4
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