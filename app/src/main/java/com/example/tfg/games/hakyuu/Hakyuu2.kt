package com.example.tfg.games.hakyuu

import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.Direction
import com.example.tfg.games.Games
import kotlin.math.min
import kotlin.random.Random

class Hakyuu2 private constructor(
    val type: Games = Games.HAKYUU,
    val noNotes: Boolean = true,
    val numColumns: Int,
    val numRows: Int,
    val random: Random,
    var iterations: Int = 1
) {
                        // pair(value, regionID)
    private val numPositions = numColumns * numRows
    private val board: Array<Pair<Int, Int>> = initBoard()
    private var remainingPositions: MutableSet<Int> = (0..< numPositions).toMutableSet()
    private var currentID = 0
    private val maxRegionSize = min(numColumns, numRows)

    private fun initBoard(): Array<Pair<Int, Int>> {
        val res = Array(size = numPositions, init = { Pair(0, 0) })
        res.shuffle(random)
        return res
    }

    private fun getRandomPosition(): Int {
        return remainingPositions.random(random)
    }

    private fun boardCreated(): Boolean {
        return remainingPositions.isEmpty()
    }

    private val htmlColors = arrayOf(
        "#FF0000", // Red
        "#00FF00", // Green
        "#0000FF", // Blue
        "#FFFF00", // Yellow
        "#FF00FF", // Magenta
        "#00FFFF", // Cyan
        "#800000", // Maroon
        "#008000", // Olive
        "#000080", // Navy
        "#808000", // Teal
        "#800080", // Purple
        "#008080", // Gray
        "#C0C0C0", // Silver
        "#FFA500", // Orange
        "#FFC0CB", // Pink
        "#800000", // Brown
        "#808080", // Dark Gray
        "#A52A2A", // Brown
        "#00FF7F", // Spring Green
        "#ADFF2F", // Green Yellow
        "#7FFF00", // Chartreuse
        "#32CD32", // Lime Green
        "#8B008B", // Dark Magenta
        "#FF69B4", // Hot Pink
        "#4B0082", // Indigo
        "#800080", // Purple
        "#FF6347", // Tomato
        "#FF4500", // Orange Red
        "#FFD700", // Gold
        "#DAA520", // Goldenrod
        "#FF8C00", // Dark Orange
        "#DC143C", // Crimson
        "#FF1493", // Deep Pink
        "#00BFFF", // Deep Sky Blue
        "#87CEEB", // Sky Blue
        "#4682B4", // Steel Blue
        "#6A5ACD", // Slate Blue
        "#7B68EE", // Medium Slate Blue
        "#9370DB", // Medium Purple
        "#8A2BE2", // Blue Violet
        "#9932CC", // Dark Orchid
        "#8B008B", // Dark Magenta
        "#BA55D3", // Medium Orchid
        "#9400D3", // Dark Violet
        "#800080", // Purple
        "#663399", // Rebecca Purple
        "#4B0082", // Indigo
        "#9370DB", // Medium Purple
        "#800080", // Purple
        "#8A2BE2", // Blue Violet
    )

    fun printBoard() {
        val tmpColors = htmlColors.toMutableSet()
        val colorMap = mutableMapOf<Int,String>()

        var htmlCode = """<table style="font-size: large; border-collapse: collapse; margin: 20px auto;"><tbody>"""

        (0..<numPositions).forEach {
            val num = board[it].first
            val id = board[it].second
            //val print = if (num==0) "   " else if (num < 10) " $num " else "$num "

            if (!colorMap.containsKey(id)) {
                val color = tmpColors.first()
                tmpColors.remove(color)
                colorMap[id] = color
            }

            if (it%numColumns == 0){
                htmlCode += """<tr>"""
            }

            htmlCode += """<td style="background-color: ${colorMap[id]}; vertical-align: middle; text-align: center; height: 40px; width: 40px;">$num</td>"""

            if (it%numColumns == numColumns-1){
                htmlCode += """</tr>"""
            }

        }
        htmlCode +="""</tbody></table>"""
        print(htmlCode)
    }

    fun getRegionStatData(): IntArray {
        val ls = board.map { it.second }.groupBy { it }.map { it.value.size }
        val result = IntArray(size = ls.max())
        ls.forEach {
            result[it-1]++
        }
        return result
    }

    private fun randomPropagationNumber(): Int {
        //return random.nextInt(maxRegionSize - 1) + 1
        return ((maxRegionSize - 1) * Curves.easierInOutSine(random.nextDouble(1.0))).toInt() + 1
    }

    fun createGame() {
        while (!boardCreated()) {
            propagateRandomRegion()
        }
    }

    fun propagateRandomRegion(numPropagations: Int = randomPropagationNumber(), iterations: Int = 1) {
        val seed = getRandomPosition()
        val region = mutableListOf(seed)

        for (index in 1..numPropagations) {
            propagateOnce(region)
        }

        val result = populateRegion(region)

        if (!result) {
            if (iterations > 20)
                modifyNeighbouringRegions(seed)

            propagateRandomRegion(numPropagations = numPropagations, iterations = iterations+1)
        }
        else {
            this.iterations += iterations
        }
    }

    private fun deleteRegion(regionId: Int) {
        board.forEachIndexed { index, pair ->
            if (pair.second == regionId) {
                board[index] = Pair(0, 0)
                remainingPositions.add(index)
            }
        }
    }

    private fun modifyNeighbouringRegions(seed: Int) {
        val position = Direction.entries.shuffled(random).mapNotNull { direction: Direction ->
            Coordinate.move(
                direction = direction,
                position = seed,
                numRows = numRows,
                numColumns = numColumns
            )
        }.find { board[it].second > 0 }

        if (position != null)
            deleteRegion(board[position].second)

    }

    private fun populateRegion(region: List<Int>): Boolean {
        val values = Array(region.size) { emptyList<Int>() }
        val positions = Array(region.size) { -1 }
        region.map { position ->
                position to (1..region.size)
                    .filter { value -> isValidValueRule3(value = value, position = position) }
                    .toList()
            }
            .sortedBy { it.second.size }
            .forEachIndexed { index, pair ->
                values[index] = pair.second
                positions[index] = pair.first
            }

        val res = tryPopulateRegion(possibleValuesPerPosition = values, index = 0, result = Array(region.size) { -1 })

        return if (res == null){
            false
        } else{
            finalizeRegion(positions = positions, values = res)
            true
        }
    }

    private fun finalizeRegion(positions: Array<Int>, values: Array<Int>) {
        currentID++
        for (p in positions.withIndex()) {
            board[p.value] = Pair(values[p.index], currentID)
            remainingPositions.remove(p.value)
        }
    }

    private fun tryPopulateRegion(possibleValuesPerPosition: Array<List<Int>>, index: Int, result: Array<Int>): Array<Int>? {
        //Return condition
        if (possibleValuesPerPosition.isEmpty()) return result

        //Check first position
        val possibleValues = possibleValuesPerPosition.first()
        if (possibleValues.isEmpty()) return null //backtrack

        for (value in possibleValues.shuffled(random)) {
            result[index] = value
            val newPossibleValues = Array(possibleValuesPerPosition.size - 1) { it ->
                possibleValuesPerPosition[it+1].filter { it != value }
            }
            val res = tryPopulateRegion(newPossibleValues, index + 1, result)
            if (res != null) return res
        }

        // Populate failed
        return null
    }

    // If two fields in a row or column contain the same number Z, there must be at least Z fields with different numbers between these two fields.
    private fun isValidValueRule3(value: Int, position: Int): Boolean {
        val errorNotFound = Direction.entries.all { direction: Direction ->
            val errorNotFoundInDirection = (1..value)
                .mapNotNull { moveValue: Int ->
                    Coordinate.move(
                        direction = direction,
                        position = position,
                        numRows = numRows,
                        numColumns = numColumns,
                        value = moveValue
                    )
                } //Null values are out of bounds of the board and can be ignored
                .all { otherPosition: Int ->
                    board[otherPosition].first != value
                }

            errorNotFoundInDirection
        }
        return errorNotFound
    }

    internal fun boardMeetsRules(): Boolean {
        val tmp = mutableMapOf<Int, MutableSet<Int>>()
        board.withIndex().forEach { (position, pair) ->
            val (value, regionID) = pair
            if (!isValidValueRule3(value = value, position = position)) {
                println("Rule 3 failed: with value:$value, position:$position")
                return false
            }
            if (tmp.containsKey(regionID)) {
                val elementAdded = tmp[regionID]!!.add(value)
                if (!elementAdded) {
                    println("Rule 2 failed: with region:${tmp[regionID]}")
                    return false
                }
            }
            else{
                tmp[regionID] = mutableSetOf(value)
            }
        }
        return true
    }

    private fun propagateOnce(region: MutableList<Int>) {
        if (region.size == maxRegionSize) return

        for (position in region.shuffled(random)) {
            for (direction in Direction.entries.shuffled(random)) {
                val result = tryPropagate(
                    propagation = Coordinate.move(direction = direction, position = position, numColumns=numColumns, numRows = numRows),
                    region = region
                )
                if (result) return
            }
        }

    }

    private fun tryPropagate(propagation: Int?, region: MutableList<Int>): Boolean {
        if (propagation != null && remainingPositions.contains(propagation) && !region.contains(propagation)){
            region.add(propagation)
            //remainingPositions.remove(propagation)
            return true
        }
        return false
    }

    companion object {
        fun create(numColumns: Int, numRows: Int, random: Random): Hakyuu2 {
            return Hakyuu2(
                numRows = numRows,
                numColumns = numColumns,
                random = random
            )
        }
    }


}

