package com.example.tfg.games.kendoku

import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.CustomTestWatcher
import com.example.tfg.games.common.AbstractGame
import com.example.tfg.games.common.AbstractGameUnitTest
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.JankoBoard
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import kotlin.math.sqrt
import kotlin.random.Random

data class JankoKendokuBoard(
    override val boardId: Int,
    override val difficulty: String,
    override val size: Int,
    override val problem: String,
    override val areas: String,
    override val solution: String,
    override var addValuesToStart: (startBoard: IntArray) -> Unit
): JankoBoard {

    fun getOperationsPerRegion(regions: IntArray): MutableMap<Int, KendokuOperation> {
        val operationResultPerRegion =  getOperationResultPerRegion(regions)
        val completedBoard = getCompletedBoard()
        val positionsPerRegion = regions.indices.groupBy { position -> regions[position] }
        val operationsPerRegion = mutableMapOf<Int, KendokuOperation>()
        problem.replace("\n", " ").split(" ")
            .withIndex().filterNot { it.value=="-" || it.value=="." }
            .forEach { (index, s) ->
                val regionID = regions[index]
                val operation = if (s.indexOf("+") != -1) KendokuOperation.SUM
                else if (s.indexOf("-") != -1) KendokuOperation.SUBTRACT
                else if (s.indexOf("x") != -1 || s.indexOf("*") != -1) KendokuOperation.MULTIPLY
                else if (s.indexOf("/") != -1) KendokuOperation.DIVIDE
                else {
                    KnownKendokuOperation.entries.find {
                        it.operate(positionsPerRegion[regionID]!!.map { pos -> completedBoard[pos] }) == operationResultPerRegion[regionID]
                    }!!.toGeneralEnum().reverse()
                }
                operationsPerRegion[regionID] = operation
            }
        return operationsPerRegion
    }

    private fun getOperationResultPerRegion(regions: IntArray): MutableMap<Int, Int> {
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

@ExtendWith(CustomTestWatcher::class)
class KendokuUnitTest : AbstractGameUnitTest(
    enumEntries = KendokuStrategy.entries,
    getScore = { gameBoard: AbstractGame ->
        (gameBoard.score as KendokuScore).toString()
    }
) {
    private val random = Random((Math.random()*10000000000).toLong())

    override fun loadJankoData(): List<JankoBoard> {
        val file = File("src/test/testdata/kendoku-data.json")
        return Gson().fromJson(file.readText(), object : TypeToken<List<JankoKendokuBoard?>?>() {}.type)
    }

    override suspend fun getGameBoard(size: Int, seed: Long, difficulty: Difficulty): AbstractGame {
        return Kendoku.createTesting(
            size = size,
            seed = seed,
            difficulty = difficulty
        )
    }

    override fun getGameBoardJanko(seed: Long, jankoBoard: JankoBoard): AbstractGame {
        val regions = jankoBoard.getRegions()
        return Kendoku.solveBoard(
            seed = seed,
            size = sqrt(regions.size.toDouble()).toInt(),
            startBoard = jankoBoard.getStartBoard(),
            completedBoard = jankoBoard.getCompletedBoard(),
            regions = regions,
            operationPerRegion = (jankoBoard as JankoKendokuBoard).getOperationsPerRegion(regions)
        )
    }

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


    private fun foldResult(line: Array<MutableList<Int>>): String {
        return line.joinToString(separator = ";") { it.joinToString(separator = "") }
    }
    private fun foldResult(line: MutableList<IntArray>): String {
        return line.joinToString(separator = ";") { it.joinToString(separator = "") }
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

    @Test
    fun testOkJankoBoard() {
        // 47 paso de 1 a 2 brute forces
        val boardId = 67 //38 67 13 74 28 219 414 406 80 224
        testOkJankoBoard(boardId)
    }

}