package com.example.tfg.games.hakyuu

import com.example.tfg.common.Difficulty
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.games.Regions
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
    private lateinit var region : List<Coordinate>
    private lateinit var possibleValues: MutableMap<Coordinate, MutableList<Int>>
    private val random = Random((Math.random()*10000000000).toLong())

    @BeforeEach
    fun setUp() {
        hakyuu = Hakyuu.example()

        region = listOf(
            Coordinate(0,0),
            Coordinate(0,1),
            Coordinate(0,2),
            Coordinate(1,0),
            Coordinate(1,1),
            Coordinate(1,2),
            Coordinate(2,0),
            Coordinate(2,1),
            Coordinate(2,2),
        )

        possibleValues = region.associateWith { mutableListOf(1,2,3,4,5,6,7,8,9) }.toMutableMap()
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
        val coordinates1 = twoRandomDistinctInts(possibleValues=list)
        val coord1 = region[coordinates1.first]
        val coord2 = region[coordinates1.second]
        possibleValues[coord1] = values1.toMutableList()
        possibleValues[coord2] = values1.toMutableList()

        val values2 = twoRandomDistinctInts(possibleValues=list).toList()
        val coordinates2 = twoRandomDistinctInts(possibleValues=list)
        val coord3 = region[coordinates2.first]
        val coord4 = region[coordinates2.second]
        possibleValues[coord3] = values2.toMutableList()
        possibleValues[coord4] = values2.toMutableList()

        val detectedPairs = hakyuu.detectObviousPairs(possibleValues = possibleValues, region = region)

        val bool = detectedPairs[0].toList().containsAll(arrayOf(coord1,coord2).toList()) || detectedPairs[0].toList().containsAll(arrayOf(coord3,coord4).toList())
                && detectedPairs[1].toList().containsAll(arrayOf(coord1,coord2).toList()) || detectedPairs[1].toList().containsAll(arrayOf(coord3,coord4).toList())

        assert(bool)
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
        val coord1 = region[coordinates.first]
        val coord2 = region[coordinates.second]
        val coord3 = region[coordinates.third]
        possibleValues[coord1] = values1.toMutableList()
        possibleValues[coord2] = values1.toMutableList()
        possibleValues[coord3] = values1.toMutableList()


        val detectedTriples = hakyuu.detectObviousTriples(possibleValues = possibleValues, region = region)

        assert(detectedTriples[0].toList().containsAll(arrayOf(coord1,coord2).toList()))
    }

    @RepeatedTest(10)
    fun testDetectObviousTriplesB() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val values1 = threeRandomDistinctInts(possibleValues=list).toList()
        val coordinates = threeRandomDistinctInts(possibleValues=list)
        val coord1 = region[coordinates.first]
        val coord2 = region[coordinates.second]
        val coord3 = region[coordinates.third]
        possibleValues[coord1] = values1.toMutableList()
        possibleValues[coord2] = values1.toMutableList()

        //We delete a random number from values1 for the next values
        possibleValues[coord3] = values1.subtract(listOf(values1.random(random)).toSet()).toList().toMutableList()


        val detectedTriples = hakyuu.detectObviousTriples(possibleValues = possibleValues, region = region)

        assert(detectedTriples[0].toList().containsAll(arrayOf(coord1,coord2).toList()))
    }

    @RepeatedTest(10)
    fun testDetectObviousTriplesC() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val values1 = threeRandomDistinctInts(possibleValues=list).toList()
        val coordinates = threeRandomDistinctInts(possibleValues=list)
        val coord1 = region[coordinates.first]
        val coord2 = region[coordinates.second]
        val coord3 = region[coordinates.third]

        //For values [a,b,c] we get [b,c];[a,c];[a,b]
        possibleValues[coord1] = values1.subtract(listOf(values1[0]).toSet()).toList().toMutableList()
        possibleValues[coord2] = values1.subtract(listOf(values1[1]).toSet()).toList().toMutableList()
        possibleValues[coord3] = values1.subtract(listOf(values1[2]).toSet()).toList().toMutableList()


        val detectedTriples = hakyuu.detectObviousTriples(possibleValues = possibleValues, region = region)

        assert(detectedTriples[0].toList().containsAll(arrayOf(coord1,coord2).toList()))
    }

    @RepeatedTest(10)
    fun testDeleteHiddenSingles() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val coordinate1 = possibleValues.toList()[randomDistinctInt(list)].first
        val coordinate2 = possibleValues.toList()[randomDistinctInt(list)].first

        val value1 = randomDistinctInt(list) + 1
        val value2 = randomDistinctInt(list) + 1

        possibleValues.forEach { (coordinate, values) ->
            if (coordinate != coordinate1) values.remove(value1)
            if (coordinate != coordinate2) values.remove(value2)
        }

        hakyuu.cleanHiddenSingles(
            coordsPerValue = hakyuu.getCoordinatesPerValues(region, possibleValues),
            possibleValues = possibleValues
        )

        assert(possibleValues[coordinate1]!!.size == 1 && possibleValues[coordinate1]!!.contains(value1))
        assert(possibleValues[coordinate2]!!.size == 1 && possibleValues[coordinate2]!!.contains(value2))
    }


    @RepeatedTest(10)
    fun testDeleteHiddenPairs() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val coordinate1 = possibleValues.toList()[randomDistinctInt(list)].first
        val coordinate2 = possibleValues.toList()[randomDistinctInt(list)].first
        val coordinate3 = possibleValues.toList()[randomDistinctInt(list)].first
        val coordinate4 = possibleValues.toList()[randomDistinctInt(list)].first

        val value1 = randomDistinctInt(list) + 1
        val value2 = randomDistinctInt(list) + 1
        val value3 = randomDistinctInt(list) + 1
        val value4 = randomDistinctInt(list) + 1

        possibleValues.forEach { (coordinate, values) ->
            if (coordinate != coordinate1 && coordinate != coordinate2) {
                values.remove(value1)
                values.remove(value2)
            }
            if (coordinate != coordinate3 && coordinate != coordinate4 ) {
                values.remove(value3)
                values.remove(value4)
            }
        }

        hakyuu.cleanHiddenPairs(
            coordsPerValue = hakyuu.getCoordinatesPerValues(region, possibleValues),
            possibleValues = possibleValues
        )

        assert(possibleValues[coordinate1]!!.size == 2 &&
                possibleValues[coordinate1] == possibleValues[coordinate2] &&
                possibleValues[coordinate1]!!.containsAll(listOf(value1, value2)))

        assert(possibleValues[coordinate3]!!.size == 2 &&
                possibleValues[coordinate3] == possibleValues[coordinate4] &&
                possibleValues[coordinate3]!!.containsAll(listOf(value3, value4)))
    }

    @RepeatedTest(10)
    fun testDeleteHiddenTriples() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val coordinate1 = possibleValues.toList()[randomDistinctInt(list)].first
        val coordinate2 = possibleValues.toList()[randomDistinctInt(list)].first
        val coordinate3 = possibleValues.toList()[randomDistinctInt(list)].first

        val value1 = randomDistinctInt(list) + 1
        val value2 = randomDistinctInt(list) + 1
        val value3 = randomDistinctInt(list) + 1

        possibleValues.forEach { (coordinate, values) ->
            if (coordinate != coordinate1 && coordinate != coordinate2 && coordinate != coordinate3) {
                values.remove(value1)
                values.remove(value2)
                values.remove(value3)
            }
        }

        hakyuu.cleanHiddenTriples(
            coordsPerValue = hakyuu.getCoordinatesPerValues(region, possibleValues),
            possibleValues = possibleValues
        )

        assert(possibleValues[coordinate1]!!.size == 3 &&
                possibleValues[coordinate1] == possibleValues[coordinate2] &&
                possibleValues[coordinate1] == possibleValues[coordinate3] &&
                possibleValues[coordinate1]!!.containsAll(listOf(value1, value2, value3)))
    }

    private fun startBoard(): MutableMap<Coordinate, Int> {
        val res = mutableMapOf<Coordinate, Int>()
        val start =
            "- - - 1 - - 2 1\n" +
            "- - 1 - - - 4 -\n" +
            "- - - - 4 - - 1\n" +
            "- - - - 1 - - 3\n" +
            "- - 3 - - 6 - -\n" +
            "- - - - - - 1 -\n" +
            "- 1 - 7 - - 3 -\n" +
            "- 6 - - - 2 - -"
        
        for ((irow,row) in start.lines().withIndex()) {
            for ((icol,col) in row.split(" ").withIndex()) {
                if (col == "-") continue
                val value = col.toInt()

                res[Coordinate(irow,icol)] = value
            }
        }

        return res
    }

    @Test
    fun testOkBoard() {
        val (actualValues, strategiesUsed) = hakyuu.populateValues(startBoard())
        val result = hakyuu.boardMeetsRules(actualValues)

        for (a in hakyuu.boardRegions) {
            println("${a.key}:${a.value}")
        }
        hakyuu.printActualValues(actualValues)

        println("Num of iterations: ${hakyuu.numIterations}")
        println("Strategies used: ${
            strategiesUsed.withIndex().joinToString { (index, value) -> "${Hakyuu.Strategy.values()[index]}: $value" }
        }")
        assert(result)
    }

    @ParameterizedTest
    @ValueSource(ints = [5, 6, 7, 8])
    fun testOkRandomBoard(input: Int, testInfo: TestInfo) {
        val repeat = 100

        val iterations = IntArray(size = repeat)
        val times = LongArray(size = repeat)
        val seeds = LongArray(size = repeat)

        repeat(repeat) {
            val startTime = System.currentTimeMillis()

            val seed = (Math.random()*10000000000).toLong()
            val random = Random(seed)
            val gameType = Hakyuu.create(numColumns = input, numRows = input, random = random)
            val actualValues = gameType.createNewGame(difficulty = Difficulty.EASY)

            val result = gameType.boardMeetsRules(actualValues)

            val endTime = System.currentTimeMillis()

            iterations[it] = gameType.numIterations
            times[it] = endTime - startTime
            seeds[it] = seed

            assert(result) { "$it failed seed: $seed " }
        }
        println("Test with sizes ${input}x$input")

        println("Test\tSeed\t\tNum Iterations\tTime (ms)")

        (0..<repeat).forEach {
            var seed = seeds[it].toString()
            seed += (" ".repeat(10 - seed.length))
            println("${it + 1}\t\t${seeds[it]}\t${iterations[it]}\t\t\t\t${times[it]}")
        }

        iterations.sort()
        println("Iterations Mid range: ${(iterations.first()+iterations.last()) / 2}")
        println("Iterations Mean: ${iterations.average()}")
        println("Iterations Median: ${median(iterations, repeat)}")

        times.sort()
        println("Times Mid range: ${(times.first()+times.last()) / 2}")
        println("Times Mean: ${times.average()}")
        println("Times Median: ${median(times, repeat)}")
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
    fun testOkSeededBoard() {
        val input = 8
        val seed = 3210878768
        val random = Random(seed)

        val startTime = System.currentTimeMillis()
        val gameType = Hakyuu.create(numColumns = input, numRows = input, random = random)
        val actualValues = gameType.createNewGame(difficulty = Difficulty.EASY)

        val result = gameType.boardMeetsRules(actualValues)

        val endTime = System.currentTimeMillis()

        assert(result && gameType.numIterations==1) { "Failed: $seed " }

        println("Test with sizes ${input}x$input")
        println("Time ${endTime - startTime}")
        println("Num of iterations ${gameType.numIterations}")
    }

    @ParameterizedTest
    @ValueSource(strings = ["6,46", "7,48", "8,99", "9,44", "10,105", "12,71", "15,1", "17,1"])
    fun testOkJankoBoards(input: String, testInfo: TestInfo) {
        val spl = input.split(',')
        val jankoSize = spl[1].toInt()
        val size = spl[0].toInt()

        val iterations = IntArray(jankoSize)
        val times = LongArray(jankoSize)
        val strategies = Array(jankoSize){""}

        val file = File("src/test/testdata/$size-areas.txt")

        val txt = file.readText()

        val boards = txt.split("Tablero").drop(1).map {
            it.substring(it.indexOf('\n')+1)
        }

        for ((index, board) in boards.withIndex()) {
            //println("Board $index")

            val gameType = Hakyuu.create(numColumns = size, numRows = size, random = random)
            gameType.boardRegions = Regions.parseString(board.dropLast(1))

            /*
            for (a in gameType.boardRegions) {
                println("${a.key}:${a.value}")
            }
             */

            val startTime = System.currentTimeMillis()
            val (actualValues, strategiesUsed) = gameType.createNewGame2(difficulty = Difficulty.EASY)
            val result = gameType.boardMeetsRules(actualValues)
            val endTime = System.currentTimeMillis()

            //gameType.printActualValues(actualValues)

            assert(result && gameType.numIterations==1) { "Failed: $index " }

            iterations[index] = gameType.numIterations
            times[index] = endTime - startTime
            strategies[index] = strategiesUsed.withIndex().joinToString { (index, value) -> "${Hakyuu.Strategy.values()[index]}: $value" }
        }
        println("Test with sizes ${size}x$size")

        println("Test\tNum Iterations\tTime (ms)\t")

        (0..<jankoSize).forEach {
            println("${it + 1}\t\t${iterations[it]}\t\t\t\t${times[it]}\t\t${strategies[it]}")
        }

        iterations.sort()
        println("Iterations Mid range: ${(iterations.first()+iterations.last()) / 2}")
        println("Iterations Mean: ${iterations.average()}")
        println("Iterations Median: ${median(iterations,jankoSize)}")

        times.sort()
        println("Times Mid range: ${(times.first()+times.last()) / 2}")
        println("Times Mean: ${times.average()}")
        println("Times Median: ${median(times,jankoSize)}")

    }


    @Test
    fun testOkSeededBoardHakyuu2() {
        val input = 15
        val seed = 2640217587
        val msBeforeSkipBoard: Long = 1000 //After msBeforeSkipBoard ms it reset the board that is being generated, in case its too complex
        val random = Random(seed)

        val startTime = System.currentTimeMillis()
        val gameType = Hakyuu2.create(numColumns = input, numRows = input, random = random)

        gameType.createGame(msBeforeSkipBoard)

        val endTime = System.currentTimeMillis()

        print("""<div style="display: flex; font-size: large; justify-content: space-evenly;">""")

        gameType.printBoard()

        assert(gameType.boardMeetsRules()) { "Failed: $seed " }

        println("Test with sizes ${input}x$input")
        println("Time ${endTime - startTime}")
        println("Iterations ${gameType.iterations}")
        println("Resets ${gameType.numBoardReset}")

        print("</div>")

    }


    @ParameterizedTest
    @ValueSource(ints = [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15])
    fun testCreateHakyuuBoards(size: Int, testInfo: TestInfo) {
        println("<h1>Test with sizes ${size}x$size</h1>")
        val repeat = 100
        val msBeforeSkipBoard: Long = 1000 //After msBeforeSkipBoard ms it reset the board that is being generated, in case its too complex

        val iterations = IntArray(size = repeat)
        val times = LongArray(size = repeat)
        val seeds = LongArray(size = repeat)
        val regionSizes = Array(
            size = size,
            init = { IntArray(size = repeat) }
        )
        val numResets = IntArray(size = repeat)

        print("""<button onclick="var el = document.getElementById('boards-$size');el.style.display = (el.style.display == 'none') ? 'flex' : 'none'; ">Show boards</button> """)
        print("""<div id="boards-$size"style="display:none; flex-wrap: wrap;">""")

        repeat(repeat) { iteration ->
            val startTime = System.currentTimeMillis()

            val seed = (Math.random()*10000000000).toLong()
            val random = Random(seed)

            val gameType = Hakyuu2.create(numColumns = size, numRows = size, random = random)

            gameType.createGame(msBeforeSkipBoard)

            val endTime = System.currentTimeMillis()

            print("""<div style="margin: 10px;">""")
            print("<h1>Board ${iteration+1}</h1>")
            gameType.printBoard()
            print("</div>")

            iterations[iteration] = gameType.iterations
            times[iteration] = endTime - startTime
            seeds[iteration] = seed
            gameType.getRegionStatData().forEachIndexed{ regionSize, value ->
                regionSizes[regionSize][iteration] = value
            }
            numResets[iteration] = gameType.numBoardReset

            assert(gameType.boardMeetsRules()) { "$iteration failed seed: $seed " }
        }
        print("</div>")
        print("""<br><br><div style="display: flex; font-size: large; justify-content: space-evenly;">""")

        var htmlCode = """<table style="border-spacing: 20px 0;"><tbody>"""
        htmlCode += """<tr><th>Test</th><th>Seed</th><th>Num Iterations</th><th>Num Resets</th><th>Time (ms)</th><th>Region Sizes</th></tr>"""
        (0..<repeat).forEach {
            val sizes = regionSizes.map { arr -> arr[it] }.joinToString(separator = " ")
            htmlCode += """<tr><td>${it + 1}</td><td>${seeds[it]}</td><td>${iterations[it]}<td>${numResets[it]}</td><td>${times[it]}</td><td>${sizes}</td></tr>"""
        }
        htmlCode += "</tbody></table>"
        print(htmlCode)


        print("<div>")

        var htmlCode2 = """<table style="border-spacing: 20px 0;"><tbody>"""
        htmlCode2 += """<tr><th>Region size</th><th>Mode</th><th>Mean</th><th>Median</th></tr>"""
        regionSizes.forEachIndexed{ size, arr ->
            arr.sort()
            htmlCode2 += """<tr><td>${size+1}</td><td>${mode(arr)}</td><td>${arr.average()}</td><td>${median(arr, repeat)}</td></tr>"""
        }
        htmlCode2 += "</tbody></table>"
        print(htmlCode2)

        print("</div>")


        print("<div>")

        var htmlCode3 = """<table style="border-spacing: 20px 0;"><tbody>"""
        htmlCode3 += """<tr><th></th><th>Mid Range</th><th>Mean</th><th>Median</th><th>Total</th></tr>"""

        iterations.sort()
        htmlCode3 += """<tr><td><b>Iterations</b></td><td>${(iterations.first()+iterations.last()) / 2}</td><td>${iterations.average()}</td><td>${median(iterations, repeat)}</td><td>${iterations.sum()}</td></tr>"""
        numResets.sort()
        htmlCode3 += """<tr><td><b>Resets</b></td><td>${(numResets.first()+numResets.last()) / 2}</td><td>${numResets.average()}</td><td>${median(numResets, repeat)}</td><td>${numResets.sum()}</td></tr>"""
        times.sort()
        htmlCode3 += """<tr><td><b>Times (ms)</b></td><td>${(times.first()+times.last()) / 2}</td><td>${times.average()}</td><td>${median(times, repeat)}</td><td>${times.sum()}</td></tr>"""

        htmlCode3 += "</tbody></table>"
        print(htmlCode3)

        print("</div></div>")

    }

}