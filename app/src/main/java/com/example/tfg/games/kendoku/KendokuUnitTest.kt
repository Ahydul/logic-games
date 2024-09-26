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
/*
        val startTime = System.currentTimeMillis()
        repeat(1000000) {
            val test2 = kendoku.getRegionSumCombinations(possibleValues = possibleValues, region = mutableListOf(4,6,7), sum = 18)
        }
        val endTime = System.currentTimeMillis()
        println("Time: ${endTime - startTime} ms")
 */

        val test1 = kendoku.getRegionSumCombinations(possibleValues, mutableListOf(0, 1, 2, 3, 5),28)
        val test2 = kendoku.getRegionSumCombinations(possibleValues, mutableListOf(4,6,7,8), 10)

        //println("${test2.size} combinations")
        //test2.forEach { println(it.joinToString(separator = "")) }

        val foldResult = { result: List<IntArray> ->
            result.joinToString(separator = ";") { arr -> arr.joinToString(separator = "") }
        }

        assert(foldResult(test1) == "32698;23698")
        assert(foldResult(test2) == "4321;4312;4231;4213;4132;4123;3421;3412;3241;3214;3142;3124;2512;2431;2413;2341;2314;2251;2215;2152;2143;2134;1621;1531;1432;1423;1351;1342;1324;1261;1243;1234;1162;1153;1135;1126")
    }

    @Test
    fun testGetRegionSubtractCombinations() {
        val kendoku = Kendoku(0, 9,0L)

        val possibleValues = arrayOf(
            mutableListOf(1,2,3), mutableListOf(2,3),
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(5,8),
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9)
        )

        val test1 = kendoku.getRegionSubtractCombinations(possibleValues, mutableListOf(0, 1),1)
        val test2 = kendoku.getRegionSubtractCombinations(possibleValues, mutableListOf(2,3), 3)
        val test3 = kendoku.getRegionSubtractCombinations(possibleValues, mutableListOf(4,5), 7)

        val foldResult = { result: List<IntArray> ->
            result.joinToString(separator = ";") { arr -> arr.joinToString(separator = "") }
        }

        assert(foldResult(test1) == "12;32;23")
        assert(foldResult(test2) == "25;85;58")
        assert(foldResult(test3) == "81;18;92;29")
    }

    @Test
    fun testGetRegionMultiplicationCombinations() {
        val kendoku = Kendoku(0, 3,0L)

        val possibleValues = arrayOf(
            mutableListOf(1,2,3), mutableListOf(2,3), mutableListOf(5,6),
            mutableListOf(8,9), mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(5,8),
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9)
        )

        val test1 = kendoku.getRegionMultiplyCombinations(possibleValues, mutableListOf(0, 1, 2, 4), 60)
        val test2 = kendoku.getRegionMultiplyCombinations(possibleValues, mutableListOf(4,6,7,8), 1200)

        val foldResult = { result: List<IntArray> ->
            result.joinToString(separator = ";") { arr -> arr.joinToString(separator = "") }
        }

        assert(foldResult(test1) == "2352;1354;1265;1256")
        assert(foldResult(test2) == "5865;5685;5586;5568")
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