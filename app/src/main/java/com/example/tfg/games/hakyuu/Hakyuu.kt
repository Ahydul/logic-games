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
    override val boardRegions: Map<Int, List<Coordinate>>,

    override val numColumns: Int,
    override val numRows: Int
) : GameType {

    @Suppress("UnstableApiUsage")
    override fun createNewGame(
        difficulty: Difficulty,
        random: Random
    ): Map<Coordinate, Int> {
        val result = mutableMapOf<Coordinate, Int>()
        val resultTmp = mutableMapOf<Coordinate, Int>()

        //Regions ordered by the size of its associated list
        val orderedRegions = boardRegions.toList().sortedBy { it.second.size }.toMap()

        // Populate coordinates
        for (region in orderedRegions) {
            val possibleValues = (1 .. region.value.size).shuffled(random).toMutableList()
            for (coordinate in region.value) {
                var value = possibleValues.find { possibleValue: Int ->
                    isInvalidValueRule3(
                        value = possibleValue,
                        coordinate = coordinate,
                        actualValues = resultTmp
                    )
                }
                possibleValues.remove(value)
                resultTmp.put(coordinate, value ?: 0)
            }
        }

        result.putAll(resultTmp)
        return result
    }

    @Suppress("UnstableApiUsage")
    fun createNewGame2(
        difficulty: Difficulty,
        random: Random
    ): Map<Coordinate, Int> {
        val result = mutableMapOf<Coordinate, Int>()

        val orderedRegions = boardRegions.toList().sortedBy { it.second.size }.toMap()
        val possibleValuesPerCoordinate = mutableMapOf<Coordinate, MutableList<Int>>()
        val coordinateRegionIDs = mutableMapOf<Coordinate, Int>()

        // Populate possible values
        for (region in orderedRegions) {
            val values = (1 .. region.value.size).toMutableList()
            for (coordinate in region.value) {
                possibleValuesPerCoordinate.put(coordinate, values)
                coordinateRegionIDs.put(coordinate, region.key)
            }
        }


        for (entry in possibleValuesPerCoordinate)
            if (entry.value.size==1) {
                result.put(entry.key, entry.value[0])
                possibleValuesPerCoordinate.remove(entry.key)
            }

        // Update the possible values and loop while it returns true (it updated some values)
        while (
            updatePossibleValues(
                possibleValues = possibleValuesPerCoordinate,
                coordinateRegionIDs = coordinateRegionIDs,
                actualValues = result
            )
        )
        {
            // Update values that logically only have one possibility
            // Check if there is a contradiction (no possible value in a cell)
            for (entry in possibleValuesPerCoordinate)
                when(entry.value.size) {
                    0 -> {
                        //TODO manage error
                    }
                    1 -> {
                        result.put(entry.key, entry.value[0])
                        possibleValuesPerCoordinate.remove(entry.key)
                    }
                    else -> {}
                }
        }

        val resultTmp = mutableMapOf<Coordinate, Int>()
        resultTmp.putAll(result)

        result.putAll(resultTmp)
        return result
    }

    //Update possible values. Return true if the update changed the possible values.
    internal fun updatePossibleValues(possibleValues: Map<Coordinate, MutableList<Int>>, coordinateRegionIDs:  Map<Coordinate, Int>, actualValues: Map<Coordinate, Int>): Boolean {
        var possibleValuesChanged = false
        for (entry in possibleValues) {
            //Remove values that are invalid
            val coordinate = entry.key
            val values = entry.value
            val region = boardRegions[coordinateRegionIDs[coordinate]]!!
            for (value in values){
                if (isInvalidValueRule2(value = value,  region = region, actualValues = actualValues) ||
                        isInvalidValueRule3(value = value, coordinate = coordinate, actualValues = actualValues)) {
                    values.remove(value)
                    possibleValuesChanged = true
                }
            }
        }

        // Detect obvious pairs and triples
        for (region in boardRegions.values) {

        }

        return possibleValuesChanged
    }

    internal fun detectObviousPairs(region: List<Coordinate>, possibleValues: Map<Coordinate, List<Int>>): List<Pair<Coordinate, Coordinate>> {
        if (region.size < 3) return emptyList()

        val res = mutableListOf<Pair<Coordinate, Coordinate>>()
        val filteredRegions = region.filter { possibleValues[it]!!.size == 2 }
        filteredRegions.forEachIndexed { index, coord1 ->
            val coord2 = filteredRegions.drop(index + 1).find { coord2 -> possibleValues[coord2] == possibleValues[coord1] }
            if (coord2 != null)
                res.add(Pair(coord1, coord2))
        }

        return res
    }

    internal fun detectObviousTriples(region: List<Coordinate>, possibleValues: Map<Coordinate, List<Int>>): List<Triple<Coordinate, Coordinate, Coordinate>> {
        require(!region.any { !possibleValues.containsKey(it) })
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

    // In every area of N fields, every number from the range 1~N must appear exactly once.
    private fun isInvalidValueRule2(value: Int, region: List<Coordinate>, actualValues: Map<Coordinate, Int>): Boolean {
        for (coordinate in region) {
            if (actualValues.containsKey(coordinate) && actualValues[coordinate] == value)
                return true
        }
        return false
    }

    // If two fields in a row or column contain the same number Z, there must be at least Z fields with different numbers between these two fields.
    private fun isInvalidValueRule3(value: Int, coordinate: Coordinate, actualValues: Map<Coordinate, Int>): Boolean {
        val errorNotFound = Direction.values().all { direction: Direction ->
            val errorNotFoundInDirection = (1..value)
                //Null coordinates are out of bounds of the board and can be ignored
                .mapNotNull { moveValue: Int ->
                    coordinate.move(
                        direction = direction,
                        numRows = numRows,
                        numColumns = numColumns,
                        value = moveValue
                    )
                }
                .all { otherCoordinate: Coordinate ->
                    actualValues.get(otherCoordinate) != value
                }

            errorNotFoundInDirection
        }
        return errorNotFound
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
                numColumns = numColumns
            )
        }


        fun create(numColumns: Int, numRows: Int, random: Random, minNumberOfRegions: Int): Hakyuu {
            //val regions = Regions(numColumns = numColumns, numRows = numRows, random).divideRegionsOptionB(minNumberOfRegions)
            val regions = Regions(numColumns = numColumns, numRows = numRows, random).divideRegionsOptionA()

            return Hakyuu(
                boardRegions = regions,
                numRows = numRows,
                numColumns = numColumns
            )
        }
    }

}