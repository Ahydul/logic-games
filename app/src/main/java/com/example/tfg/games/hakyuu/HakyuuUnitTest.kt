package com.example.tfg.games.hakyuu

import com.example.tfg.common.GameFactory
import com.example.tfg.common.utils.CustomTestWatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExtendWith(CustomTestWatcher::class)
class HakyuuUnitTest {
    private lateinit var hakyuu: Hakyuu
    private lateinit var region : List<Int>
    private lateinit var possibleValues: Array<MutableList<Int>>
    private val random = Random((Math.random()*10000000000).toLong())

    @BeforeEach
    fun setUp() {
        hakyuu = GameFactory.exampleHakyuu()
        region = (0..<9).toList()
        possibleValues = region.map { (1..9).toMutableList() }.toTypedArray()
    }

    private fun randomDistinctInt(possibleValues: MutableList<Int>): Int {
        require(possibleValues.isNotEmpty())

        val res = possibleValues.random(random)
        possibleValues.remove(res)
        return res
    }

    private fun twoRandomDistinctInts(possibleValues: MutableList<Int>): Pair<Int,Int> {
        return Pair(randomDistinctInt(possibleValues), randomDistinctInt(possibleValues))
    }

    @RepeatedTest(10)
    fun testDetectObviousPairs() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val values1 = twoRandomDistinctInts(possibleValues=list).toList()
        val pos1 = twoRandomDistinctInts(possibleValues=list)
        val position1 = region[pos1.first]
        val position2 = region[pos1.second]
        possibleValues[position1] = values1.toMutableList()
        possibleValues[position2] = values1.toMutableList()

        val values2 = twoRandomDistinctInts(possibleValues=list).toList()
        val pos2 = twoRandomDistinctInts(possibleValues=list)
        val position3 = region[pos2.first]
        val position4 = region[pos2.second]
        possibleValues[position3] = values2.toMutableList()
        possibleValues[position4] = values2.toMutableList()

        val detectedPairs = hakyuu.cleanObviousPairs(possibleValues = possibleValues, region = region)

        assert(detectedPairs.containsAll(listOf(position1,position2,position3,position4)))
    }

    private fun threeRandomDistinctInts(possibleValues: MutableList<Int>): Triple<Int,Int,Int> {
        return Triple(randomDistinctInt(possibleValues), randomDistinctInt(possibleValues), randomDistinctInt(possibleValues))
    }


    @RepeatedTest(10)
    fun testDetectObviousTriplesA() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val values1 = threeRandomDistinctInts(possibleValues=list).toList()
        val coordinates = threeRandomDistinctInts(possibleValues=list)
        val pos1 = region[coordinates.first]
        val pos2 = region[coordinates.second]
        val pos3 = region[coordinates.third]
        possibleValues[pos1] = values1.toMutableList()
        possibleValues[pos2] = values1.toMutableList()
        possibleValues[pos3] = values1.toMutableList()


        val detectedTriples = hakyuu.cleanObviousTriples(possibleValues = possibleValues, region = region)

        assert(detectedTriples.containsAll(listOf(pos1,pos2,pos3)))
    }

    @RepeatedTest(10)
    fun testDetectObviousTriplesB() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val values1 = threeRandomDistinctInts(possibleValues=list).toList()
        val coordinates = threeRandomDistinctInts(possibleValues=list)
        val pos1 = region[coordinates.first]
        val pos2 = region[coordinates.second]
        val pos3 = region[coordinates.third]
        possibleValues[pos1] = values1.toMutableList()
        possibleValues[pos2] = values1.toMutableList()

        //We delete a random number from values1 for the next values
        possibleValues[pos3] = values1.subtract(listOf(values1.random(random)).toSet()).toList().toMutableList()


        val detectedTriples = hakyuu.cleanObviousTriples(possibleValues = possibleValues, region = region)

        assert(detectedTriples.containsAll(listOf(pos1,pos2,pos3)))
    }

    @RepeatedTest(10)
    fun testDetectObviousTriplesC() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val values1 = threeRandomDistinctInts(possibleValues=list).toList()
        val coordinates = threeRandomDistinctInts(possibleValues=list)
        val pos1 = region[coordinates.first]
        val pos2 = region[coordinates.second]
        val pos3 = region[coordinates.third]

        //For values [a,b,c] we get [b,c];[a,c];[a,b]
        possibleValues[pos1] = values1.subtract(listOf(values1[0]).toSet()).toList().toMutableList()
        possibleValues[pos2] = values1.subtract(listOf(values1[1]).toSet()).toList().toMutableList()
        possibleValues[pos3] = values1.subtract(listOf(values1[2]).toSet()).toList().toMutableList()


        val detectedTriples = hakyuu.cleanObviousTriples(possibleValues = possibleValues, region = region)

        assert(detectedTriples.containsAll(listOf(pos1,pos2,pos3)))
    }

    @RepeatedTest(10)
    fun testDeleteHiddenSingles() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val position1 = randomDistinctInt(list)
        val position2 = randomDistinctInt(list)

        val value1 = randomDistinctInt(list) + 1
        val value2 = randomDistinctInt(list) + 1

        possibleValues.withIndex().forEach { (position, values) ->
            if (position != position1) values.remove(value1)
            if (position != position2) values.remove(value2)
        }

        hakyuu.cleanHiddenSingles(
            positionsPerValue = hakyuu.getPositionsPerValues(region, possibleValues),
            possibleValues = possibleValues
        )

        assert(possibleValues[position1].size == 1 && possibleValues[position1].contains(value1))
        assert(possibleValues[position2].size == 1 && possibleValues[position2].contains(value2))
    }


    @RepeatedTest(10)
    fun testDeleteHiddenPairs() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val position1 = randomDistinctInt(list)
        val position2 = randomDistinctInt(list)
        val position3 = randomDistinctInt(list)
        val position4 = randomDistinctInt(list)

        val value1 = randomDistinctInt(list) + 1
        val value2 = randomDistinctInt(list) + 1
        val value3 = randomDistinctInt(list) + 1
        val value4 = randomDistinctInt(list) + 1

        possibleValues.withIndex().forEach { (position, values) ->
            if (position != position1 && position != position2) {
                values.remove(value1)
                values.remove(value2)
            }
            if (position != position3 && position != position4 ) {
                values.remove(value3)
                values.remove(value4)
            }
        }

        hakyuu.cleanHiddenPairs(
            positionsPerValue = hakyuu.getPositionsPerValues(region, possibleValues),
            possibleValues = possibleValues
        )

        assert(possibleValues[position1].size == 2 &&
                possibleValues[position1] == possibleValues[position2] &&
                possibleValues[position1].containsAll(listOf(value1, value2)))

        assert(possibleValues[position3].size == 2 &&
                possibleValues[position3] == possibleValues[position4] &&
                possibleValues[position3].containsAll(listOf(value3, value4)))
    }

    @RepeatedTest(10)
    fun testDeleteHiddenTriples() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val position1 = randomDistinctInt(list)
        val position2 = randomDistinctInt(list)
        val position3 = randomDistinctInt(list)

        val value1 = randomDistinctInt(list) + 1
        val value2 = randomDistinctInt(list) + 1
        val value3 = randomDistinctInt(list) + 1

        possibleValues.withIndex().forEach { (position, values) ->
            if (position != position1 && position != position2 && position != position3) {
                values.remove(value1)
                values.remove(value2)
                values.remove(value3)
            }
        }

        hakyuu.cleanHiddenTriples(
            positionsPerValue = hakyuu.getPositionsPerValues(region, possibleValues),
            possibleValues = possibleValues
        )

        assert(possibleValues[position1].size == 3 &&
                possibleValues[position1] == possibleValues[position2] &&
                possibleValues[position1] == possibleValues[position3] &&
                possibleValues[position1].containsAll(listOf(value1, value2, value3)))
    }


    @Test
    fun testOkBoard() {
        val startTime = System.currentTimeMillis()
        val gameType = Hakyuu.solveBoard(seed = 0, boardToSolve = GameFactory.START_STR, boardRegions = GameFactory.REGION_STR)
        val endTime = System.currentTimeMillis()

        print(gameType.printBoard(gameType.completedBoard))

        println("Num of iterations: ${gameType.iterations}")
        println("Time ${endTime - startTime}")
        println("Score ${gameType.getScoreValue()}")

        assert(gameType.boardMeetsRules() && gameType.iterations == 1)
    }

    private fun median(arr: LongArray, size: Int): Number {
        return if (size % 2 == 0) {
            (arr[size / 2 - 1] + arr[size / 2]) / 2.0
        } else {
            arr[size / 2]
        }
    }

    private fun median(arr: IntArray, size: Int): Number{
        return if (size % 2 == 0) {
            (arr[size / 2 - 1] + arr[size / 2]) / 2.0
        } else {
            arr[size / 2]
        }
    }

    //arr sorted
    private fun mode(arr: IntArray): Number{
        var currentMax = 0
        var currentResult = 0
        var tmpMax = 0
        var tmpResult = 0

        for (i in arr) {
            if (tmpResult != i) {
                if (currentMax < tmpMax) {
                    currentMax = tmpMax
                    currentResult = tmpResult
                }
                tmpMax = 1
                tmpResult = i
            }else {
                tmpMax++
            }
        }

        return currentResult
    }


    @Test
    fun testCreateSeededHakyuuBoard() {
        val input = 11
        val seed = 5598423764L

        val startTime = System.currentTimeMillis()
        val gameType = Hakyuu(numColumns = input, numRows = input, seed = seed)

        val res = gameType.createGame()

        val endTime = System.currentTimeMillis()

        print(gameType.printBoard())

        assert(res) { "Failed: $seed " }

        println("Test with sizes ${input}x$input")
        println("Time ${endTime - startTime}")
        println("Iterations ${gameType.iterations}")

    }


    @ParameterizedTest
    @ValueSource(strings = ["6,46", "7,48", "8,99", "9,44", "10,105", "12,71", "15,1", "17,1"])
    fun testOkJankoBoards(input: String, testInfo: TestInfo) {
        val spl = input.split(',')
        val jankoSize = spl[1].toInt()
        val boardSize = spl[0].toInt()

        val fileAreas = File("src/test/testdata/$boardSize-areas.txt")
        val boardRegions = fileAreas.readText()
            .split(Regex("""Tablero \d+\n"""))
            .drop(1)
            .map {
                it.substring(startIndex = 0, endIndex = it.lastIndexOf('\n')) }

        val fileStartBoards = File("src/test/testdata/$boardSize-startboard.txt")
        val startBoards = fileStartBoards.readText()
            .split(Regex("""Tablero \d+\n"""))
            .drop(1)
            .map { it.substring(startIndex = 0, endIndex = it.lastIndexOf('\n')) }

        val seed = (Math.random()*10000000000).toLong()

        val getGameType = { iteration: Int ->
            Hakyuu.solveBoard(seed = seed, boardToSolve = startBoards[iteration], boardRegions = boardRegions[iteration])
        }

        testHakyuuBoards(numColumns = boardSize, numRows = boardSize, repeat = jankoSize, getGameType = getGameType)
    }

    @ParameterizedTest
    @ValueSource(ints = [3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13])//, 14, 15])
    fun testCreateHakyuuBoards(numColumns: Int, testInfo: TestInfo) {
        val numRows = (Math.random()*11).toInt() + 3 // [3,13]
        val repeat = 100

        val getGameType = { _: Int ->
            val res = Hakyuu(numColumns = numColumns, numRows = numRows, seed = (Math.random()*10000000000).toLong())
            res.createGame()
            res
        }

        testHakyuuBoards(numColumns = numColumns, numRows = numRows, repeat = repeat, getGameType = getGameType)
    }

    private fun testHakyuuBoards(numColumns: Int, numRows: Int, repeat: Int, getGameType: (Int) -> Hakyuu) {
        val boards = Array(size = repeat) { "" }
        val scores = IntArray(size = repeat)
        val iterations = IntArray(size = repeat)
        val times = LongArray(size = repeat)
        val seeds = LongArray(size = repeat)
        val regionSizes = mutableListOf<IntArray>()

        repeat(repeat) { iteration ->
            val startTime = System.currentTimeMillis()
            val gameType = getGameType(iteration)
            val endTime = System.currentTimeMillis()

            assert(gameType.boardMeetsRules()) { "$iteration failed seed: ${gameType.seed} " }

            boards[iteration] = gameType.printBoard()
            scores[iteration] = gameType.getScoreValue()
            iterations[iteration] = gameType.iterations
            times[iteration] = endTime - startTime
            seeds[iteration] = gameType.seed
            gameType.getRegionStatData().forEachIndexed{ regionSize, value ->
                if (regionSizes.size == regionSize) {
                    val arr = IntArray(size = repeat)
                    arr[iteration] = value
                    regionSizes.add(arr)
                }
                else regionSizes[regionSize][iteration] = value
            }
        }

        printBoards(numColumns = numColumns, numRows = numRows, repeat = repeat, boards = boards)
        printBoardsData(iterations = iterations, scores = scores, times = times, seeds = seeds, regionSizes = regionSizes)
    }


    private fun printBoards(numColumns: Int, numRows: Int, repeat: Int, boards: Array<String>) {
        println("<h1>Test with sizes ${numColumns}x$numRows</h1>")

        print("""<button onclick="var el = document.getElementById('boards-$numColumns');el.style.display = (el.style.display == 'none') ? 'flex' : 'none'; ">Show boards</button> """)
        print("""<div id="boards-$numColumns"style="display:none; flex-wrap: wrap;">""")

        repeat(repeat) { iteration ->
            print("""<div style="margin: 10px;">""")
            print("<h1>Board ${iteration+1}</h1>")
            print(boards[iteration])
            print("</div>")
        }

        print("</div>")
    }

    private fun printBoardsData(
        iterations: IntArray,
        times: LongArray,
        scores: IntArray,
        seeds: LongArray,
        regionSizes:  List<IntArray>
    ){
        val numBoards = iterations.size
        require(times.size == numBoards && scores.size == numBoards && seeds.size == numBoards && regionSizes[0].size == numBoards) { "Incorrect sizes provided" }

        print("""<br><br><div style="display: flex; font-size: large; justify-content: space-evenly;">""") // A

        print(getStatisticsStr(iterations, times, scores, seeds, regionSizes, numBoards))

        print("<div>") // B

        print("<div>") // C
        print(getRegionStatsStr(regionSizes, numBoards))
        print("</div>") // C

        print("<div style='margin-top: 50px;'>") // D
        print(getItTimesScoresStatsStr(iterations, times, scores, numBoards))
        print("</div>") // D

        print("</div>") // B

        print("</div>") // A
    }

    private fun getStatisticsStr(iterations: IntArray, times: LongArray, scores: IntArray, seeds: LongArray, regionSizes: List<IntArray>, numBoards: Int): String {
        var htmlCode = """<table style="border-spacing: 20px 0;justify-content: start;display: flex;"><tbody>"""
        htmlCode += """<tr><th>Test</th><th>Seed</th><th>Num Iterations</th><th>Time (ms)</th><th>Scores</th><th>Region Sizes</th></tr>"""
        (0..<numBoards).forEach {
            val sizes = regionSizes.joinToString(separator = "") { arr -> "<p style='margin: 0'>" + arr[it] + "</p>" }
            htmlCode += """<tr><td>${it + 1}</td><td>${seeds[it]}</td><td>${iterations[it]}</td><td>${times[it]}</td><td>${scores[it]}</td><td style="display: flex;">${sizes}</td></tr>"""
        }
        htmlCode += "</tbody></table>"

        return htmlCode
    }

    private fun getRegionStatsStr(regionSizes: List<IntArray>, numBoards: Int): String {
        return """<table style="border-spacing: 20px 0;"><tbody>""" +
                """<tr><th>Region size</th><th>Mode</th><th>Mean</th><th>Median</th></tr>""" +
                getRegionStatsStr2(regionSizes, numBoards) +
                "</tbody></table>"
    }

    private fun getRegionStatsStr2(regionSizes: List<IntArray>, numBoards: Int): String {
        return regionSizes.mapIndexed { size, arr ->
            arr.sort()
            """<tr><td>${size+1}</td><td>${mode(arr)}</td><td>${arr.average()}</td><td>${median(arr, numBoards)}</td></tr>"""
        }.joinToString {
            it
        }
    }

    private fun getItTimesScoresStatsStr(iterations: IntArray, times: LongArray, scores: IntArray, numBoards: Int): String {
        return """<table style="border-spacing: 20px 0;"><tbody>""" +
                """<tr><th></th><th>Mid Range</th><th>Mean</th><th>Median</th><th>Max</th><th>Min</th><th>Total</th></tr>""" +
                getIndividualStats(label = "Iterations", arr = iterations, numBoards = numBoards) +
                getIndividualStats(label = "Times (ms)", arr = times, numBoards = numBoards) +
                getIndividualStats(label = "Scores", arr = scores, numBoards = numBoards) +
                "</tbody></table>"
    }

    private fun getIndividualStats(label: String, arr: LongArray, numBoards: Int): String {
        arr.sort()
        return """<tr><td><b>$label</b></td><td>${(arr.first()+arr.last()) / 2}</td><td>${arr.average()}</td><td>${median(arr, numBoards)}</td><td>${arr.max()}</td><td>${arr.min()}</td><td>${arr.sum()}</td></tr>"""
    }

    private fun getIndividualStats(label: String, arr: IntArray, numBoards: Int): String {
        arr.sort()
        return """<tr><td><b>$label</b></td><td>${(arr.first()+arr.last()) / 2}</td><td>${arr.average()}</td><td>${median(arr, numBoards)}</td><td>${arr.max()}</td><td>${arr.min()}</td><td>${arr.sum()}</td></tr>"""
    }

}