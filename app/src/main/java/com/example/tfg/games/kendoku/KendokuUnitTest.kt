package com.example.tfg.games.kendoku

import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.CustomTestWatcher
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

    /*
        Combination functions Tests
     */

    private fun foldCombination(combination: List<IntArray>): String {
        return combination.joinToString(separator = ";") { arr -> arr.joinToString(separator = "") }
    }
    private fun parseRegion(region: String): MutableList<Int> {
        return region.split(";").map { it.toInt() }.toMutableList()
    }
    private fun parsePossibleValues(possibleValues: String): Array<MutableList<Int>> {
        return possibleValues.split(";").map { it.split("").drop(1).dropLast(1).map { it.toInt() }.toMutableList() }.toTypedArray()
    }
    private fun createBoard(possibleValues: Array<MutableList<Int>>): IntArray {
        return IntArray(possibleValues.size) {
            val possValues = possibleValues[it]
            if (possValues.size == 1) possValues.first()
            else 0
        }
    }

    @ParameterizedTest
    @CsvSource(
        "123;23;56;89;;123456789, 0;1;2;3;5, 28, 32698;32689;23698;23689",
        ";123456789;;123456789;123456789;123456789, 1;3;4;5, 10, 4321;4312;4231;4213;4132;4123;3421;3412;3241;3214;3142;3124;2512;2431;2413;2341;2314;2251;2215;2152;2143;2134;1621;1531;1432;1423;1351;1342;1324;1261;1243;1234;1162;1153;1135;1126",
        "2;13456789;134;3, 0;1;2;3, 10, 2413;2143",
    )
    fun testGetRegionSumCombinations(possibleValuesInput: String, regionInput: String, sum: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 3,0L)
        val possibleValues = parsePossibleValues(possibleValuesInput)
        val board = createBoard(possibleValues)
        val region = parseRegion(regionInput)

        val test = kendoku.getRegionCombinations(possibleValues, board, region, sum, KnownKendokuOperation.SUM)

        println(foldCombination(test))

        println(foldCombination(test))
        assert(foldCombination(test) == expectedResult)
    }

    @ParameterizedTest
    @CsvSource(
        "123;23, 0;1, 1, 12;32;23",
        "123456789;58, 0;1, 3, 25;85;58",
        "123456789;123456789, 0;1, 7, 81;18;92;29",
        "12356789;4, 0;1, 2, 64;24",
    )
    fun testGetRegionSubtractCombinations(possibleValuesInput: String, regionInput: String, subtraction: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 9,0L)

        val possibleValues = parsePossibleValues(possibleValuesInput)
        val board = createBoard(possibleValues)
        val region = parseRegion(regionInput)

        val test = kendoku.getRegionCombinations(possibleValues, board, region, subtraction, KnownKendokuOperation.SUBTRACT)

        println(foldCombination(test))
        assert(foldCombination(test) == expectedResult)
    }

    @ParameterizedTest
    @CsvSource(
        "123;23;56;;123456789;, 0;1;2;4, 60, 2352;1354;1265;1256",
        ";123456789;;123456789;123456789;123456789, 1;3;4;5, 1200, 5865;5685;5586;5568",
        "5;12346789;12346789, 0;1;2, 40, 581;542;524;518",
    )
    fun testGetRegionMultiplicationCombinations(possibleValuesInput: String, regionInput: String, multiplication: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 3,0L)

        val possibleValues = parsePossibleValues(possibleValuesInput)
        val board = createBoard(possibleValues)
        val region = parseRegion(regionInput)

        val test = kendoku.getRegionCombinations(possibleValues, board, region, multiplication, KnownKendokuOperation.MULTIPLY)

        println(foldCombination(test))
        assert(foldCombination(test) == expectedResult)
    }

    @ParameterizedTest
    @CsvSource(
        "123;23, 0;1, 3, 13",
        "123456789;12348, 0;1, 2, 12;21;24;42;63;48;84",
        "123456789;123456789, 0;1, 7, 17;71",
        "12356789;4, 0;1, 2, 24;84",
        "13456789;2, 0;1, 4, 82",
        "13456789;2, 0;1, 2, 12;42",
    )
    fun testGetRegionDivideCombinations(possibleValuesInput: String, regionInput: String, division: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 9,0L)

        val possibleValues = parsePossibleValues(possibleValuesInput)
        val board = createBoard(possibleValues)
        val region = parseRegion(regionInput)

        val test = kendoku.getRegionCombinations(possibleValues, board, region, division, KnownKendokuOperation.DIVIDE)

        println(foldCombination(test))
        assert(foldCombination(test) == expectedResult)
    }


    /*
        Clean singles/pairs/triples functions Tests
     */

    private fun foldResult(line: Array<MutableList<Int>>): String {
        return line.joinToString(separator = ";") { it.joinToString(separator = "") }
    }

    @ParameterizedTest
    @CsvSource(
        "123;23;234;24;235;56;74, 3, 1;23;234;24;235;6;7",
        "1;23;234;24;235;6;7, 1, 1;23;234;24;5;6;7",
        "1345;2345;345;56;345;345;3457, 4, 1;2;345;6;345;345;7",
        "1;2;345;6;345;345;7, 0, 1;2;345;6;345;345;7",
    )
    fun testCleanHiddenSingles(input: String, expectedNumSingles: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 7,0L)
        val line = parsePossibleValues(input)

        val numberSingles = kendoku.cleanHiddenSingles(line)
        val foldResult = foldResult(line)

        println(foldResult)
        assert(foldResult == expectedResult)
        assert(numberSingles == expectedNumSingles)
    }

    @ParameterizedTest
    @CsvSource(
        "12345;23;23;23456;56;56, 2, 14;23;23;4;56;56",
        "12;12;1234567;1234567;1234567;1234567;1234567, 1, 12;12;34567;34567;34567;34567;34567",
        "12;12;34;34;56;56;1234567, 3, 12;12;34;34;56;56;7",
    )
    fun testCleanNakedPairs(input: String, expectedNumPairs: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 6,0L)
        val line = parsePossibleValues(input)

        val numPairs = kendoku.cleanNakedPairsInLine(line)
        val foldResult = foldResult(line)

        println(foldResult)
        assert(foldResult == expectedResult)
        assert(numPairs == expectedNumPairs)
    }

    @ParameterizedTest
    @CsvSource(
        "12345;2345;345;345;345;56, 1, 12;2;345;345;345;6",
        "123;123;12;1234567;1234567;1234567;1234567, 1, 123;123;12;4567;4567;4567;4567",
        "123;123;123;456;456;456;1234567, 2, 123;123;123;456;456;456;7",
        "123;23;24;43;1234567;1234567;1234567, 1, 1;23;24;43;1567;1567;1567",
    )
    fun testCleanNakedTriples(input: String, expectedNumTriples: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 7,0L)
        val line = parsePossibleValues(input)

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
        val line = parsePossibleValues(input)

        val numberSPT = kendoku.cleanHiddenSinglesPairsTriplesInline(line)
        val foldResult = foldResult(line)

        println(foldResult)
        println(numberSPT.toList())
        assert(foldResult == expectedResult)
        assert(numberSPT.toList() == expectedSPT.split(";").map { it.toInt() })
    }

/*
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
*/
}