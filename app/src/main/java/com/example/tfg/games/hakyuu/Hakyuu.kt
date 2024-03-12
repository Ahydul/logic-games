package com.example.tfg.games.hakyuu

import com.example.tfg.common.Difficulty
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Direction
import com.example.tfg.games.GameType
import com.example.tfg.games.Games
import com.example.tfg.games.Regions
import kotlin.random.Random

class Hakyuu private constructor(
    override val type: Games = Games.HAKYUU,
    override val rules: List<String> = listOf(),
    override val url: String = "",
    override val noNotes: Boolean = true,
    override var boardRegions: Map<Int, List<Coordinate>>,

    override val numColumns: Int,
    override val numRows: Int,

    override val random: Random
) : GameType {

    @Suppress("UnstableApiUsage")
    override fun createNewGame(
        difficulty: Difficulty,
    ): Map<Coordinate, Int> {
        val result = mutableMapOf<Coordinate, Int>()

        val possibleValuesPerCoordinate = populatePossibleValues(actualValues = result)
        val success = populateCells(possibleValues = possibleValuesPerCoordinate, actualValues = result)

        if (!success) {
            // Random regions provided can't make a playable game. We repeat the proccess with other regions.
            boardRegions = Regions(numColumns = numColumns, numRows = numRows, random).divideRegionsOptionA()
            return createNewGame(difficulty)
        }

        possibleValuesPerCoordinate.forEach {(c,v) ->
            result.put(c,0)
        }

        return result
    }

    private fun populatePossibleValues(actualValues: MutableMap<Coordinate, Int>): MutableMap<Coordinate,MutableList<Int>> {
        val possibleValues = mutableMapOf<Coordinate, MutableList<Int>>()
        val orderedRegions = boardRegions.toList().sortedBy { it.second.size }.toMap()

        for (region in orderedRegions) {
            val values = (1 .. region.value.size).toList()
            for (coordinate in region.value) {
                if (values.size==1) actualValues.put(coordinate, values[0])
                else {
                    possibleValues.put(coordinate, values.toMutableList())
                }
            }
        }
        return possibleValues
    }


    private fun populateCells(possibleValues: MutableMap<Coordinate, MutableList<Int>>, actualValues: MutableMap<Coordinate, Int>): Boolean {
        while (true)
        {
            // If ended populating return true
            if (boardPopulated(actualValues)) return true

            val possibleValuesUpdated = updatePossibleValues(
                possibleValues = possibleValues,
                actualValues = actualValues
            )

            // If possibleValues didn't update we need to brute force a value
            if (!possibleValuesUpdated){
                val minPossibleValues = possibleValues.minByOrNull { it.value.size }!!

                val newActualValues = actualValues.toMutableMap()
                val newPossibleValues = mutableMapOf<Coordinate,MutableList<Int>>()
                possibleValues.forEach { (c,v) -> newPossibleValues.put(c,v.toMutableList())}

                //Brute force
                for(chosenValue in minPossibleValues.value) {
                    newPossibleValues.remove(minPossibleValues.key)
                    newActualValues[minPossibleValues.key] = chosenValue

                    val res = populateCells(possibleValues = newPossibleValues, actualValues = newActualValues)
                    if (res) {
                        //SET POSSIBLE VALUES
                        possibleValues.clear()
                        possibleValues.putAll(newPossibleValues)
                        //SET ACTUAL VALUES
                        actualValues.putAll(newActualValues)
                        return true
                    }
                }
                return false
            }

            // Update values and check if found a contradiction
            val noContradiction = updateValues(possibleValues = possibleValues, actualValues = actualValues)

            // Found contradiction -> can't populate
            if (!noContradiction) {
                return false
            }
        }
    }

    // Update values that logically only have one possibility
    // Return false if a contradiction was found
    private fun updateValues(possibleValues: MutableMap<Coordinate, MutableList<Int>>, actualValues: MutableMap<Coordinate, Int>): Boolean {
        for (entry in possibleValues.toList())
            when(entry.second.size) {
                0 -> {
                    return false
                }
                1 -> {
                    actualValues.put(entry.first, entry.second.first())
                    possibleValues.remove(entry.first)
                }
                else -> {}
            }
        return true
    }

    private fun getRegionID(coordinate: Coordinate): Int {
        return boardRegions.filter { it.value.contains(coordinate) }.keys.first()
    }

    // Update possible values. Return true if the update changed the possible values.
    internal fun updatePossibleValues(possibleValues: Map<Coordinate, MutableList<Int>>, actualValues: Map<Coordinate, Int>): Boolean {
        var possibleValuesChanged = false
        for (entry in possibleValues) {
            //Remove values that are invalid
            val coordinate = entry.key
            val values = entry.value
            val region = boardRegions[getRegionID(coordinate)]!!

            val valueChanged = values.removeIf { value ->
                !(isValidValueRule2(value = value,  region = region, actualValues = actualValues) &&
                        isValidValueRule3(value = value, coordinate = coordinate, actualValues = actualValues))
            }

            if (valueChanged) possibleValuesChanged = true
        }

        for (r in boardRegions.values) {
            val region = r.filter { actualValues.containsKey(it) }

            var valueChanged = cleanHiddenValues(region = region, possibleValues = possibleValues)

            for ((c1,c2) in detectObviousPairs(region = region, possibleValues = possibleValues)) {
                val values = possibleValues[c1]!!
                possibleValues.filter { (c,_) -> region.contains(c) && c!=c1 && c!=c2 }.forEach { (c,v) ->
                    valueChanged = valueChanged || v.removeAll(values)
                }
            }

            for ((c1,c2,c3) in detectObviousTriples(region = region, possibleValues = possibleValues)) {
                val values = possibleValues[c1]!!
                possibleValues.filter { (c,_) -> region.contains(c) && c!=c1 && c!=c2 && c!=c3 }.forEach { (c,v) ->
                    valueChanged = valueChanged || v.removeAll(values)
                }
            }

            if (valueChanged) possibleValuesChanged = true
        }

        return possibleValuesChanged
    }

    // Delete possible values to reveal hidden singles, pairs and triples.
    // Singles are updated to actualValues
    internal fun cleanHiddenValues(region: List<Coordinate>, possibleValues: Map<Coordinate, MutableList<Int>>): Boolean {
        val coordsPerValue = getCoordinatesPerValues(region = region, possibleValues = possibleValues)

        return cleanHiddenSingles(coordsPerValue = coordsPerValue, possibleValues = possibleValues) ||
            cleanHiddenPairs(coordsPerValue = coordsPerValue, possibleValues = possibleValues) ||
            cleanHiddenTriples(coordsPerValue = coordsPerValue, possibleValues = possibleValues)
    }

    internal fun getCoordinatesPerValues(region: List<Coordinate>, possibleValues: Map<Coordinate, MutableList<Int>>): Array<MutableList<Coordinate>> {
        val coordsPerValue = Array(region.size) { mutableListOf<Coordinate>() }
        possibleValues.filter { region.contains(it.key) }.forEach { (coordinate, values) ->
            values.forEach { value -> coordsPerValue[value - 1].add(coordinate) }
        }
        return coordsPerValue
    }

    // Delete values to reveal hidden singles
    internal fun cleanHiddenSingles(coordsPerValue:  Array<MutableList<Coordinate>>, possibleValues: Map<Coordinate, MutableList<Int>>): Boolean {
        var res = false
        coordsPerValue.withIndex().filter { it.value.size == 1 }
            .forEach { (index, value) ->
                // Remove all the possible values but the hidden single
                val coordinate = value.first()
                val coordActualValue = possibleValues[coordinate]!!
                coordActualValue.clear()
                coordActualValue.add(index + 1)

                res = true
            }
        return res
    }

    // Delete values to reveal hidden pairs
    internal fun cleanHiddenPairs(coordsPerValue:  Array<MutableList<Coordinate>>, possibleValues: Map<Coordinate, MutableList<Int>>): Boolean {
        var res = false

        val filteredPossiblePairs = coordsPerValue.withIndex().filter { it.value.size == 2 }
        var drop = 0
        filteredPossiblePairs.forEach { (index1, value) ->
            drop++
            val otherPair = filteredPossiblePairs.drop(drop).find { (_, value2) ->
                value2 == value
            }

            if (otherPair != null) { //index1, index2 are pairs
                val index2 = otherPair.index
                // Remove from each coordinate the possible values that are not the hidden pairs
                value.forEach {
                        coordinate -> possibleValues[coordinate]!!.removeIf { it != index1+1 && it != index2+1 }
                }
                res = true
            }
        }
        return res
    }

    // Delete values to reveal hidden triples
    internal fun cleanHiddenTriples(coordsPerValue:  Array<MutableList<Coordinate>>, possibleValues: Map<Coordinate, MutableList<Int>>): Boolean {
        var res = false
        val filteredPossibleTriples = coordsPerValue.withIndex().filter { it.value.size == 2 || it.value.size == 3 }
        var drop = 0
        filteredPossibleTriples.forEach { (index1, value) ->
            drop++
            val union = filteredPossibleTriples.drop(drop).map { (i, v) ->
                IndexedValue(index = i, value = v.union(value))
            }

            val otherTriples = union.filter { other ->
                union.any { it.value == other.value && (it.value.size == 3 || it.value.size == 2)  }
            }

            if (otherTriples.size == 2) { //index1, index2 and index3 are triples
                val index2 = otherTriples[0].index
                val index3 = otherTriples[1].index
                // Remove from each coordinate the possible values that are not the hidden triples
                value.forEach {
                        coordinate -> possibleValues[coordinate]!!.removeIf { it != index1+1 && it != index2+1 && it != index3+1 }
                }
                res = true
            }
        }
        return res
    }

    internal fun detectObviousPairs(region: List<Coordinate>, possibleValues: Map<Coordinate, List<Int>>): List<Pair<Coordinate, Coordinate>> {
        val filteredRegions = region.filter { possibleValues.containsKey(it) && possibleValues[it]!!.size == 2 }

        val res = mutableListOf<Pair<Coordinate, Coordinate>>()
        filteredRegions.forEachIndexed { index, coordinate1 ->
            val coordinate2 = filteredRegions.drop(index + 1).find { coord2 -> possibleValues[coord2] == possibleValues[coordinate1] }
            if (coordinate2 != null)
                res.add(Pair(coordinate1, coordinate2))
        }

        return res
    }

    internal fun detectObviousTriples(region: List<Coordinate>, possibleValues: Map<Coordinate, List<Int>>): List<Triple<Coordinate, Coordinate, Coordinate>> {
        val filteredRegions = region
            .filter { possibleValues.containsKey(it) && (possibleValues[it]!!.size == 2 || possibleValues[it]!!.size == 3)}
            .sortedBy { possibleValues[it]!!.size }

        val res = mutableListOf<Triple<Coordinate, Coordinate, Coordinate>>()

        // Obvious triples can only have size 2 or 3

        filteredRegions.forEachIndexed { index, coord1 ->
            // Substract possible values: {coord2 values} - {coord1 values}
            val union = filteredRegions.drop(index + 1).map { coord2 ->
                Pair(coord2, possibleValues[coord2]!!.union(possibleValues[coord1]!!))
            }
            // Find two coordinates whose substracted possible values are the same
            val otherTriples = union.filter { other ->
                union.any { it.second == other.second && (it.second.size == 3 || it.second.size == 2) }
            }
            // If it was found add it
            if (otherTriples.size == 2) {
                res.add(Triple(coord1, otherTriples[0].first, otherTriples[1].first))
            }
        }

        return res
    }

    // Rule 1. All cells must be populated
    private fun boardPopulated(actualValues: Map<Coordinate, Int>): Boolean {
        return actualValues.count() == numColumns*numRows
    }

    // In every area of N fields, every number from the range 1~N must appear exactly once.
    private fun isValidValueRule2(value: Int, region: List<Coordinate>, actualValues: Map<Coordinate, Int>): Boolean {
        for (coordinate in region) {
            if (actualValues.containsKey(coordinate) && actualValues[coordinate] == value)
                return false
        }
        return true
    }

    // If two fields in a row or column contain the same number Z, there must be at least Z fields with different numbers between these two fields.
    private fun isValidValueRule3(value: Int, coordinate: Coordinate, actualValues: Map<Coordinate, Int>): Boolean {
        val errorNotFound = Direction.values().all { direction: Direction ->
            val errorNotFoundInDirection = (1..value)
                .map { moveValue: Int ->
                    coordinate.move(
                        direction = direction,
                        numRows = numRows,
                        numColumns = numColumns,
                        value = moveValue
                    )
                } //Null coordinates are out of bounds of the board and can be ignored
                .all { otherCoordinate: Coordinate? ->
                    actualValues.get(otherCoordinate) != value
                }

            errorNotFoundInDirection
        }
        return errorNotFound
    }

    internal fun boardMeetsRules(actualValues: Map<Coordinate, Int>): Boolean {
        return actualValues.all { (coordinate, value) ->
            val region = boardRegions[getRegionID(coordinate)]!!.filter { it!=coordinate }
            isValidValueRule2(value = value, region = region, actualValues = actualValues) &&
                    isValidValueRule3(value = value, coordinate = coordinate, actualValues = actualValues)
        }
    }

    companion object {
        fun example(): Hakyuu {
            val numColumns = 6
            val numRows = 6
            val sections = intArrayOf(
                0, 1, 2, 3, 4, 5,
                0, 6, 2, 4, 4, 7,
                6, 6, 2, 8, 8, 7,
                9, 6,10, 8, 8, 7,
                9,11,10,12,13,13,
                14,10,10,12,12,12
            )
            val regions = mutableMapOf<Int, MutableList<Coordinate>>()

            sections.forEachIndexed { index, value ->
                if (regions.contains(value)) {
                    regions[value]!!.add(
                        Coordinate.fromIndex(
                            index,
                            numRows = numRows,
                            numColumns = numColumns
                        )
                    )
                } else {
                    regions[value] = mutableListOf(
                        Coordinate.fromIndex(
                            index,
                            numRows = numRows,
                            numColumns = numColumns
                        )
                    )
                }
            }

            return Hakyuu(
                boardRegions = regions,
                numRows = numRows,
                numColumns = numColumns,
                random = Random(1)
            )
        }


        fun create(numColumns: Int, numRows: Int, random: Random): Hakyuu {
            //val regions = Regions(numColumns = numColumns, numRows = numRows, random).divideRegionsOptionB(minNumberOfRegions)
            val regions = Regions(numColumns = numColumns, numRows = numRows, random).divideRegionsOptionA()

            return Hakyuu(
                boardRegions = regions,
                numRows = numRows,
                numColumns = numColumns,
                random = random
            )
        }
    }

}