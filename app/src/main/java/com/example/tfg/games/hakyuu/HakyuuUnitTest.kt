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


    private fun startBoard(): IntArray {
        val start =
            "- 4 - - 3 1 - -\n" +
            "- - 2 - - 2 - -\n" +
            "- - - - - - - 5\n" +
            "- - - - - - - -\n" +
            "- - - - - 4 - -\n" +
            "3 - - - - - - -\n" +
            "- - 4 - - 6 - -\n" +
            "- - 3 5 - - 6 -"

        return start.replace('\n',' ').split(" ").map { if (it=="-") 0 else it.toInt() }.toIntArray()
    }

    @Test
    fun testOkBoard() {
        val startTime = System.currentTimeMillis()
        val board = startBoard()
        val result = hakyuu.solveBoard(board = board)
        val endTime = System.currentTimeMillis()

        hakyuu.printBoard(board)

        println("Num of iterations: ${hakyuu.iterations}")
        println("Time ${endTime - startTime}")
        println("Score ${hakyuu.getScoreValue()}")

        assert(result)
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

/*
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

        val boards = txt
            .split("Tablero")
            .drop(1)
            .map {
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

 */




    @Test
    fun testCreateSeededHakyuuBoard() {
        val input = 11
        val seed = 5598423764L

        val startTime = System.currentTimeMillis()
        val gameType = Hakyuu(numColumns = input, numRows = input, seed = seed)

        val res = gameType.createGame()

        val endTime = System.currentTimeMillis()

        gameType.printBoard()

        assert(res) { "Failed: $seed " }

        println("Test with sizes ${input}x$input")
        println("Time ${endTime - startTime}")
        println("Iterations ${gameType.iterations}")

    }


    @ParameterizedTest
    @ValueSource(ints = [3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13])//, 14, 15])
    fun testCreateHakyuuBoards(numColumns: Int, testInfo: TestInfo) {
        val numRows = (Math.random()*11).toInt() + 3 // [3,13]
        println("<h1>Test with sizes ${numColumns}x$numRows</h1>")
        val repeat = 100

        val iterations = IntArray(size = repeat)
        val times = LongArray(size = repeat)
        val seeds = LongArray(size = repeat)
        val regionSizes = Array(
            size = max(numColumns,numRows),
            init = { IntArray(size = repeat) }
        )

        print("""<button onclick="var el = document.getElementById('boards-$numColumns');el.style.display = (el.style.display == 'none') ? 'flex' : 'none'; ">Show boards</button> """)
        print("""<div id="boards-$numColumns"style="display:none; flex-wrap: wrap;">""")

        repeat(repeat) { iteration ->
            val startTime = System.currentTimeMillis()

            val seed = (Math.random()*10000000000).toLong()

            val gameType = Hakyuu(numColumns = numColumns, numRows = numRows, seed = seed)

            val res = gameType.createGame()

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

            assert(res) { "$iteration failed seed: $seed " }
        }
        print("</div>")
        print("""<br><br><div style="display: flex; font-size: large; justify-content: space-evenly;">""")

        var htmlCode = """<table style="border-spacing: 20px 0;"><tbody>"""
        htmlCode += """<tr><th>Test</th><th>Seed</th><th>Num Iterations</th><th>Time (ms)</th><th>Region Sizes</th></tr>"""
        (0..<repeat).forEach {
            val sizes = regionSizes.map { arr -> arr[it] }.joinToString(separator = " ")
            htmlCode += """<tr><td>${it + 1}</td><td>${seeds[it]}</td><td>${iterations[it]}</td><td>${times[it]}</td><td>${sizes}</td></tr>"""
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
        htmlCode3 += """<tr><th></th><th>Mid Range</th><th>Mean</th><th>Median</th><th>Max</th><th>Min</th><th>Total</th></tr>"""

        iterations.sort()
        htmlCode3 += """<tr><td><b>Iterations</b></td><td>${(iterations.first()+iterations.last()) / 2}</td><td>${iterations.average()}</td><td>${median(iterations, repeat)}</td><td>${iterations.max()}</td><td>${iterations.min()}</td><td>${iterations.sum()}</td></tr>"""
        times.sort()
        htmlCode3 += """<tr><td><b>Times (ms)</b></td><td>${(times.first()+times.last()) / 2}</td><td>${times.average()}</td><td>${median(times, repeat)}</td><td>${times.max()}</td><td>${times.min()}</td><td>${times.sum()}</td></tr>"""

        htmlCode3 += "</tbody></table>"
        print(htmlCode3)

        print("</div></div>")

    }

}