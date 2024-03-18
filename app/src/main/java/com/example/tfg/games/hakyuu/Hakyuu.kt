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
    override val random: Random,
    override var numIterations: Int = 1
) : GameType {

    @Suppress("UnstableApiUsage")
    fun createNewGame(
        difficulty: Difficulty
    ): Map<Coordinate, Int> {
        val result = mutableMapOf<Coordinate, Int>()

        val possibleValuesPerCoordinate = populatePossibleValues(actualValues = result)
        val success = populateCells(possibleValues = possibleValuesPerCoordinate, actualValues = result, foundSPT = mutableListOf())
/*
        if (!success) {
            // Random regions provided can't make a playable game. We repeat the proccess with other regions.
            boardRegions = Regions(numColumns = numColumns, numRows = numRows, random).divideRegionsOptionA()
            numIterations++
            return createNewGame(difficulty)
        }

 */

        for (row in 0..<numRows) {
            for (col in 0 ..<numColumns) {
                val coordinate = Coordinate(row = row, column = col)
                if (!result.containsKey(coordinate)) {
                    result.put(coordinate,0)
                }
            }
        }

        //println("Result:")
        //printActualValues(result)

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

    internal fun printActualValues(actualValues: Map<Coordinate, Int>) {
        val reset = "\u001b[0m"
        val red = "\u001b[31m"
        val green = "\u001b[32m"
        val yellow = "\u001b[33m"
        val blue = "\u001b[34m"
        val magenta = "\u001b[35m"
        val cyan = "\u001b[36m"
        val white = "\u001b[37m"

        for (row in 0..<numRows) {
            for (col in 0..<numColumns) {
                val coordinate = Coordinate(column = col, row = row)
                val num = actualValues.getOrDefault(coordinate,0)
                val print = if (num==0) " " else "$num"
                print("$print ")
            }
            println()
        }
        println("=========================")
    }

    private fun populateCells(possibleValues: MutableMap<Coordinate, MutableList<Int>>, actualValues: MutableMap<Coordinate, Int>,
                              foundSPT: MutableList<Coordinate>): Boolean {
        while (true)
        {
            // If ended populating return true
            if (boardPopulated(actualValues)) return true

            val noContradiction = populateValues(possibleValues = possibleValues, actualValues = actualValues, foundSPT = foundSPT)

            // Found contradiction -> can't populate
            if (!noContradiction) {
                return false
            }
        }
    }

    // Tries to populate values while there is no contradiction
    // Return if there wasnt a contradiction
    internal fun populateValues(possibleValues: MutableMap<Coordinate, MutableList<Int>>, actualValues: MutableMap<Coordinate, Int>,
                                foundSPT: MutableList<Coordinate>): Boolean {

        var possibleValuesChanged = false
        for (region in boardRegions.values) {
            for (coordinate in region.filter { possibleValues.containsKey(it) }) {
                if (region.contains(Coordinate(5,3))) {
                    val pito = 0
                }
                val values = possibleValues[coordinate]!!
                val valueChanged = values.removeIf { value ->
                    !(isValidValueRule2(value = value,  region = region, actualValues = actualValues) &&
                            isValidValueRule3(value = value, coordinate = coordinate, actualValues = actualValues)
                            )
                }

                possibleValuesChanged = valueChanged || possibleValuesChanged

            }

            val coordsPerValue = getCoordinatesPerValues(region = region, possibleValues = possibleValues, foundSPT = foundSPT)

            val singles = cleanHiddenSingles(coordsPerValue = coordsPerValue, possibleValues = possibleValues)
            val pairs = cleanHiddenPairs(coordsPerValue = coordsPerValue, possibleValues = possibleValues)
            val triples = cleanHiddenTriples(coordsPerValue = coordsPerValue, possibleValues = possibleValues)

            var valueChanged = singles.isNotEmpty() || pairs.isNotEmpty() || triples.isNotEmpty()

            foundSPT.addAll(singles)
            foundSPT.addAll(pairs)
            foundSPT.addAll(triples)


            for ((c1,c2) in detectObviousPairs(region = region, possibleValues = possibleValues, foundSPT = foundSPT)) {
                val values = possibleValues[c1]!!
                region.filter { coordinate -> coordinate!=c1 && coordinate!=c2 && possibleValues.containsKey(coordinate)}
                    .forEach { coordinate ->
                        valueChanged = valueChanged || possibleValues[coordinate]!!.removeAll(values)
                    }
            }

            for ((c1,c2,c3) in detectObviousTriples(region = region, possibleValues = possibleValues, foundSPT = foundSPT)) {
                val values = possibleValues[c1]!!
                region.filter { coordinate -> coordinate!=c1 && coordinate!=c2 && coordinate!=c3 && possibleValues.containsKey(coordinate) }
                    .forEach { coordinate ->
                        valueChanged = valueChanged || possibleValues[coordinate]!!.removeAll(values)
                    }
            }

            possibleValuesChanged = possibleValuesChanged || valueChanged
            // Update values or return if found contradiction
            if (possibleValuesChanged){
                for (coordinate in region.filter { possibleValues.containsKey(it) }) {
                    val values = possibleValues[coordinate]!!
                    if (values.size == 1) {
                        actualValues.put(coordinate, values.first())
                        possibleValues.remove(coordinate)
                    } else if(values.size == 0) {
                        return false
                    }
                }
                if (!boardMeetsRules(actualValues)) {
                    return false
                }
            }
        }
        if (!boardMeetsRules(actualValues)) {
            return false
        }

        if (!possibleValuesChanged){
            //Recursive function to populateCells
            return bruteForceAValue(possibleValues = possibleValues, actualValues = actualValues, foundSPT = foundSPT)
        }

        return true
    }

    private fun bruteForceAValue(possibleValues: MutableMap<Coordinate, MutableList<Int>>, actualValues: MutableMap<Coordinate, Int>,
                                 foundSPT: MutableList<Coordinate>):Boolean {
        val minPossibleValues = possibleValues.minByOrNull { it.value.size }!!

        //Brute force
        for(chosenValue in minPossibleValues.value) {
            possibleValues.remove(minPossibleValues.key)
            val newPossibleValues = mutableMapOf<Coordinate,MutableList<Int>>()
            possibleValues.forEach { (c,v) -> newPossibleValues.put(c,v.toMutableList())}

            val newActualValues = actualValues.toMutableMap()
            newActualValues[minPossibleValues.key] = chosenValue

            val newFoundSPT = foundSPT.toMutableList()

            val res = populateCells(possibleValues = newPossibleValues, actualValues = newActualValues, foundSPT = newFoundSPT)
            if (res) {
                possibleValues.clear()
                possibleValues.putAll(newPossibleValues)
                actualValues.putAll(newActualValues)
                foundSPT.clear()
                foundSPT.addAll(newFoundSPT)
                return true
            }
        }
        // If brute force didn't solve the board this is an invalid state
        return false
    }

    private fun getRegionID(coordinate: Coordinate): Int {
        return boardRegions.filter { it.value.contains(coordinate) }.keys.first()
    }

    private fun getRegion(coordinate: Coordinate): List<Coordinate> {
        return boardRegions[getRegionID(coordinate)]!!
    }

    internal fun getCoordinatesPerValues(region: List<Coordinate>, possibleValues: Map<Coordinate, List<Int>>,
                                         foundSPT: List<Coordinate> = mutableListOf()): Array<MutableList<Coordinate>> {
        val coordsPerValue = Array(region.size) { mutableListOf<Coordinate>() }
        region.filter { coordinate -> !foundSPT.contains(coordinate) && possibleValues.containsKey(coordinate) }.forEach { coordinate ->
            possibleValues[coordinate]!!.forEach { value -> coordsPerValue[value - 1].add(coordinate) }
        }
        return coordsPerValue
    }

    // Delete values to reveal hidden singles
    internal fun cleanHiddenSingles(coordsPerValue: Array<MutableList<Coordinate>>, possibleValues: Map<Coordinate, MutableList<Int>>): List<Coordinate> {
        var res = mutableListOf<Coordinate>()
        coordsPerValue.withIndex().filter { it.value.size == 1 }
            .forEach { (index, value) ->
                // Remove all the possible values but the hidden single
                val coordinate = value.first()
                val coordActualValue = possibleValues[coordinate]!!
                coordActualValue.clear()
                coordActualValue.add(index + 1)

                res.add(coordinate)
            }
        return res
    }

    // Delete values to reveal hidden pairs
    internal fun cleanHiddenPairs(coordsPerValue:  Array<MutableList<Coordinate>>, possibleValues: Map<Coordinate, MutableList<Int>>): List<Coordinate> {
        val filteredPossiblePairs = coordsPerValue.withIndex().filter { it.value.size == 2 }
        var res = mutableListOf<Coordinate>()
        var drop = 0
        filteredPossiblePairs.forEach { (index1, value) ->
            drop++
            val otherPair = filteredPossiblePairs.drop(drop).find { (_, value2) ->
                value2 == value
            }

            if (otherPair != null) { //index1, index2 are pairs
                val index2 = otherPair.index
                // Remove from each coordinate the possible values that are not the hidden pairs
                value.forEach {coordinate ->
                    possibleValues[coordinate]!!.removeIf { it != index1+1 && it != index2+1 }
                    res.add(coordinate)
                }
            }
        }
        return res
    }

    // Delete values to reveal hidden triples
    internal fun cleanHiddenTriples(coordsPerValue:  Array<MutableList<Coordinate>>, possibleValues: Map<Coordinate, MutableList<Int>>): List<Coordinate> {
        val filteredPossibleTriples = coordsPerValue.withIndex().filter { it.value.size == 2 || it.value.size == 3 }
        var res = mutableListOf<Coordinate>()
        var drop = 0
        filteredPossibleTriples.forEach { (index1, value) ->
            drop++
            val union = filteredPossibleTriples.drop(drop).map { (i, v) ->
                IndexedValue(index = i, value = v.union(value))
            }.filter { it.value.size==3 }

            val otherTriples = union.filter { other ->
                union.any { it.value == other.value && it.index != other.index }
            }

            if (otherTriples.size == 2) { //index1, index2 and index3 are triples
                val index2 = otherTriples[0].index
                val index3 = otherTriples[1].index
                // Remove from each coordinate the possible values that are not the hidden triples
                otherTriples[0].value.forEach { coordinate ->
                    possibleValues[coordinate]!!.removeIf { it != index1+1 && it != index2+1 && it != index3+1 }
                    res.add(coordinate)
                }
            }
        }
        if (res.size > 0){
            val pito = 0
        }
        return res
    }

    internal fun detectObviousPairs(region: List<Coordinate>, possibleValues: Map<Coordinate, List<Int>>,
                                    foundSPT: List<Coordinate> = mutableListOf()): List<Pair<Coordinate, Coordinate>> {
        val filteredRegions = region.filter { coordinate ->
            possibleValues[coordinate]?.size == 2 && !foundSPT.contains(coordinate)
        }

        val res = mutableListOf<Pair<Coordinate, Coordinate>>()
        filteredRegions.forEachIndexed { index, coordinate1 ->
            val coordinate2 = filteredRegions.drop(index + 1).find { coord2 -> possibleValues[coord2] == possibleValues[coordinate1] }
            if (coordinate2 != null)
                res.add(Pair(coordinate1, coordinate2))
        }

        return res
    }

    internal fun detectObviousTriples(region: List<Coordinate>, possibleValues: Map<Coordinate, List<Int>>,
                                      foundSPT: List<Coordinate> = mutableListOf()): List<Triple<Coordinate, Coordinate, Coordinate>> {
        val filteredRegions = region.filter { coordinate ->
            (possibleValues[coordinate]?.size == 2 || possibleValues[coordinate]?.size == 3) && !foundSPT.contains(coordinate)
        }

        val res = mutableListOf<Triple<Coordinate, Coordinate, Coordinate>>()

        // Obvious triples can only have size 2 or 3

        filteredRegions.forEachIndexed { index, coord1 ->
            // Substract possible values: {coord2 values} - {coord1 values}
            val union = filteredRegions.drop(index + 1).map { coord2 ->
                Pair(coord2, possibleValues[coord2]!!.union(possibleValues[coord1]!!))
            }
            // Find two coordinates whose substracted possible values are the same
            val otherTriples = union.filter { other ->
                union.any { it.first != other.first && it.second == other.second && (it.second.size == 3 || it.second.size == 2) }
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
            val a = "10:[(0,1)]\n" +
                    "15:[(0,3)]\n" +
                    "14:[(0,5)]\n" +
                    "4:[(4,1)]\n" +
                    "1:[(5,0)]\n" +
                    "9:[(0,0), (1,0)]\n" +
                    "2:[(3,0), (4,0)]\n" +
                    "7:[(4,4), (4,5)]\n" +
                    "11:[(0,2), (1,2), (2,2)]\n" +
                    "12:[(0,4), (1,3), (1,4)]\n" +
                    "13:[(1,5), (2,5), (3,5)]\n" +
                    "3:[(1,1), (2,0), (2,1), (3,1)]\n" +
                    "8:[(2,3), (2,4), (3,3), (3,4)]\n" +
                    "5:[(3,2), (4,2), (5,1), (5,2)]\n" +
                    "6:[(4,3), (5,3), (5,4), (5,5)]"

            val b = "6:[(3,2)]\n" +
                    "4:[(3,4)]\n" +
                    "5:[(4,3)]\n" +
                    "2:[(4,2), (5,2)]\n" +
                    "12:[(0,0), (1,0), (2,0)]\n" +
                    "13:[(0,1), (0,2), (1,1)]\n" +
                    "8:[(0,5), (1,4), (1,5)]\n" +
                    "7:[(2,3), (2,4), (3,3)]\n" +
                    "1:[(5,3), (5,4), (5,5)]\n" +
                    "9:[(0,3), (0,4), (1,2), (1,3)]\n" +
                    "10:[(2,1), (2,2), (3,0), (3,1)]\n" +
                    "3:[(2,5), (3,5), (4,4), (4,5)]\n" +
                    "11:[(4,0), (4,1), (5,0), (5,1)]"

            val c = "13:[(0,0)]\n" +
                    "12:[(2,1)]\n" +
                    "1:[(5,0)]\n" +
                    "6:[(5,3)]\n" +
                    "9:[(0,1), (0,2), (0,3)]\n" +
                    "10:[(0,4), (1,4), (2,4)]\n" +
                    "11:[(0,5), (1,5), (2,5)]\n" +
                    "7:[(2,2), (3,2), (3,3)]\n" +
                    "2:[(1,0), (2,0), (3,0), (4,0)]\n" +
                    "8:[(1,1), (1,2), (1,3), (2,3)]\n" +
                    "3:[(3,1), (4,1), (5,1), (5,2)]\n" +
                    "4:[(3,4), (4,2), (4,3), (4,4)]\n" +
                    "5:[(3,5), (4,5), (5,4), (5,5)]"

            return Hakyuu(
                boardRegions = Regions.parseString(c),
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