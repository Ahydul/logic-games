package com.example.tfg.games.hakyuu

import com.example.tfg.common.utils.Coordinate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class HakyuuUnitTest {
    lateinit var hakyuu: Hakyuu
    lateinit var region : List<Coordinate>
    lateinit var possibleValues: MutableMap<Coordinate, List<Int>>
    val random = Random((Math.random()*10000000000).toLong())

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

        possibleValues = region.associateWith { listOf(1,2,3,4,5,6,7,8,9) }.toMutableMap()
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
        possibleValues[coord1] = values1
        possibleValues[coord2] = values1

        val values2 = twoRandomDistinctInts(possibleValues=list).toList()
        val coordinates2 = twoRandomDistinctInts(possibleValues=list)
        val coord3 = region[coordinates2.first]
        val coord4 = region[coordinates2.second]
        possibleValues[coord3] = values2
        possibleValues[coord4] = values2

        val detectedPairs = hakyuu.detectObviousPairs(possibleValues = possibleValues, region = region)

        val bool = detectedPairs[0].toList().containsAll(arrayOf(coord1,coord2).toList()) || detectedPairs[0].toList().containsAll(arrayOf(coord3,coord4).toList())
                && detectedPairs[1].toList().containsAll(arrayOf(coord1,coord2).toList()) || detectedPairs[1].toList().containsAll(arrayOf(coord3,coord4).toList())

        assert(bool)
    }

    private fun threeRandomDistinctInts(possibleValues: MutableList<Int>): Triple<Int,Int,Int> {
        return Triple(randomDistinctInt(possibleValues), randomDistinctInt(possibleValues), randomDistinctInt(possibleValues))
    }


    @RepeatedTest(10)
    fun testDetectObviousTriples() {
        val maxNumber = region.size - 1
        val list = (0..maxNumber).toMutableList()

        val values1 = threeRandomDistinctInts(possibleValues=list).toList()
        val coordinates = threeRandomDistinctInts(possibleValues=list)
        val coord1 = region[coordinates.first]
        val coord2 = region[coordinates.second]
        val coord3 = region[coordinates.third]
        possibleValues[coord1] = values1
        possibleValues[coord2] = values1
        possibleValues[coord3] = values1


        val detectedTriples = hakyuu.detectObviousTriples(possibleValues = possibleValues, region = region)

        assert(detectedTriples[0].toList().containsAll(arrayOf(coord1,coord2).toList()))
    }


}