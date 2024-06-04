package com.example.tfg.games.hakyuu

class BruteForceResult(
    private val newPossibleValues: Array<MutableList<Int>>,
    private val newActualValues: IntArray,
    private val newFoundSPT: MutableList<Int>,
    private val result: HakyuuScore
) {
    operator fun component1() = newPossibleValues
    operator fun component2() = newActualValues
    operator fun component3() = newFoundSPT
    operator fun component4() = result
}