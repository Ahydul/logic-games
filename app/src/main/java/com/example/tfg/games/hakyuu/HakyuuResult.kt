package com.example.tfg.games.hakyuu

import com.example.tfg.games.common.Score

class BruteForceValues(
    private val newPossibleValues: Array<MutableList<Int>>,
    private val newActualValues: IntArray,
    private val score: Score
) {
    operator fun component1() = newPossibleValues
    operator fun component2() = newActualValues
    operator fun component3() = score
}

open class GenericResult<T> private constructor(
    val value: T?,
    private val result: Result
) {
    operator fun component1() = value
    operator fun component2() = result

    enum class Result {
        SUCCESS,
        NO_CHANGES,
        CONTRADICTION,
        NOT_UNIQUE_SOLUTION,
        MAX_BF_OVERPASSED
    }

    fun gotSuccess(): Boolean {
        return this.result == Result.SUCCESS
    }
    
    fun get(): T? {
        return this.value
    }

    fun gotContradiction(): Boolean {
        return this.result == Result.CONTRADICTION
    }

    fun gotNoChangesFound(): Boolean {
        return this.result == Result.NO_CHANGES
    }

    fun errorToPopulateResult(): PopulateResult {
        return PopulateResult(null, this.result)
    }

    fun errorToBruteForceResult(): BruteForceResult {
        return BruteForceResult(null, this.result)
    }
    
    companion object {
        fun <T> success(value: T) = GenericResult(value, Result.SUCCESS)
        fun <T> contradiction() = GenericResult<T>(null, Result.CONTRADICTION)
        fun <T> maxBFOverpassed() = GenericResult<T>(null, Result.MAX_BF_OVERPASSED)
        fun <T> boardNotUnique() = GenericResult<T>(null, Result.NOT_UNIQUE_SOLUTION)
        fun <T> noChangesFound() = GenericResult<T>(null, Result.NO_CHANGES)
    }
}

typealias PopulateResult = GenericResult<Score>
typealias BruteForceResult = GenericResult<BruteForceValues>