package com.example.tfg.games.hakyuu

import com.example.tfg.games.Score

class HakyuuScore(score: Int = 0) : Score(score) {

    fun addScoreRule3() {
        score += 1
    }
    fun addScoreRule2() {
        score += 1
    }
    fun addScoreHiddenSingle(numFound: Int) {
        score += (20*numFound)
    }
    fun addScoreHiddenPairs(numFound: Int) {
        score += (21*numFound)
    }
    fun addScoreHiddenTriples(numFound: Int) {
        score += (22*numFound)
    }

    fun addScoreObviousSingle(numFound: Int) {
        score += (11*numFound)
    }

    fun addScoreObviousPairs(numFound: Int) {
        score += (12*numFound)
    }

    fun addScoreBruteForce() {
        score += 1000
    }
}