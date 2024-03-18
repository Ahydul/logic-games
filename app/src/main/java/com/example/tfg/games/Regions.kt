package com.example.tfg.games

import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.Direction
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Regions(
    private val numColumns: Int,
    private val numRows: Int,
    private val random: Random,
) {
    private val numPositions = numColumns * numRows
    private val definitiveRegions = mutableMapOf<Int, MutableList<Coordinate>>()
    private val tempRegions = mutableMapOf<Int, MutableList<Coordinate>>()
    private val maxRegionSize = min(numColumns, numRows) - 2
    private val remaining = resetRemaining()

    private fun resetRemaining(): MutableList<Coordinate> {
        return (0..< numPositions)
            .map { Coordinate.fromIndex(it, numRows, numColumns) }
            .shuffled(random)
            .toMutableList()
    }

    private fun getLastRegionID(): Int {
        return max(tempRegions.keys.maxOrNull() ?: -1, definitiveRegions.keys.maxOrNull() ?: -1)
    }

    //Get random number [1,maxRegionSize-1]
    private fun randomPropagationNumber(): Int {
        //return random.nextInt(maxRegionSize - 1) + 1
        return ((maxRegionSize - 1) * Curves.moreProgressively(random.nextDouble(1.0))).toInt() + 1
    }


    private fun mergeSomeRegionOfSizeOne() {
        val regionsWithOnlyOne = definitiveRegions
            .filterValues { it.size==1 }
            .mapValues { it.value[0] }

        for (region in regionsWithOnlyOne) {
            //Check we haven't deleted it already
            if (!definitiveRegions.containsKey(region.key)) continue

            val position = region.value
            //We try to merge to random adjacent regions that have only one position to 'region'
            for (direction in Direction.entries.shuffled(random)) {
                val coordinate = position.move(direction = direction, numRows = numRows, numColumns = numColumns)
                val key = definitiveRegions.filter { it.value.size == 1 && it.value[0] == coordinate }

                //Must be just one k
                for (k in key) {
                    //if (definitiveRegions[region.key]!!.size == maxRegionSize) continue
                    definitiveRegions[region.key]!!.add(k.value[0])
                    definitiveRegions.remove(k.key)
                }
            }
        }
    }

    private fun addNewSeedToRegions() {
        val position = remaining.last()
        val nextRegionID = getLastRegionID() + 1
        tempRegions[nextRegionID] = mutableListOf(position)
        remaining.removeLast()
    }

    // Propagate random region. If it fails, remove that region from regions and add it to definitiveRegions
    private fun propagateRandomRegion(numPropagations: Int = 1) {
        if (tempRegions.isEmpty()){
            addNewSeedToRegions()
        }

        val regionID = tempRegions.keys.random(random)
        val positions = tempRegions[regionID]!!

        for (a in 1..numPropagations){
            val result = propagateRegionOnce(positions = positions)
            // We remove regions that cant be propagated or propagated enough times
            // and add them to the definitive regions
            if (!result || tempRegions[regionID]!!.size > numPropagations){
                definitiveRegions[regionID] = positions
                tempRegions.remove(regionID)
                return
            }
        }
    }

    // Try to propagate the region in a random direction from a random position of the region
    // Try each possible combination and return false if the region can't be propagated
    private fun propagateRegionOnce(positions: MutableList<Coordinate>): Boolean {
        //Region can't be propagated anymore
        if (positions.size == maxRegionSize) return false

        for (position in positions.shuffled(random)) {
            for (direction in Direction.entries.shuffled(random)) {
                val result = tryPropagate(
                    propagation = position.move(direction = direction, numRows = numRows, numColumns = numColumns),
                    positions = positions,
                )
                if (result) return true
            }
        }
        //Region can't be propagated anymore
        return false
    }

    private fun tryPropagate(propagation: Coordinate?, positions: MutableList<Coordinate>): Boolean {
        if (propagation != null && remaining.contains(propagation)){
            positions.add(propagation)
            remaining.remove(propagation)
            return true
        }
        return false
    }

    fun divideRegionsOptionA(): Map<Int, List<Coordinate>> {
        reset()

        // Propagate regions until there are no remaining positions without a regionID
        while (remaining.isNotEmpty()) {
            propagateRandomRegion(numPropagations = randomPropagationNumber())
        }

        definitiveRegions.putAll(tempRegions)

        return definitiveRegions
    }

    fun divideRegionsOptionB(minNumberOfRegions: Int): Map<Int, List<Coordinate>> {
        require(minNumberOfRegions in 1..<numPositions) { "$minNumberOfRegions must be less than the $numPositions" }
        reset()

        //Initialize temp regions
        for (region in 0..< minNumberOfRegions) {
            val position = remaining.last()
            tempRegions[region] = mutableListOf(position)
            remaining.removeLast()
        }

        // Propagate regions until there are no remaining positions without a regionID
        while (remaining.isNotEmpty()) {
            propagateRandomRegion(numPropagations = randomPropagationNumber())
        }

        definitiveRegions.putAll(tempRegions)
        //mergeSomeRegionOfSizeOne()

        return definitiveRegions
    }

    fun divideRegionsOptionC(): Map<Int, List<Coordinate>> {
        reset()

        val propagations = randomRegionSizes()

        // Propagate regions until there are no remaining positions without a regionID
        while (propagations.isNotEmpty() && remaining.isNotEmpty()) {
            propagateRandomRegion(numPropagations = propagations.last())
            propagations.removeLast()
        }

        definitiveRegions.putAll(tempRegions)

        return definitiveRegions
    }


    private fun reset() {
        tempRegions.clear()
        remaining.clear()
        remaining.addAll(resetRemaining())
    }


    private fun randomRegionSizes(maxNumberPerRegions: Int = maxRegionSize + 2): MutableList<Int> {
        val regions = IntArray(maxRegionSize){ 0 }
        val result = mutableListOf<Int>()
        var x = 0
        while (x != numPositions) {
            val index = random.nextInt(maxRegionSize) + 1
            if (regions[index-1] < maxNumberPerRegions && (x + index) <= numPositions) {
                regions[index - 1] += 1
                result.add(index)
            }
            else continue
            x += index
        }

        return result
    }

    companion object {
        fun getRegionID(coordinate: Coordinate, regions: Map<Int, List<Coordinate>>): Int {
            return regions.keys.find { regions[it]!!.contains(coordinate) }!!
        }

        fun parseString(str: String): Map<Int, List<Coordinate>> {
            val res = mutableMapOf<Int, List<Coordinate>>()
            val lines = str.replace("[","").replace("]","").split('\n')

            for (line in lines) {
                val spl = line.split(':')
                val coordinates = spl[1].split(", ").map { Coordinate.parseString(it) }
                res[spl[0].toInt()] = coordinates
            }
            return res
        }
    }
}