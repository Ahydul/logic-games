package com.example.tfg.games.kendoku

import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.CustomTestWatcher
import com.example.tfg.games.common.Difficulty
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.math.sqrt
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
        return possibleValues.split(";").map { it.split("").drop(1).dropLast(1).map { i -> i.toInt() }.toMutableList() }.toTypedArray()
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
        "5678;;;789;19;, 0;3;4, 25, None",
        "5678;;;19;789;, 0;3;4, 25, 898",
        "123456789;;;123456789;123456789;, 0;3;4, 26, 989",
        "123456789;;;123456789;123456789;, 0;3;4, 27, None",
        ";;;;;, 0;3;4, 27, None",
        "1;2;3;;;, 0;3;4, 27, None",
    )
    fun testGetRegionSumCombinations(possibleValuesInput: String, regionInput: String, sum: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 3, 3,0L)
        val possibleValues = parsePossibleValues(possibleValuesInput)
        val board = createBoard(possibleValues)
        val region = parseRegion(regionInput)

        val test = kendoku.getRegionCombinations(possibleValues, board, region, sum, KnownKendokuOperation.SUM)

        println(foldCombination(test))
        assert(foldCombination(test) == expectedResult || foldCombination(test) == "" && expectedResult == "None")
    }

    @ParameterizedTest
    @CsvSource(
        "123;23, 0;1, 1, 12;32;23",
        "123456789;58, 0;1, 3, 25;85;58",
        "123456789;123456789, 0;1, 7, 81;18;92;29",
        "12356789;4, 0;1, 2, 64;24",
        "123456789;123456789, 0;1, 10, None",
        ";, 0;1, 10, None",
    )
    fun testGetRegionSubtractCombinations(possibleValuesInput: String, regionInput: String, subtraction: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 9, 9,0L)

        val possibleValues = parsePossibleValues(possibleValuesInput)
        val board = createBoard(possibleValues)
        val region = parseRegion(regionInput)

        val test = kendoku.getRegionCombinations(possibleValues, board, region, subtraction, KnownKendokuOperation.SUBTRACT)

        println(foldCombination(test))
        assert(foldCombination(test) == expectedResult || foldCombination(test) == "" && expectedResult == "None")
    }

    @ParameterizedTest
    @CsvSource(
        "123;23;56;;123456789;, 0;1;2;4, 60, 2352;1354;1265;1256",
        ";123456789;;123456789;123456789;123456789, 1;3;4;5, 1200, 5865;5685;5586;5568",
        "5;12346789;12346789, 0;1;2, 40, 581;542;524;518",
        "12346789;12346789;12346789, 0;1;2, 11, None",
        "12346789;12346789;;12346789;, 0;1;3, 3, 311",
        "12346789;12346789;;12346789;, 0;1;3, 8, 811;421;412;241;214;142;124",
        "12346789;236789;3, 0;1;2, 12, None",
        ";;, 0;1;2, 12, None",
        "1;2;, 0;1;2, 12, None",
    )
    fun testGetRegionMultiplicationCombinations(possibleValuesInput: String, regionInput: String, multiplication: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 3, 3,0L)

        val possibleValues = parsePossibleValues(possibleValuesInput)
        val board = createBoard(possibleValues)
        val region = parseRegion(regionInput)

        val test = kendoku.getRegionCombinations(possibleValues, board, region, multiplication, KnownKendokuOperation.MULTIPLY)

        println(foldCombination(test))
        assert(foldCombination(test) == expectedResult || foldCombination(test) == "" && expectedResult == "None")
    }

    @ParameterizedTest
    @CsvSource(
        "123;23, 0;1, 3, 13",
        "123456789;12348, 0;1, 2, 12;21;24;42;63;48;84",
        "123456789;123456789, 0;1, 3, 13;31;26;62;39;93",
        "123456789;1234578, 0;1, 3, 13;31;62;93",
        "123456789;123456789, 0;1, 7, 17;71",
        "123456789;123456789, 0;1, 9, 19;91",
        "12356789;4, 0;1, 2, 24;84",
        "13456789;2, 0;1, 4, 82",
        "13456789;2, 0;1, 2, 12;42",
        "123456789;123456789, 0;1, 1, None",
        ";, 0;1, 1, None",
    )
    fun testGetRegionDivideCombinations(possibleValuesInput: String, regionInput: String, division: Int, expectedResult: String) {
        val kendoku = Kendoku(0, 9, 9,0L)

        val possibleValues = parsePossibleValues(possibleValuesInput)
        val board = createBoard(possibleValues)
        val region = parseRegion(regionInput)

        val test = kendoku.getRegionCombinations(possibleValues, board, region, division, KnownKendokuOperation.DIVIDE)

        println(foldCombination(test))
        assert(foldCombination(test) == expectedResult || foldCombination(test) == "" && expectedResult == "None")
    }


    /*
        Clean singles/pairs/triples functions Tests
     */

    private fun foldResult(line: Array<MutableList<Int>>): String {
        return line.joinToString(separator = ";") { it.joinToString(separator = "") }
    }
    private fun foldResult(line: MutableList<IntArray>): String {
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
        val kendoku = Kendoku(0, 7, 7,0L)
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
        val kendoku = Kendoku(0, 6, 6,0L)
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
        val kendoku = Kendoku(0, 7, 7,0L)
        val line = parsePossibleValues(input)

        val numTriples = kendoku.cleanNakedTriplesInLine(line)
        val foldResult = foldResult(line)

        println(foldResult)
        assert(foldResult == expectedResult)
        assert(numTriples == expectedNumTriples)
    }

    @ParameterizedTest
    @CsvSource(
        "7, 123;23;234;24;235;56;74, 3;0;0, 1;23;234;24;235;6;7",
        "7, 1;23;234;24;235;6;7, 1;0;0, 1;23;234;24;5;6;7",
        "7, 1345;2345;345;56;345;345;3457, 4;0;0, 1;2;345;6;345;345;7",
        "7, 1;2;345;6;345;345;7, 0;0;0, 1;2;345;6;345;345;7",
        "7, 1234567;12345;1234567;12345;12345;12345;12345, 0;1;0, 67;12345;67;12345;12345;12345;12345",
        "7, 34567;345;34567;345;345;12345;12345, 0;2;0, 67;345;67;345;345;12;12",
        "7, 12367;1235;12367;123;123;123;1234, 2;1;0, 67;5;67;123;123;123;4",
        "7, 1234567;1234;1234567;1234;1234567;1234;1234, 0;0;1, 567;1234;567;1234;567;1234;1234",
        "7, 123456;1234;1234567;1234;1234567;1234;1234, 0;0;1, 56;1234;567;1234;567;1234;1234",
        "7, 123456;1234;123457;1234;123467;1234;1234, 0;0;1, 56;1234;57;1234;67;1234;1234",
        "7, 3456;34;3457;34;3467;1234;1234, 0;1;1, 56;34;57;34;67;12;12",
        "4, 124;124;124;, 0;0;0, 124;124;124;",
    )
    fun testCleanHiddenSinglesPairsTriples(size: Int, input: String, expectedSPT: String, expectedResult: String) {
        val kendoku = Kendoku(0, size, size,0L)
        val line = parsePossibleValues(input)

        val numberSPT = kendoku.cleanHiddenSinglesPairsTriplesInline(line)
        val foldResult = foldResult(line)

        println(foldResult)
        println(numberSPT.toList())
        assert(foldResult == expectedResult)
        assert(numberSPT.toList() == expectedSPT.split(";").map { it.toInt() })
    }

    private fun parseCombinations(combinations: String): MutableList<IntArray> {
        return combinations.split(";").map { it.split("").drop(1).dropLast(1).map { i -> i.toInt() }.toIntArray() }.toMutableList()
    }

    @ParameterizedTest
    @CsvSource(
        "3, ;123;123;;;123;123;123;123, 0;3;4, 323;321;231, 1, ;123;123;;;123;13;123;123",
        "3, ;123;123;;;123;123;123;123, 0;3;4, 321;213, 2, ;123;123;;;23;13;123;123",
        "3, ;123;123;;;123;123;123;123, 0;3;4, 132;123, 2, ;123;123;;;1;123;123;123",
        "6, ;;;123456;123456;123456;;;;123456;123456;123456, 0;1;2;6;7;8, 654321;653321;562231;346321;246321, 1, ;;;12345;12345;12345;;;;123456;123456;123456",
        "6, ;;;123456;123456;123456;;;;123456;123456;123456, 0;1;2;6;7;8, 654321;653312;562231;346321;246321, 2, ;;;12345;12345;12345;;;;23456;23456;23456",
        "6, ;;;123456;123456;123456;;;;123456;123456;123456, 0;1;2;6;7;8, 654321;652321;562231;546321;246312, 3, ;;;12345;12345;12345;;;;2456;2456;2456",
        "6, ;;;12345;12345;12345;;;;2456;2456;2456, 0;1;2;6;7;8, 654321;652321;562231;546321;246312, 0, ;;;12345;12345;12345;;;;2456;2456;2456",
        "4, ;;;1234;;1234;1234;1234;1234;1234;1234;1234;1234;1234;1234;1234, 0;1;2;4, 4123;4213;1234, 3, ;;;34;;1234;1234;1234;123;1234;1234;1234;123;1234;1234;1234",
    )
    fun testCleanCageUnitOverlap(size: Int, possibleValuesInput: String, regionInput: String, combinationsInput: String, expectedNumber: Int, expectedResult: String) {
        val kendoku = Kendoku(0, size, size,0L)

        val possibleValues = parsePossibleValues(possibleValuesInput)
        val region = parseRegion(regionInput)
        val regionID = 1 // Must be any number != 0
        region.forEach { kendoku.boardRegions[it] = regionID }
        val combinations = parseCombinations(combinationsInput)

        val number = kendoku.cleanCageUnitOverlapType2(regionID, region, combinations, possibleValues)

        val foldResult = foldResult(possibleValues)
        println(foldResult)
        assert(foldResult == expectedResult)
        assert(number == expectedNumber)
    }

    @ParameterizedTest
    @CsvSource(
        "3, ;;12;123;123;123;123;123;123, 0;1, 12;21;13;31, 13;31, 1",
        "4, ;;1234;1234;;1234;1234;1234;234;1234;1234;1234;234;1234;1234;1234, 0;1;4, 123;124;132;144;142;321;231;213;432;243;324;244, 123;124;132;144;142;321;231, 1",
        "4, ;;123;123;;1234;1234;1234;234;1234;1234;1234;234;1234;1234;1234, 0;1;4, 123;124;132;144;142;321;231;213;432;243;324;244, 144;142, 2",
    )
    fun testCombinationHiddenSingle(size: Int, possibleValuesInput: String, regionInput: String, combinationsInput: String, expectedResult: String, expectedNumChanges: Int) {
        val kendoku = Kendoku(0, size, size,0L)

        val possibleValues = parsePossibleValues(possibleValuesInput)
        val region = parseRegion(regionInput)
        val regionID = 1 // Must be any number != 0
        region.forEach { kendoku.boardRegions[it] = regionID }
        val combinations = parseCombinations(combinationsInput)
        val regionIndexesPerColumn = mutableMapOf<Int, MutableList<Int>>()
        val regionIndexesPerRow = mutableMapOf<Int, MutableList<Int>>()
        region.forEachIndexed { index, position ->
            val coordinate = Coordinate.fromIndex(position, size, size)
            regionIndexesPerRow.getOrPut(coordinate.row) { mutableListOf() }.add(index)
            regionIndexesPerColumn.getOrPut(coordinate.column) { mutableListOf() }.add(index)
        }
        val actualValues = IntArray(size*size)

        val numChanges = kendoku.cageUnitOverlapType1(region, combinations, actualValues, possibleValues, regionIndexesPerColumn, regionIndexesPerRow)

        val foldResult = foldResult(combinations)

        println(foldResult)
        assert(foldResult == expectedResult)
        assert(numChanges == expectedNumChanges)
    }

    @ParameterizedTest
    @CsvSource(
        "3, ;12;123;;;123;123;123;123, 0;3;4, 123;213;231, 123;213;231, 0",
        "3, ;123;123;;;12;123;123;123, 0;3;4, 312;321;231, 231, 1",
        "3, ;123;123;;;123;12;123;123, 0;3;4, 123;213;231, 231, 1",
        "4, ;;;12;;1234;1234;1234;23;1234;1234;1234;1234;1234;1234;1234, 0;1;2;4, 1234;1244;1233;3412;2413;4321, 4321, 2",
    )
    fun testBiValueAttackOnRegion(size: Int, possibleValuesInput: String, regionInput: String, combinationsInput: String, expectedResult: String, expectedNumChanges: Int) {
        val kendoku = Kendoku(0, size, size,0L)

        val possibleValues = parsePossibleValues(possibleValuesInput)
        val region = parseRegion(regionInput)
        val regionID = 1 // Must be any number != 0
        region.forEach { kendoku.boardRegions[it] = regionID }
        val combinations = parseCombinations(combinationsInput)
        val regionIndexesPerColumn = mutableMapOf<Int, MutableList<Int>>()
        val regionIndexesPerRow = mutableMapOf<Int, MutableList<Int>>()
        region.forEachIndexed { index, position ->
            val coordinate = Coordinate.fromIndex(position, size, size)
            regionIndexesPerRow.getOrPut(coordinate.row) { mutableListOf() }.add(index)
            regionIndexesPerColumn.getOrPut(coordinate.column) { mutableListOf() }.add(index)
        }

        val numChanges = kendoku.biValueAttackOnRegion(region, possibleValues, combinations, regionIndexesPerColumn, regionIndexesPerRow)

        val foldResult = foldResult(combinations)

        println(foldResult)
        assert(foldResult == expectedResult)
        assert(numChanges == expectedNumChanges)
    }
/*
    @ParameterizedTest
    @CsvSource(
        "6, 25;46;13;12346;12346;35, 345-10, 2;6;3;12346;12346;35, 3",
        "6, 24;156;14;256;56;3;24;126;1234;36;14;5, 0167-13;23459-21, 24;156;14;256;56;3;24;126;2;36;1;5, 2",
    )
 */
    fun testCleanInniesAndOuties(size: Int, possibleValuesInput: String, regionSum: String, expectedResult: String, expectedNumChanges: Int) {
        val boardRegions = IntArray(size*size) { it }
        val possibleValues = Array(size*size) { (1..size).toMutableList() }
        val board = createBoard(possibleValues)

        val sumRegions = mutableListOf<Int>()
        val operationResultPerRegion = mutableMapOf<Int,Int>()
        var id = size*size
        regionSum.split(";").forEach { s ->
            val spl = s.split("-")
            val opSum = spl[1].toInt()
            val positions = spl[0].split("").drop(1).dropLast(1).map { it.toInt() }
            positions.forEach { boardRegions[it] = id }
            operationResultPerRegion[id] = opSum
            sumRegions.add(id)
            id++
        }

        val regions = mutableMapOf<Int, MutableList<Int>>()
        parsePossibleValues(possibleValuesInput).forEachIndexed { position, ints ->
            possibleValues[position] = ints
            val regionID = boardRegions[position]
            regions.getOrPut(regionID) { mutableListOf() }.add(position)
        }

        val kendoku = Kendoku(0, size, size,0L, boardRegions = boardRegions)

        val numChanges = kendoku.cleanInniesAndOuties(board, regions, possibleValues) { sumRegions.contains(it) }

        val foldResult = foldResult(possibleValues)

        println(foldResult)
        assert(foldResult.contains(expectedResult))
        assert(numChanges == expectedNumChanges)
    }

    @ParameterizedTest
    @CsvSource(
        "4, 0;1-2;3, 14;41;23;32-12;21;32;23;43;34, 14;41-32;23, 2",
        "4, 0;1-2;3, 21;21;34;43-12;21;13;31;24;42, 34;43-12;21, 2",
        "6, 0;1-2;3-4;5, 16;61;14;41;23;32-12;21;13;31;14;41;25;52;36;63-12;21;13;31;14;41;25;52;36;63, 14;41-25;52;36;63-25;52;36;63, 2",
        "6, 0;6-1;2-3;4-5;10;11, 14;41-12;21;13;31;15;51;24;42;36;63-12;21;13;31;15;51;24;42;36;63-661;334;236;263;326;362;623;632, 41-15;51;36;63-15;51;36;63-236;263, 2",
    )
    fun testCombinationComparison(size: Int, regionPositions: String, combinationsInput: String, expectedResult: String, expectedNumChanges: Int) {
        val parsePositions = { index: Int ->
            regionPositions.split("-")[index].split(";").map { it.toInt() }.toMutableList()
        }
        val regions = mutableMapOf<Int, MutableList<Int>>()
        val regionCombinations = mutableMapOf<Int, MutableList<IntArray>>()
        val regions2 = IntArray(size*size)
        var regionID = 0

        combinationsInput.split("-").forEachIndexed { index, input ->
            val positions = parsePositions(index)
            regions[regionID] = positions
            regionCombinations[regionID] = parseCombinations(input)
            positions.forEach { position -> regions2[position] = regionID }

            regionID++
        }

        val kendoku = Kendoku(0, size, size,0L, boardRegions = regions2)
        kendoku.combinationComparison((0..< size), regions, regionCombinations)

        val foldResult = regionCombinations.values.joinToString(separator = "-") { combinations -> foldResult(combinations) }

        println(foldResult)
        assert(foldResult == expectedResult)
        //assert(numChanges == expectedNumChanges)
    }

    @ParameterizedTest
    @CsvSource(
        "5, 12;;;;12;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;23;;;;23, 12;;;;12;1345;12345;12345;12345;1345;1345;12345;12345;12345;1345;1345;12345;12345;12345;1345;23;;;;23, 1",
        "5, 12;1345;1345;1345;12;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;12345;23;1345;1345;1345;23, 12;1345;1345;1345;12;1345;12345;12345;12345;1345;1345;12345;12345;12345;1345;1345;12345;12345;12345;1345;23;1345;1345;1345;23, 1",
        "5, 12;;;;12;14;1235;1235;1235;14;12345;12345;12345;12345;12345;45;1235;1235;1235;45;23;;;;23, 12;;;;12;14;1235;1235;1235;14;135;124;124;124;135;45;123;123;123;45;23;;;;, 4",
        "3, 123;123;123;123;123;123;123;123;123, 123;123;123;123;123;123;123;123;123, 0",
    )
    fun testCleanXWing(size: Int, possibleValuesInput: String, expectedResult: String, expectedNumChanges: Int) {
        val kendoku = Kendoku(0, size, size,0L)
        val possibleValues = parsePossibleValues(possibleValuesInput)

        val numChanges = kendoku.cleanXWing(possibleValues)
        val foldResult = foldResult(possibleValues)

        println(foldResult)
        assert(foldResult.contains(expectedResult))
        assert(numChanges == expectedNumChanges)
    }

    @ParameterizedTest
    @CsvSource(
        "4, 12;23;134;134;1234;13;1234;1234;1234;124;1234;1234;1234;124;1234;1234, 12;23;134;134;234;13;1234;1234;1234;124;1234;1234;1234;124;1234;1234",
    )
    fun testCleanYWing(size: Int, possibleValuesInput: String, expectedResult: String) {
        val kendoku = Kendoku(0, size, size,0L)
        val possibleValues = parsePossibleValues(possibleValuesInput)

        kendoku.cleanYWing(possibleValues)
        val foldResult = foldResult(possibleValues)

        println(foldResult)
        assert(foldResult.contains(expectedResult))
    }


    @ParameterizedTest
    @CsvSource(
        "4, 1234;123;123;1234;1234;123;1234;123;1234;1234;123;123;123;1234;1234;123, 123;123;123;1234;1234;123;1234;123;1234;1234;123;123;123;1234;1234;123, 1",
        "5, 1234;12345;1234;1234;12345;1234;12345;1234;12345;1234;1234;12345;12345;1234;1234;1234;1234;12345;12345;1234;12345;12345;1234;1234;12345, 1234;1234;1234;1234;12345;1234;12345;1234;12345;1234;1234;12345;12345;1234;1234;1234;1234;12345;12345;1234;12345;1234;1234;1234;12345, 1",
        "5, 1234;1345;1234;134;1345;1234;12345;134;1345;134;134;12345;12345;134;134;1234;134;1345;12345;1234;12345;1345;134;1234;12345, 1234;134;1234;134;1345;1234;12345;134;1345;134;134;12345;12345;134;134;134;134;1345;12345;1234;1345;134;134;1234;12345, 2",
    )
    fun testCleanColoring(size: Int, possibleValuesInput: String, expectedResult: String, expectedNumChanges: Int) {
        val kendoku = Kendoku(0, size, size,0L)
        val possibleValues = parsePossibleValues(possibleValuesInput)

        val numChanges = kendoku.cleanColoring(possibleValues)
        val foldResult = foldResult(possibleValues)

        println(foldResult)
        assert(foldResult == expectedResult)
        assert(numChanges == expectedNumChanges)
    }

    @ParameterizedTest
    @CsvSource(
        "4, 0;1;2;3;4, 0;1;4, 2;3",
        "4, 2;3;4;5;6, 2;3, 4;5;6",
        "4, 2;3;6;9;10, 2;3, 6;10;9",
        "4, 2;3;6;9;10;11, 2;3;6, 9;10;11",
        "4, 3;4;5;6, 3, 4;5;6",
        "4, 0;4;8;12, 0;4, 8;12",
        "4, 3;4;5;6;7;11;15, 3;7;11;15, 4;5;6",
    )
    fun testRegionDivide(size: Int, regionPositions: String, expectedPositions1: String, expectedPositions2: String) {
        val parsePositions = regionPositions.split(";").map { it.toInt() }

        val kendoku = Kendoku(0, size, size,0L)

        val res = kendoku.divideRegion(0, parsePositions)
        print(res)
        assert(res[0]?.joinToString(";") == expectedPositions1 && res[1]?.joinToString(";") == expectedPositions2)
    }


    /*
        Test Janko Boards
     */

    data class JankoKendokuBoard(
        val boardId: Int,
        val difficulty: String,
        val size: Int,
        val problem: String,
        val areas: String,
        val solution: String,
        var addValuesToStart: (startBoard: IntArray) -> Unit = {}
    ) {
        fun getStartBoard(): IntArray {
            val startBoard = IntArray(size*size) // Janko boards are always empty
            addValuesToStart(startBoard) // For debug
            return startBoard
        }

        fun getRegions() = areas.replace("\n"," ").split(" ").map { it.toInt() }.toIntArray()

        fun getCompletedBoard() = solution.replace("\n"," ").split(" ").map { it.toInt() }.toIntArray()

        fun getOperationsPerRegion(regions: IntArray): MutableMap<Int, KendokuOperation> {
            val operationsPerRegion = mutableMapOf<Int, KendokuOperation>()
            problem.replace("\n", " ").split(" ")
                .withIndex().filterNot { it.value=="-" || it.value=="." }
                .forEach { (index, s) ->
                    val regionID = regions[index]
                    val operation = if (s.indexOf("+") != -1) KendokuOperation.SUM
                        else if (s.indexOf("-") != -1) KendokuOperation.SUBTRACT
                        else if (s.indexOf("x") != -1 || s.indexOf("*") != -1) KendokuOperation.MULTIPLY
                        else if (s.indexOf("/") != -1) KendokuOperation.DIVIDE
                        else KendokuOperation.SUM_UNKNOWN
                    operationsPerRegion[regionID] = operation
                }
            return operationsPerRegion
        }

        fun getOperationResultPerRegion(regions: IntArray): MutableMap<Int, Int> {
            val operationResultPerRegion = mutableMapOf<Int, Int>()
            problem.replace("\n", " ").split(" ")
                .withIndex().filterNot { it.value=="-" || it.value=="." }
                .forEach { (index, s) ->
                    val regionID = regions[index]
                    val result = s.toIntOrNull() ?: s.dropLast(1).toInt()
                    operationResultPerRegion[regionID] = result
                }
            return operationResultPerRegion
        }
    }

    private fun loadKendokuData(): List<JankoKendokuBoard> {
        val file = File("src/test/testdata/kendoku-data.json")
        return Gson().fromJson(file.readText(), object : TypeToken<List<JankoKendokuBoard?>?>() {}.type)
    }

    private val scoreDebug = KendokuStrategy.entries.joinToString(", ") { strat ->
        strat.name.split("_").joinToString("") { s -> s.replaceFirstChar { it.uppercase() } }
    }

    @Test
    fun testOkJankoBoard() {
        // 47 paso de 1 a 2 brute forces
        val boardId = 3 //38 67 13 74 28 219 414 406 80 224
        val kendokuBoard = loadKendokuData()
        println("board, difficulty, score, times, bruteForces, regions, numUnknownOperations, $scoreDebug")
        val board = kendokuBoard.find { it.boardId == boardId } !!
        board.addValuesToStart = { }

        val result = testJankoBoard(board)
        assert(result == "") {
            print(result)
        }
    }

    @Test
    fun testOkJankoBoards() {
        val kendokuBoard = loadKendokuData()
        println("board, difficulty, score, times, brute-forces, regions, numUnknownOperations, $scoreDebug")

        val result = kendokuBoard/*.filter { it.boardId != 50 }*/.map { board ->
            board.addValuesToStart = { }
            testJankoBoard(board)
        }
        val resultNotOK = result.filter { it != "" }
        assert(resultNotOK.isEmpty()) {
            println("\nERRORS:")
            print(resultNotOK.joinToString(separator = "\n"))
        }
    }

    @Test
    fun testOkSeededBoard() {
        println("size, seed, difficulty, score, times, brute-forces, regions, numUnknownOperations, $scoreDebug")
        val size = 9
        val timeout = 1500L
        val seed: Long = 1034942735
        val difficulty = Difficulty.MASTER
        val printBoards = false

        var result: String? = null
        runBlocking {
            result = withTimeoutOrNull(timeout) {
                testBoard(size, difficulty, seed, printBoards)
            }
        }

        assert(result == "") {
            print(result ?: "Timed out")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [3,4,5,6,7,8,9])
    fun testOkBoards(size: Int) = runBlocking {
        println("size, seed, difficulty, score, times, brute-forces, regions, numUnknownOperations, $scoreDebug")

        val repeat = 100
        val timeout = 1500L
        val difficulty = Difficulty.MASTER
        val printBoards = false

        val seedsWithTimeout = mutableListOf<Long>()
        val result = runBlocking { (1..repeat).map {
            var res: String?
            do {
                val seed = (Math.random()*10000000000).toLong()
                res = withTimeoutOrNull(timeout) { testBoard(size, difficulty, seed, printBoards) }
                if (res == null) seedsWithTimeout.add(seed)
            } while (res == null)
            res
        } }

        println("${seedsWithTimeout.size} timeouts with timeout: $timeout")
        println("Seeds: ${seedsWithTimeout.joinToString()}")

        val resultNotOK = result.filter { it != "" }
        assert(resultNotOK.isEmpty()) {
            println("\nERRORS:")
            print(resultNotOK.joinToString(separator = "\n"))
        }
    }


    private fun testJankoBoard(board: JankoKendokuBoard, seed: Long = (Math.random()*10000000000).toLong()): String {
        val regions = board.getRegions()

        val startTime = System.currentTimeMillis()
        val kendoku = Kendoku.solveBoard(
            seed = seed,
            size = sqrt(regions.size.toDouble()).toInt(),
            startBoard = board.getStartBoard(),
            completedBoard = board.getCompletedBoard(),
            regions = regions,
            operationPerRegion = board.getOperationsPerRegion(regions)
        )
        val endTime = System.currentTimeMillis()

        val correctBoard = kendoku.startBoard.contentEquals(kendoku.completedBoard)
        val numBruteForces = kendoku.score.getBruteForceValue()

        println("${board.boardId}, ${board.difficulty}, ${kendoku.getScoreValue()}, ${endTime - startTime}, $numBruteForces, ${kendoku.getRegionStatData().joinToString(separator = "|")}, ${kendoku.operationPerRegion.values.count { it.isUnknown() }}, ${(kendoku.score as KendokuScore)}")

        return if (correctBoard) ""
        else "\nBoard: ${board.boardId} is incorrect" +
            "\nActual board:\n${kendoku.printStartBoard()}" +
            "\nExpected board:\n${kendoku.printCompletedBoard()}"
    }

    private suspend fun testBoard(size: Int, difficulty: Difficulty, seed: Long, printBoards: Boolean): String {
        val mainStartTime = System.currentTimeMillis()

        val kendoku = Kendoku.createTesting(
            size = size,
            seed = seed,
            difficulty = difficulty
        )

        val score = kendoku.getScoreValue()
        val time = System.currentTimeMillis() - mainStartTime
        val numBruteForces = kendoku.score.getBruteForceValue()
        val regions = kendoku.getRegionStatData().joinToString(separator = "|")
        val numUnknownOps = kendoku.operationPerRegion.values.count { it.isUnknown() }
        val completeScore = kendoku.score as KendokuScore

        println("${size}x${size}, $seed, $difficulty, $score, $time, $numBruteForces, $regions, $numUnknownOps, $completeScore")

        if (printBoards) println(kendoku.printStartBoardHTML())

        return kendoku.boardMeetsRulesStr()
    }
}