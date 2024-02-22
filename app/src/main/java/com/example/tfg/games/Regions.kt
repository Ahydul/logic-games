package com.example.tfg.games

import com.example.tfg.common.utils.Coordinate
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
        return max(tempRegions.keys.maxOrNull() ?: 0, definitiveRegions.keys.max())
    }

    private fun randomPropagationNumber(): Int {
        return random.nextInt(maxRegionSize)
    }

    private fun tryToMergeRegions(): Boolean {

        return true
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
            //val result = tryToMergeRegions()
            //if (result) propagateRandomRegion(numPropagations)
            addNewSeedToRegions()
        }

        val regionID = tempRegions.keys.random(random)
        val positions = tempRegions[regionID]!!

        for (a in 1..numPropagations){
            val result = propagateRegionOnce(positions = positions)
            //We remove regions that cant be propagated and add them to the definitive regions
            if (!result){
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

        return definitiveRegions
    }

    private fun reset() {
        tempRegions.clear()
        remaining.clear()
        remaining.addAll(resetRemaining())
    }






    //TODO
    fun divideRegionsOptionA(numColumns: Int, numRows: Int, maxNumberPerRegions: Int): IntArray {
        reset()

        val regionNumber = randomRegionSizes(
            maxRegionSize = maxRegionSize,
            maxNumberPerRegions = maxNumberPerRegions,
            numPositions = numPositions
        )

        var regionId = 0
        while(remaining.isNotEmpty()){
            val position = random.nextInt(maxRegionSize)
            val propagation = regionNumber[random.nextInt(maxRegionSize)]
            //propagateRegion(regionId, regions, position, propagation)
            regionId += 1
        }

        return IntArray(maxRegionSize){0}
    }

    private fun randomRegionSizes(maxRegionSize: Int, numPositions: Int, maxNumberPerRegions: Int = maxRegionSize + 2): IntArray {
        val result = IntArray(maxRegionSize){0}
        var x = 0
        while (x != numPositions) {
            val index = Random.nextInt(maxRegionSize) + 1
            if (result[index-1] < maxNumberPerRegions && (x + index) <= numPositions)
                result[index-1] += 1
            else continue

            x += index
        }

        return result
    }
}