package com.example.tfg.games.hakyuu

import com.example.tfg.common.Difficulty
import com.example.tfg.games.Games
import com.example.tfg.games.Score
import com.google.gson.Gson
import com.google.gson.JsonElement

class HakyuuScore(
    game: Games = Games.HAKYUU,
    private var newValue: Int = 0,
    private var rule2: Int = 0,
    private var rule3: Int = 0,
    private var hiddenSingle: Int = 0,
    private var hiddenPair: Int = 0,
    private var hiddenTriple: Int = 0,
    private var obviousPair: Int = 0,
    private var obviousTriple: Int = 0,
    private var bruteForce: Int = 0
) : Score(game) {


    override fun get(): Int {
        return newValue + (rule2+rule3)*2 + obviousPair*5 + obviousTriple*10 +
                hiddenSingle*20 + hiddenPair*25 + hiddenTriple*30 + bruteForce*1000
    }

    override fun reset() {
        this.newValue = 0
        this.rule2 = 0
        this.rule3 = 0
        this.hiddenSingle = 0
        this.hiddenPair = 0
        this.hiddenTriple = 0
        this.obviousPair = 0
        this.obviousTriple = 0
        this.bruteForce = 0
    }

    override fun add(s: Score?) {
        if (s == null) return

        val other = s as HakyuuScore
        this.newValue += other.newValue
        this.rule2 += other.rule2
        this.rule3 += other.rule3
        this.hiddenSingle += other.hiddenSingle
        this.hiddenPair += other.hiddenPair
        this.hiddenTriple += other.hiddenTriple
        this.obviousPair += other.obviousPair
        this.obviousTriple += other.obviousTriple
        this.bruteForce += other.bruteForce
    }


    override fun isTooHighForDifficulty(difficulty: Difficulty): Boolean {
        return when(difficulty){
            Difficulty.BEGINNER -> this.get() > MAX_BEGINNER
            Difficulty.EASY -> this.get() > MAX_EASY
            Difficulty.MEDIUM -> this.get() > MAX_MEDIUM
            Difficulty.HARD -> this.get() > MAX_HARD || this.bruteForce > 0
            Difficulty.EXPERT -> this.get() > MAX_EXPERT || this.bruteForce > MAX_EXPERT_BRUTE_FORCES
            Difficulty.MASTER -> this.get() > MAX_MASTER || this.bruteForce > MAX_MASTER_BRUTE_FORCES
        }
    }

    override fun getDifficulty(): Difficulty {
        return when (this.get()) {
            in (MIN_BEGINNER..MAX_BEGINNER) -> Difficulty.BEGINNER
            in (MIN_EASY..MAX_EASY) -> Difficulty.EASY
            in (MIN_MEDIUM..MAX_MEDIUM) -> Difficulty.MEDIUM
            in (MIN_HARD..MAX_HARD) -> Difficulty.HARD
            in (MIN_EXPERT..MAX_EXPERT) -> Difficulty.EXPERT
            in (MIN_MASTER..MAX_MASTER) -> Difficulty.MASTER
            else -> throw Error("Invalid score $this used to get the difficulty")
        }
    }

    override fun serialize(): JsonElement {
        return Gson().toJsonTree(this).asJsonObject
    }

    override fun isTooLowForDifficulty(difficulty: Difficulty): Boolean {
        return when(difficulty){
            Difficulty.BEGINNER -> this.get() < MIN_BEGINNER
            Difficulty.EASY -> this.get() < MIN_EASY
            Difficulty.MEDIUM -> this.get() < MIN_MEDIUM
            Difficulty.HARD -> this.get() < MIN_HARD
            Difficulty.EXPERT -> this.get() < MIN_EXPERT
            Difficulty.MASTER -> this.get() < MIN_MASTER
        }
    }

    fun getBruteForceValue(): Int {
        return bruteForce
    }

    fun addScoreNewValue() {
        newValue += 1
    }

    fun addScoreRule3() {
        rule3 += 2
    }

    fun addScoreRule2() {
        rule2 += 2
    }

    fun addScoreHiddenSingle(numFound: Int) {
        hiddenSingle += numFound
    }

    fun addScoreHiddenPairs(numFound: Int) {
        hiddenPair += numFound
    }

    fun addScoreHiddenTriples(numFound: Int) {
        hiddenTriple += numFound
    }

    fun addScoreObviousPairs(numFound: Int) {
        obviousPair += numFound
    }

    fun addScoreObviousTriples(numFound: Int) {
        obviousTriple += numFound
    }

    fun addScoreBruteForce() {
        bruteForce++
    }

    companion object {
        const val MIN_BEGINNER = 1
        const val MAX_BEGINNER = 100

        const val MIN_EASY = 100
        const val MAX_EASY = 300

        const val MIN_MEDIUM = 300
        const val MAX_MEDIUM = 600

        const val MIN_HARD = 600
        const val MAX_HARD = 1400

        const val MIN_EXPERT = 1100
        const val MAX_EXPERT = 3000
        const val MAX_EXPERT_BRUTE_FORCES = 1

        const val MIN_MASTER = 1900
        const val MAX_MASTER = 5000
        const val MAX_MASTER_BRUTE_FORCES = 3

        fun getMaxBruteForceValue(difficulty: Difficulty): Int {
            return when(difficulty) {
                Difficulty.EXPERT -> MAX_EXPERT_BRUTE_FORCES
                Difficulty.MASTER -> MAX_MASTER_BRUTE_FORCES
                else -> 0
            }
        }
    }
}