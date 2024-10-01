package com.example.tfg.games.kendoku

import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.CustomTestWatcher
import com.example.tfg.games.common.Difficulty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9),
            mutableListOf(), mutableListOf(1,3,4,5,6,7,8,9), mutableListOf(1,3,4), mutableListOf()
        )

        val test1 = kendoku.getRegionSumCombinations(possibleValues, mutableListOf(0, 1, 2, 3, 5),28)
        val test2 = kendoku.getRegionSumCombinations(possibleValues, mutableListOf(4,6,7,8), 10)

        val board = IntArray(13)
        board[9] = 2
        board[12] = 3

        val test3 = kendoku.getRegionCombinations(possibleValues, board, mutableListOf(9,10,11,12), 10, KnownKendokuOperation.SUM)

        val foldResult = { result: List<IntArray> ->
            result.joinToString(separator = ";") { arr -> arr.joinToString(separator = "") }
        }

        assert(foldResult(test1) == "32698;23698")
        assert(foldResult(test2) == "4321;4312;4231;4213;4132;4123;3421;3412;3241;3214;3142;3124;2512;2431;2413;2341;2314;2251;2215;2152;2143;2134;1621;1531;1432;1423;1351;1342;1324;1261;1243;1234;1162;1153;1135;1126")
        assert(foldResult(test3) == "2413;2143")
    }

    @Test
    fun testGetRegionSubtractCombinations() {
        val kendoku = Kendoku(0, 9,0L)

        val possibleValues = arrayOf(
            mutableListOf(1,2,3), mutableListOf(2,3),
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(5,8),
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9),
            mutableListOf(1,2,3,5,6,7,8,9), mutableListOf()
        )
        val board = IntArray(9)
        board[7] = 4

        val test1 = kendoku.getRegionSubtractCombinations(possibleValues, board, mutableListOf(0, 1),1)
        val test2 = kendoku.getRegionSubtractCombinations(possibleValues, board, mutableListOf(2,3), 3)
        val test3 = kendoku.getRegionSubtractCombinations(possibleValues, board, mutableListOf(4,5), 7)
        val test4 = kendoku.getRegionSubtractCombinations(possibleValues, board, mutableListOf(6,7), 2)

        val foldResult = { result: List<IntArray> ->
            result.joinToString(separator = ";") { arr -> arr.joinToString(separator = "") }
        }

        assert(foldResult(test1) == "12;32;23")
        assert(foldResult(test2) == "25;85;58")
        assert(foldResult(test3) == "81;18;92;29")
        assert(foldResult(test4) == "64;24")
    }

    @Test
    fun testGetRegionMultiplicationCombinations() {
        val kendoku = Kendoku(0, 3,0L)

        val possibleValues = arrayOf(
            mutableListOf(1,2,3), mutableListOf(2,3), mutableListOf(5,6),
            mutableListOf(8,9), mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(5,8),
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9),
            mutableListOf(), mutableListOf(1,2,3,4,6,7,8,9), mutableListOf(1,2,3,4,6,7,8,9)
        )

        val test1 = kendoku.getRegionMultiplyCombinations(possibleValues, mutableListOf(0, 1, 2, 4), 60)
        val test2 = kendoku.getRegionMultiplyCombinations(possibleValues, mutableListOf(4,6,7,8), 1200)

        val board = IntArray(12)
        board[9] = 5

        val test3 = kendoku.getRegionCombinations(possibleValues, board, mutableListOf(9,10,11), 40, KnownKendokuOperation.MULTIPLY)

        val foldResult = { result: List<IntArray> ->
            result.joinToString(separator = ";") { arr -> arr.joinToString(separator = "") }
        }

        assert(foldResult(test1) == "2352;1354;1265;1256")
        assert(foldResult(test2) == "5865;5685;5586;5568")
    }

    @Test
    fun testGetRegionDivideCombinations() {
        val kendoku = Kendoku(0, 9,0L)

        val possibleValues = arrayOf(
            mutableListOf(1,2,3), mutableListOf(2,3),
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,8),
            mutableListOf(1,2,3,4,5,6,7,8,9), mutableListOf(1,2,3,4,5,6,7,8,9),
            mutableListOf(1,2,3,5,6,7,8,9), mutableListOf()
        )
        val board = IntArray(9)
        board[7] = 4

        val test1 = kendoku.getRegionDivideCombinations(possibleValues, board, mutableListOf(0, 1),3)
        val test2 = kendoku.getRegionDivideCombinations(possibleValues, board, mutableListOf(2,3), 2)
        val test3 = kendoku.getRegionDivideCombinations(possibleValues, board, mutableListOf(4,5), 7)
        val test4 = kendoku.getRegionDivideCombinations(possibleValues, board, mutableListOf(6,7), 2)

        val foldResult = { result: List<IntArray> ->
            result.joinToString(separator = ";") { arr -> arr.joinToString(separator = "") }
        }

        assert(foldResult(test1) == "13")
        assert(foldResult(test2) == "12;21;24;42;63;48;84")
        assert(foldResult(test3) == "17;71")
        assert(foldResult(test4) == "24;84")
    }

    private fun foldResult(line: Array<MutableList<Int>>): String {
        return line.joinToString(separator = ";") { it.joinToString(separator = "") }
    }

    private fun decodeInput(input: String): Array<MutableList<Int>> {
        return input.split(";").map { it.split("").drop(1).dropLast(1).map { it.toInt() }.toMutableList() }.toTypedArray()
    }

    @Test
    fun testCleanHiddenSingles() {
        val kendoku = Kendoku(0, 6,0L)
        val input = "12345;2345;345;345;345;56"
        val line = decodeInput(input)

        var singles = kendoku.cleanHiddenSingles(line)
        assert(foldResult(line) == "1;2345;345;345;345;6")
        assert(singles == 2)

        singles = kendoku.cleanHiddenSingles(line)
        assert(foldResult(line) == "1;2;345;345;345;6")
        assert(singles == 1)

        singles = kendoku.cleanHiddenSingles(line)
        assert(foldResult(line) == "1;2;345;345;345;6")
        assert(singles == 0)
    }

    @ParameterizedTest
    @CsvSource(
        "12345;23;23;23456;56;56, 2, 14;23;23;4;56;56",
        "12;12;1234567;1234567;1234567;1234567;1234567, 1, 12;12;34567;34567;34567;34567;34567",
        "12;12;34;34;56;56;1234567, 3, 12;12;34;34;56;56;7",
    )
    fun testCleanNakedPairs(input: String, expectedNumPairs: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 6,0L)
        val line = decodeInput(input)

        val numPairs = kendoku.cleanNakedPairsInLine(line)
        val foldResult = foldResult(line)

        println(foldResult)
        assert(foldResult == expectedResult)
        assert(numPairs == expectedNumPairs)
    }

    @ParameterizedTest
    @CsvSource(
        "12345;2345;345;345;345;56, 1, 1;23;234;24;5;56;7",
        "123;123;12;1234567;1234567;1234567;1234567, 1, 123;123;12;4567;4567;4567;4567",
        "123;123;123;456;456;456;1234567, 2, 123;123;123;456;456;456;7",
        "123;23;24;43;1234567;1234567;1234567, 1, 1;23;24;43;1567;1567;1567",
    )
    fun testCleanNakedTriples(input: String, expectedNumTriples: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 7,0L)
        val line = decodeInput(input)

        val numTriples = kendoku.cleanNakedTriplesInLine(line)
        val foldResult = foldResult(line)

        println(foldResult)
        assert(foldResult == expectedResult)
        assert(numTriples == expectedNumTriples)
    }

    @ParameterizedTest
    @CsvSource(
        "123;23;234;24;235;56;74, 3;0;0, 1;23;234;24;235;6;7",
        "1;23;234;24;235;6;7, 1;0;0, 1;23;234;24;5;6;7",
        "1345;2345;345;56;345;345;3457, 4;0;0, 1;2;345;6;345;345;7",
        "1;2;345;6;345;345;7, 0;0;0, 1;2;345;6;345;345;7",
        "1234567;12345;1234567;12345;12345;12345;12345, 0;1;0, 67;12345;67;12345;12345;12345;12345",
        "34567;345;34567;345;345;12345;12345, 0;2;0, 67;345;67;345;345;12;12",
        "12367;1235;12367;123;123;123;1234, 2;1;0, 67;5;67;123;123;123;4",
        "1234567;1234;1234567;1234;1234567;1234;1234, 0;0;1, 567;1234;567;1234;567;1234;1234",
        "123456;1234;1234567;1234;1234567;1234;1234, 0;0;1, 56;1234;567;1234;567;1234;1234",
        "123456;1234;123457;1234;123467;1234;1234, 0;0;1, 56;1234;57;1234;67;1234;1234",
        "3456;34;3457;34;3467;1234;1234, 0;1;1, 56;34;57;34;67;12;12",
    )
    fun testCleanHiddenSinglesPairsTriples(input: String, expectedSPT: String, expectedResult: String) {
        val kendoku = Kendoku(0, 7,0L)
        val line = decodeInput(input)

        val numberSPT = kendoku.cleanHiddenSinglesPairsTriplesInline(line)
        val foldResult = foldResult(line)

        println(foldResult)
        println(numberSPT.toList())
        assert(foldResult == expectedResult)
        assert(numberSPT.toList() == expectedSPT.split(";").map { it.toInt() })
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
        getTest: (Kendoku) -> Boolean = { gameType: Kendoku -> gameType.boardMeetsRulesPrintingInfo() && gameType.score.get() != 0 },
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