package com.example.tfg

import com.example.tfg.common.utils.Coordinate
import kotlin.random.Random

fun logisticRandom(maxValue: Int, random: Random, f: (Double) -> Double): Int {
    val x = random.nextDouble(1.0) // generates a random number between 0 (inclusive) and 1 (exclusive)
    val v = f(x)
    return (v * maxValue).toInt()
}

fun test(maxValue: Int, random: Random, f: (Double) -> Double) {
    val m = mutableMapOf<Int,Int>()

    for (v in (0..<maxValue)) {
        m[v] = 0
    }

    repeat(100) {
        val randomValue = logisticRandom(maxValue, random, f)
        if(m.contains(randomValue)){
            m[randomValue] = m[randomValue]!! + 1
        }
        else {//Crash
            m[20000] = m[20000]!! + 1
        }
    }
    println("$m")
}


private fun detectObviousTriples(region: List<Coordinate>, possibleValues: Map<Coordinate, List<Int>>): List<Triple<Coordinate, Coordinate, Coordinate>> {
    require(!region.any { possibleValues.containsKey(it) })
    if (region.size < 4) return emptyList()

    val res = mutableListOf<Triple<Coordinate, Coordinate, Coordinate>>()

    // Obvious triples can only have size 2 or 3
    val filteredRegion = region.filter { possibleValues[it]!!.size == 2 || possibleValues[it]!!.size == 3}

    filteredRegion.forEachIndexed { index, coord1 ->
        // Substract possible values: {coord2 values} - {coord1 values}
        val pene = filteredRegion.drop(index + 1).map { coord2 ->
            Pair(coord2, possibleValues[coord2]!!.subtract(possibleValues[coord1]!!))
        }
        // Find two coordinates whose substracted possible values are the same
        val pito = pene.filter {
            pene.any { it.second == it.second }
        }
        // If it was found add it
        if (pito.size == 2) {
            res.add(Triple(coord1, pito[0].first, pito[1].first))
        }
    }

    return res
}


fun main() {
    val region = listOf(
        Coordinate(0,0),
        Coordinate(0,1),
        Coordinate(0,2), //Pair1
        Coordinate(1,0),
        Coordinate(1,1),
        Coordinate(1,2), //Pair1
        Coordinate(2,0),
        Coordinate(2,1),
        Coordinate(2,2),
    )

    val possibleValues = region.associateWith { listOf(1,2,3,4,5,6,7,8,9) }.toMutableMap()

    possibleValues[region[2]] = listOf(5,6)
    possibleValues[region[5]] = listOf(5,6)

    possibleValues[region[8]] = listOf(2,3)
    possibleValues[region[0]] = listOf(2,3)

    //val detectedPairs = detectObviousPairs(possibleValues = possibleValues, region = region)

}