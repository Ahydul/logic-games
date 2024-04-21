package com.example.tfg.games.hakyuu

import com.example.tfg.common.utils.Colors
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
    var iterations: Int = 1,
    var numBoardReset: Int = 0
) {
                        // pair(value, regionID)
    private val numPositions = numColumns * numRows
    private val board: Array<Pair<Int, Int>> = initBoard()
    private val remainingPositions: MutableSet<Int> = initRemainingPositions()
    private var currentID = 0
    private val maxRegionSize = min(numColumns, numRows)
    private val colors = Colors()
    private var startTime: Long = 0
    private var msBeforeSkipBoard: Long = 0

    private fun initBoard(): Array<Pair<Int, Int>> {
        return Array(size = numPositions, init = { Pair(0, 0) })
    }

    private fun initRemainingPositions():  MutableSet<Int> {
        return (0..< numPositions).toMutableSet()
    }

    private fun reset() {
        board.indices.forEach { board[it] = Pair(0,0) }
        remainingPositions.addAll(initRemainingPositions())
        numBoardReset++
    }

    fun createGame(msBeforeSkipBoard: Long = 0) {
        startTime = System.currentTimeMillis()
        this.msBeforeSkipBoard = msBeforeSkipBoard
        while (!boardCreated()) {
            propagateRandomRegion()
        }
    }

    private fun boardCreated(): Boolean {
        return remainingPositions.isEmpty()
    }

    fun printBoard() {
        val colorMap = mutableMapOf<Int,String>()

        var htmlCode = """<table style="font-size: large; border-collapse: collapse; margin: 20px auto;"><tbody>"""

        (0..<numPositions).forEach {
            val num = board[it].first
            val id = board[it].second

            if (!colorMap.containsKey(id)) {
                val color = colors.newColor()
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

    private fun propagateRandomRegion(numPropagations: Int = randomPropagationNumber(), iterations: Int = 1) {
        val seed = getRandomPosition()
        val region = mutableListOf(seed)

        for (index in 1..numPropagations) {
            propagateOnce(region)
        }

        val gameCreationReset = maybeResetCreateGame()
        if (gameCreationReset) return

        val result = populateRegion(region = region)

        if (!result) {
            if (iterations > 20) {
                modifyNeighbouringRegions(seed)
            }
            propagateRandomRegion(
                numPropagations = numPropagations,
                iterations = iterations+1
            )
        }
        else {
            this.iterations += iterations
        }
    }

    private fun maybeResetCreateGame(): Boolean {
        if (msBeforeSkipBoard <= 0) return false

        val actualTime = System.currentTimeMillis()
        if ((actualTime - startTime) > msBeforeSkipBoard){
            reset()
            createGame(msBeforeSkipBoard)
            return true
        }
        return false
    }

    private fun getRandomPosition(): Int {
        return remainingPositions.random(random)
    }

    private fun randomPropagationNumber(): Int {
        //return random.nextInt(maxRegionSize - 1) + 1
        return ((maxRegionSize - 1) * Curves.easierInOutSine(random.nextDouble(1.0))).toInt() + 1
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
            // If board was created here its because of a reset
            return boardCreated()
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

        val gameCreationReset = maybeResetCreateGame()
        if (gameCreationReset) return null

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

