package com.example.tfg.games.hakyuu

import com.example.tfg.common.GameFactory
import com.example.tfg.common.utils.CustomTestWatcher
import com.example.tfg.common.utils.Utils
import com.example.tfg.games.common.Difficulty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.math.max
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
        val getGameType = {
            Hakyuu.solveBoard(seed = 0, boardToSolve = GameFactory.START_STR, boardRegions = GameFactory.REGION_STR)
        }

        testHakyuuBoard(getGameType)
    }

    @Test
    fun testCreateSeededHakyuuBoardPrintingEachBoardState() {
        val size = 6
        val seed = 234234242342L

        val gameType = Hakyuu.create(
            numColumns = size,
            numRows = size,
            seed = seed,
            difficulty = Difficulty.EXPERT,
            printEachBoardState = true
        )

        // Input this in the console in the browser to make the button work
/*
        const tables = document.getElementsByTagName('table');

        for (var i = 0; i < tables.length; i++) {
            tables[i].style = 'font-size: large; border-collapse: collapse; margin: 20px auto; display: none;'
        }
        tables[0].style = 'font-size: large; border-collapse: collapse; margin: 20px auto; display: revert'

        function toggleTables() {
            console.log('next')
            for (var i = 0; i < tables.length; i++) {
                const table = tables[i];
                if(table.style.display === 'revert') {
                    table.style = 'font-size: large; border-collapse: collapse; margin: 20px auto; display: none;'
                    if((i+1) < tables.length){
                        tables[i+1].style = 'font-size: large; border-collapse: collapse; margin: 20px auto; display: revert;'
                    } else {
                        tables[0].style = 'font-size: large; border-collapse: collapse; margin: 20px auto; display: revert;'
                    }
                    break;
                }
            }
        }

        function showEndTable() {
            console.log('end')
            for (var i = 0; i < tables.length; i++) {
                const table = tables[i];
                if(table.style.display === 'revert') {
                    table.style = 'font-size: large; border-collapse: collapse; margin: 20px auto; display: none;'
                    break;
                }
            }
            tables[tables.length - 1].style = 'font-size: large; border-collapse: collapse; margin: 20px auto; display: revert;'
        }
 */

        print("""<button onclick="toggleTables()">Next</button> """.trimIndent())
        print("""<button onclick="showEndTable()">End</button> """.trimIndent())

        assert(gameType.boardMeetsRulesPrintingInfo())
    }


    @Test
    fun testCreateSeededHakyuuBoard() {
        val size = 7
        val seed = 234234242342L

        val getGameType = {
            Hakyuu.create(numColumns = size, numRows = size, seed = seed, difficulty = Difficulty.EXPERT, printEachBoardState = false)
        }

        testHakyuuBoard(getGameType, print = false)
    }

    private fun testHakyuuBoard(
        getGameType: () -> Hakyuu,
        getTest: (Hakyuu) -> Boolean = { gameType: Hakyuu -> gameType.boardMeetsRulesPrintingInfo() && gameType.score.get() != 0 },
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
        println("Iterations: ${gameType.iterations}")
        println("Score: ${gameType.score.get()}")

        assert(getTest(gameType)) { "Failed with seed: ${gameType.seed} " }
    }

    private fun loadJanko(fileName: String): List<String> {
        val file = File("src/test/testdata/$fileName")
        return file.readText()
            .split(Regex("""Tablero \d+\n"""))
            .drop(1)
            .map { it.substring(startIndex = 0, endIndex = it.lastIndexOf('\n')) }
    }

    private fun loadJankoRegions(boardSize: Int): List<String> {
        return loadJanko("$boardSize-areas.txt")
    }

    private fun loadJankoBoards(boardSize: Int): List<String> {
        return loadJanko("$boardSize-startboard.txt")
    }

    private fun loadJankoSolutions(boardSize: Int): List<String> {
        return loadJanko("$boardSize-solution.txt")
    }

    @Test
    fun testOkJankoBoard() {
        val seed = 5598423764L
        val boardSize = 6
        val boardNumber = 44

        val boardRegions = loadJankoRegions(boardSize)[boardNumber - 1]
        val startBoard = loadJankoBoards(boardSize)[boardNumber - 1]
        val solution = loadJankoSolutions(boardSize)[boardNumber - 1]

        val getGameType = {
            Hakyuu.solveBoard(seed = seed, boardToSolve = startBoard, boardRegions = boardRegions)
        }

        val getTest = { gameType: Hakyuu ->
            val board = gameType.printCompletedBoard()
            val correctBoard = board == solution
            val numBruteForces = (gameType.score as HakyuuScore).getBruteForceValue()
            val zeroBruteForce = numBruteForces == 0

            if (!correctBoard) println("Incorrect board:\n$board")
            if (!zeroBruteForce) println("Took one or more brute forces: $numBruteForces")

            correctBoard //&& oneIteration
        }

        testHakyuuBoard(getGameType, getTest)
    }

    @ParameterizedTest
    @ValueSource(strings = ["6,46", "7,48", "8,99", "9,44", "10,105", "12,71", "15,1", "17,1"])
    fun testOkJankoBoards(input: String, testInfo: TestInfo) {
        val spl = input.split(',')
        val jankoSize = spl[1].toInt()
        val boardSize = spl[0].toInt()

        val boardRegions = loadJankoRegions(boardSize)
        val startBoards = loadJankoBoards(boardSize)
        val solutions = loadJankoSolutions(boardSize)

        val seed = (Math.random()*10000000000).toLong()

        val getGameType = { iteration: Int ->
            Hakyuu.solveBoard(seed = seed, boardToSolve = startBoards[iteration], boardRegions = boardRegions[iteration])
        }

        val getTest = { gameType: Hakyuu, iteration: Int ->
            val board = gameType.printCompletedBoard()
            val correctBoard = board == solutions[iteration]
            val numBruteForces = (gameType.score as HakyuuScore).getBruteForceValue()
            val zeroBruteForce = numBruteForces == 0

            if (!correctBoard) println("Incorrect board:\n$board")
            if (!zeroBruteForce) println("Took one or more brute forces: $numBruteForces")

            correctBoard //&& oneIteration
        }


        testHakyuuBoards(
            numColumns = boardSize,
            numRows = boardSize,
            repeat = jankoSize,
            getGameType = getGameType,
            getTest = getTest,
            printBoards = false,
            //printCompleted = true
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13])//, 14, 15])
    fun testCreateHakyuuBoards(numColumns: Int, testInfo: TestInfo) {
        val repeat = 100

        val getGameType = { _: Int ->
            val seed = (Math.random()*10000000000).toLong()
            val res = Hakyuu.create(numColumns = numColumns, numRows = numColumns, seed = seed, difficulty = Difficulty.MASTER, printEachBoardState = false)
            res
        }
        val getTest = { gameType: Hakyuu, _: Int ->
            val boardMeetsRules = gameType.boardMeetsRulesPrintingInfo()
            val scoreIsNotZero = gameType.score.get() != 0
            if (!boardMeetsRules) println("Incorrect board:\n${gameType.printCompletedBoard()}")
            if (!scoreIsNotZero) println("Score is 0")

            boardMeetsRules && scoreIsNotZero
        }

        testHakyuuBoards(
            numColumns = numColumns,
            numRows = numColumns,
            repeat = repeat,
            getGameType = getGameType,
            getTest = getTest,
            printBoards = false,
            summarizeStats = true
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13])//, 14, 15])
    fun testCreateHakyuuBoardsUnevenSize(numColumns: Int, testInfo: TestInfo) {
        val numRows = (Math.random()*11).toInt() + 3 // Between: [3,13]
        val repeat = 100

        val getGameType = { _: Int ->
            val res = Hakyuu.create(numColumns = numColumns, numRows = numRows, seed = (Math.random()*10000000000).toLong(), difficulty = Difficulty.MASTER, printEachBoardState = false)
            res
        }
        val getTest = { gameType: Hakyuu, _: Int ->
            val boardMeetsRules = gameType.boardMeetsRulesPrintingInfo()
            val scoreIsNotZero = gameType.score.get() != 0
            if (!boardMeetsRules) println("Incorrect board:\n${gameType.printCompletedBoard()}")
            if (!scoreIsNotZero) println("Score is 0")

            boardMeetsRules && scoreIsNotZero
        }

        testHakyuuBoards(
            numColumns = numColumns,
            numRows = numColumns,
            repeat = repeat,
            getGameType = getGameType,
            getTest = getTest
        )
    }

    private fun testHakyuuBoards(
        numColumns: Int,
        numRows: Int,
        repeat: Int,
        getGameType: (Int) -> Hakyuu,
        getTest: (Hakyuu, Int) -> Boolean = { gameType: Hakyuu, _: Int -> gameType.boardMeetsRulesPrintingInfo()},
        printBoards: Boolean = true,
        printCompleted: Boolean = false,
        summarizeStats: Boolean = false
    ) {
        val boards = Array(size = repeat) { "" }
        val scores = IntArray(size = repeat)
        val iterations = IntArray(size = repeat)
        val times = LongArray(size = repeat)
        val seeds = LongArray(size = repeat)
        val regionSizes = mutableListOf<IntArray>()
        val failed = mutableListOf<Int>()

        repeat(repeat) { iteration ->
            val startTime = System.currentTimeMillis()
            val gameType = getGameType(iteration)
            val endTime = System.currentTimeMillis()

            val res = getTest(gameType, iteration)
            if (!res) failed.add(iteration)

            boards[iteration] = if (printCompleted) gameType.printCompletedBoardHTML() else gameType.printStartBoardHTML()
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

        if (printBoards) printBoards(numColumns = numColumns, numRows = numRows, repeat = repeat, boards = boards)
        printBoardsData(iterations = iterations, scores = scores, times = times, seeds = seeds, regionSizes = regionSizes, summarizeStats = summarizeStats)

        assert(failed.isEmpty()) {
            failed.joinToString(separator = "") { index ->
                    "\n${index + 1} failed seed: ${seeds[index]} "
                }
        }
    }


    private fun printBoards(numColumns: Int, numRows: Int, repeat: Int, boards: Array<String>) {
        println("<h1>Test with sizes ${numColumns}x$numRows</h1>")
        println("<h1>${boards.size} boards</h1>")

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

    private fun printBoardsData(iterations: IntArray, times: LongArray, scores: IntArray, seeds: LongArray, regionSizes:  List<IntArray>, summarizeStats: Boolean){
        val numBoards = iterations.size
        require(times.size == numBoards && scores.size == numBoards && seeds.size == numBoards && regionSizes[0].size == numBoards) { "Incorrect sizes provided" }

        print("""<br><br><div style="display: flex; font-size: large; justify-content: space-evenly;">""") // A

        if (summarizeStats){
            val timesSorted = times.sortedArray()
            val medianDoubled = Utils.percentile(arr = timesSorted).toInt().let { if (it == 0) 2 else it * 2 }
            val percentile = Utils.percentile(arr = timesSorted, percentile = 90).toInt()
            val last100 = timesSorted.reversed().take(100).last().toInt()
            val limit = max(max(percentile, medianDoubled), last100)
            val indices = times.withIndex().filter { (_, time) -> time > limit }.map { (index, _) -> index}.toIntArray()
            print(getStatisticsStr(
                indices = indices,
                iterations = Utils.filterIndices(iterations, indices),
                times = Utils.filterIndices(times, indices),
                scores = Utils.filterIndices(scores, indices),
                seeds = Utils.filterIndices(seeds, indices),
                regionSizes = Utils.filterIndices(regionSizes, indices),
                limit = limit
            ))
        }
        else {
            print(getStatisticsStr(iterations.indices.toList().toIntArray(), iterations, times, scores, seeds, regionSizes))
        }
        print("<div>") // B

        print("<div>") // C
        print(getRegionStatsStr(regionSizes))
        print("</div>") // C

        print("<div style='margin-top: 50px;'>") // D
        print(getItTimesScoresStatsStr(iterations, times, scores))
        print("</div>") // D

        print("</div>") // B

        print("</div>") // A
    }

    private fun getStatisticsStr(indices: IntArray, iterations: IntArray, times: LongArray, scores: IntArray, seeds: LongArray, regionSizes: List<IntArray>, limit: Int = 0): String {
        var htmlCode = "<div>"
        if (limit > 0) htmlCode += "<tr><h3>Showing boards with time higher than: $limit ms</h3></tr>"
        htmlCode += """<table style="border-spacing: 20px 0;justify-content: start;display: flex;"><tbody>"""
        val sizesLabel = regionSizes.indices.joinToString(separator = "") { "<th>${it+1}</th>" }
        htmlCode += """<tr><th>Test</th><th>Seed</th><th>Num Iterations</th><th>Time (ms)</th><th>Scores</th>$sizesLabel<th><- Region Sizes</th></tr>"""
        indices.indices.forEach {
            val sizes = regionSizes.joinToString(separator = "") { arr -> "<td>${arr[it]}</td>" }
            htmlCode += """<tr><td>${indices[it] + 1}</td><td>${seeds[it]}</td><td>${iterations[it]}</td><td>${times[it]}</td><td>${scores[it]}</td>${sizes}</tr>"""
        }
        htmlCode += "</tbody></table></div>"

        return htmlCode
    }

    private fun getRegionStatsStr(regionSizes: List<IntArray>): String {
        return """<table style="border-spacing: 20px 0;"><tbody>""" +
                """<tr><th>Region size</th><th>Mode</th><th>Mean</th><th>Median</th></tr>""" +
                getRegionStatsStr2(regionSizes) +
                "</tbody></table>"
    }

    private fun getRegionStatsStr2(regionSizes: List<IntArray>): String {
        return regionSizes.mapIndexed { size, arr ->
            arr.sort()
            """<tr><td>${size+1}</td><td>${Utils.mode(arr)}</td><td>${arr.average()}</td><td>${Utils.percentile(arr)}</td></tr>"""
        }.joinToString {
            it
        }
    }

    private fun getItTimesScoresStatsStr(iterations: IntArray, times: LongArray, scores: IntArray): String {
        return """<table style="border-spacing: 20px 0;"><tbody>""" +
                """<tr><th></th><th>Mid Range</th><th>Mean</th><th>Median</th><th>Max</th><th>Min</th><th>Total</th></tr>""" +
                getIndividualStats(label = "Iterations", arr = iterations) +
                getIndividualStats(label = "Times (ms)", arr = times) +
                getIndividualStats(label = "Scores", arr = scores) +
                "</tbody></table>"
    }

    @Suppress("SameParameterValue")
    private fun getIndividualStats(label: String, arr: LongArray): String {
        arr.sort()
        return """<tr><td><b>$label</b></td><td>${(arr.first()+arr.last()) / 2}</td><td>${arr.average()}</td><td>${Utils.percentile(arr)}</td><td>${arr.max()}</td><td>${arr.min()}</td><td>${arr.sum()}</td></tr>"""
    }

    private fun getIndividualStats(label: String, arr: IntArray): String {
        arr.sort()
        return """<tr><td><b>$label</b></td><td>${(arr.first()+arr.last()) / 2}</td><td>${arr.average()}</td><td>${Utils.percentile(arr)}</td><td>${arr.max()}</td><td>${arr.min()}</td><td>${arr.sum()}</td></tr>"""
    }

}