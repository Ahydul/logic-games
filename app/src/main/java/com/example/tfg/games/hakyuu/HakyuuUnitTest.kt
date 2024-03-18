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

    @Test
    fun testOkBoard() {
        val actualValues = hakyuu.createNewGame(Difficulty.EASY)
        val result = hakyuu.boardMeetsRules(actualValues)

        for (a in hakyuu.boardRegions) {
            println("${a.key}:${a.value}")
        }
        hakyuu.printActualValues(actualValues)

        assert(result)
        println("Num of iterations ${hakyuu.numIterations}")
    }

    private val repeat = 10
    @ParameterizedTest
    @ValueSource(ints = [5, 6, 7, 8])
    fun testOkRandomBoard(input: Int, testInfo: TestInfo) {
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
        println("Iterations Median: ${median(iterations)}")

        times.sort()
        println("Times Mid range: ${(times.first()+times.last()) / 2}")
        println("Times Mean: ${times.average()}")
        println("Times Median: ${median(times)}")
    }

    private fun median(arr: LongArray): Number {
        return if (repeat % 2 == 0) {
            (arr[repeat / 2 - 1] + arr[repeat / 2]) / 2.0
        } else {
            arr[repeat / 2]
        }
    }

    private fun median(arr: IntArray): Number{
        return if (repeat % 2 == 0) {
            (arr[repeat / 2 - 1] + arr[repeat / 2]) / 2.0
        } else {
            arr[repeat / 2]
        }
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

    @Test
    fun testOkJankoBoards() {
        val iterations = IntArray(200)
        val times = LongArray(200)

        val size = 6
        val file = File("src/test/testdata/6-areas.txt")
        val bufferedReader = file.bufferedReader()

        var line = bufferedReader.readLine()
        var index = 1
        while (line!=null && line.equals("Tablero $index")) {
            println("Tablero $index")
            var tableroStr = ""
            line = bufferedReader.readLine()
            while (line != null && !line.contains("Tablero")) {
                tableroStr += line + '\n'
                line = bufferedReader.readLine()
            }
            tableroStr = tableroStr.dropLast(1)
            hakyuu.boardRegions = Regions.parseString(tableroStr)

            for (a in hakyuu.boardRegions) {
                println("${a.key}:${a.value}")
            }
            val startTime = System.currentTimeMillis()
            val actualValues = hakyuu.createNewGame(difficulty = Difficulty.EASY)

            val result = hakyuu.boardMeetsRules(actualValues)

            val endTime = System.currentTimeMillis()

            hakyuu.printActualValues(actualValues)

            assert(result && hakyuu.numIterations==1) { "Failed: $index " }

            iterations[index-1] = hakyuu.numIterations
            times[index-1] = endTime - startTime

            index++
        }
        println("Test with sizes ${size}x$size")

        println("Test\tNum Iterations\tTime (ms)")

        (0..<repeat).forEach {
            println("${it + 1}\t${iterations[it]}\t\t\t\t${times[it]}")
        }

        iterations.sort()
        println("Iterations Mid range: ${(iterations.first()+iterations.last()) / 2}")
        println("Iterations Mean: ${iterations.average()}")
        println("Iterations Median: ${median(iterations)}")

        times.sort()
        println("Times Mid range: ${(times.first()+times.last()) / 2}")
        println("Times Mean: ${times.average()}")
        println("Times Median: ${median(times)}")

    }

}