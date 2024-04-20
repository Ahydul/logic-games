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

    private val reset = "\u001B[0m"
    private val colors = arrayOf(
        "\u001B[30m",  // Black
        "\u001B[31m",  // Red
        "\u001B[32m",  // Green
        "\u001B[33m",  // Yellow
        "\u001B[34m",  // Blue
        "\u001B[35m",  // Purple
        "\u001B[36m",  // Cyan
        "\u001B[37m",  // White
        "\u001B[38m",  //
        "\u001B[39m",  //
        "\u001B[40m",  //
        "\u001B[41m",  //
        "\u001B[42m",  //
        "\u001B[43m",  //
        "\u001B[44m",  //
        "\u001B[45m",  //
        "\u001B[90m",  // Bright Black
        "\u001B[91m",  // Bright Red
        "\u001B[92m",  // Bright Green
        "\u001B[93m",  // Bright Yellow
        "\u001B[94m",  // Bright Blue
        "\u001B[95m",  // Bright Purple
        "\u001B[96m",  // Bright Cyan
        "\u001B[97m"   // Bright White
    )

    fun printBoard() {
        (0..<numPositions).forEach {
            val num = board[it].first
            val id = board[it].second
            val print = if (num==0) "  " else if (num < 10) " $num" else "$num"

            print("${colors[id % colors.size]}$print ${reset}")

            if (it%numColumns == numColumns-1){
                println()
            }
        }
        println("=================================")
    }

    private fun randomPropagationNumber(): Int {
        return random.nextInt(maxRegionSize - 1) + 1
        return ((maxRegionSize - 1) * Curves.easierInOutSine(random.nextDouble(1.0))).toInt() + 1
    }

    fun createGame() {
        while (!boardCreated()) {
            propagateRandomRegion()
            //printBoard()
        }
        printBoard()
        println(board.map { it.second }.groupBy { it }.map { it.value.size }.sorted())
    }

    fun propagateRandomRegion(numPropagations: Int = randomPropagationNumber(), iterations: Int = 1) {
        val seed = getRandomPosition()
        val region = mutableListOf(seed)

        for (index in 1..numPropagations) {
            propagateOnce(region)
        }

        val result = populateRegion(region)

        if (!result) {
            if (iterations > 20) {
                modifyNeighbouringRegions(seed)
            }
            propagateRandomRegion(numPropagations = numPropagations, iterations = iterations+1)
        }
        else{
            //println("$iterations iterations")
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
        val position = Direction.entries.mapNotNull { direction: Direction ->
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

