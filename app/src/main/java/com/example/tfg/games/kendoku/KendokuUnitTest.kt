package com.example.tfg.games.kendoku

import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.CustomTestWatcher
import com.example.tfg.games.common.Difficulty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.random.Random

@ExtendWith(CustomTestWatcher::class)
class KendokuUnitTest {
    private val random = Random((Math.random()*10000000000).toLong())

    @Test
    fun curvesTest() {
        val size = 1000
        val boardSize = 15
        val values = IntArray(size)
        repeat(size) {
            val value = ((boardSize - 1) * Curves.lowerValues(
                random.nextDouble(1.0),
                upperLimitFactor = 0.6
            )).toInt() + 1
            values[it] = value
        }
        values.sort()
        val data = values.groupBy { it }.map { it.key to it.value.size }
        println(data)
    }

    @Test
    fun testGetRegionSumCombinations() {
        val kendoku = Kendoku(0, 3,0L)

        val possibleValues = arrayOf(
            mutableListOf(1,2,3), mutableListOf(2,3), mutableListOf(5,6),
            mutableListOf(8,9), mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(5,8),
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9)
        )


        val startTime = System.currentTimeMillis()
/*
        repeat(1000000) {
            val test2 = kendoku.getRegionSumCombinations(possibleValues = possibleValues, region = mutableListOf(4,6,7), sum = 18)
        }

 */

        val endTime = System.currentTimeMillis()
        println("Time: ${endTime - startTime} ms")

        //val test1 = kendoku.getRegionSumCombinations(possibleValues, mutableListOf(0, 1, 2, 3, 5),28)
        val test2 = kendoku.getRegionSumCombinations(possibleValues, mutableListOf(4,6,7,8), 18)

        println("${test2.size} combinations")
        test2.forEach { println(it.fold("") { acc, v -> "$acc $v" }) }

        assert(true)
    }

    @Test
    fun testCreateSeededKendokuBoard() {
        val size = 15
        val seed = 234242242L

        val getGameType = {
            Kendoku.create(size = size, seed = seed, difficulty = Difficulty.EXPERT)
        }

        testKendokuBoard(getGameType, getTest = {_: Kendoku -> true}, printHTML = true)
    }

    private fun testKendokuBoard(
        getGameType: () -> Kendoku,
        getTest: (Kendoku) -> Boolean = { gameType: Kendoku -> gameType.boardMeetsRules() && gameType.score.get() != 0 },
        print: Boolean = true,
        printHTML: Boolean = false,
    ) {
        val startTime = System.currentTimeMillis()
        val gameType = getGameType()
        val endTime = System.currentTimeMillis()

        if (print) {
            if (printHTML) {
                println(gameType.printStartBoardHTML())
                println("\n${gameType.printCompletedBoardHTML()}")
            } else {
                println(gameType.printStartBoard())
                println("\n${gameType.printCompletedBoard()}")
            }
        }

        println("Test with sizes ${gameType.numRows}x${gameType.numColumns}")
        println("Time: ${endTime - startTime} ms")
        println("Score: ${gameType.score.get()}")

        assert(getTest(gameType)) { "Failed with seed: ${gameType.seed} " }
    }

}