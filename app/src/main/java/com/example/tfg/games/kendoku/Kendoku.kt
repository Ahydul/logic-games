package com.example.tfg.games.kendoku

import com.example.tfg.common.enums.Direction
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.GameType
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.PopulateResult
import com.example.tfg.games.common.Score

class Kendoku(
    size: Int,
    seed: Long,
    score: KendokuScore = KendokuScore(),
    completedBoard: IntArray = IntArray(size * size),
    startBoard: IntArray = IntArray(size * size),
    regions: IntArray = IntArray(size * size),
    private var operationPerRegion: Map<Int, KendokuOperation> = mutableMapOf(),
    private val allowedOperations: Array<KendokuOperation> = KendokuOperation.entries
        .filterNot { it == KendokuOperation.ANY }
        .toTypedArray(),
    private var printEachBoardState: Boolean = false
): GameType(
    type = Games.HAKYUU,
    numColumns = size,
    numRows = size,
    seed = seed,
    score = score,
    completedBoard = completedBoard,
    startBoard = startBoard,
    boardRegions = regions
) {
    // Helper variables
    private var currentID = 0
    private val operationResultPerRegion: MutableMap<Int, Int> = mutableMapOf()

    // Use this instead of numColumns or numRows
    private val size = numColumns

    override fun maxRegionSize(): Int = size

    override fun createCompleteBoard(remainingPositions: MutableSet<Int>) {
        // Create regions

        while (remainingPositions.isNotEmpty()) {
            propagateRandomEmptyRegion(remainingPositions)
        }

        // Populate cells

        latinSquare(size).forEachIndexed { row, values ->
            values.forEachIndexed { column, value ->
                completedBoard[row*size + column] = value
            }
        }

        // Create operations

        operationPerRegion = boardRegions.groupBy { it }.map { (regionID, values) ->
            val operation = if (values.size == 1) KendokuOperation.ANY
                else allowedOperations.filterNot {
                        // Subtractions can't have more than 2 operands
                    (it == KendokuOperation.SUBTRACT && values.size != 2) ||
                        // Divisions can't have more than 2 operands and must result in integers
                    (it == KendokuOperation.DIVIDE && (values.size != 2 || (values.max() % values.min() != 0))) ||
                        // Multiplications can't be result in numbers higher than 1000
                    (it == KendokuOperation.MULTIPLY && (values.reduce { acc, num -> acc * num } > 1000))
                }.random(random)

            operationResultPerRegion[regionID] = operation.operate(values)

            regionID to operation
        }.toMap()

        println(operationPerRegion)
    }

    private fun propagateRandomEmptyRegion(
        remainingPositions: MutableSet<Int>,
        numPropagations: Int = randomPropagationNumber()
    ) {
        currentID++

        val seed = remainingPositions.random(random)
        boardRegions[seed] = currentID
        remainingPositions.remove(seed)

        val region = mutableListOf(seed)

        for (index in 1..numPropagations) {
            propagateOnce(remainingPositions = remainingPositions, region = region)
        }

        if (printEachBoardState) {
            print(printBoardHTML(completedBoard, boardRegions, true))
        }
    }

    private fun randomPropagationNumber(): Int {
        return ((maxRegionSize() - 1) * Curves.lowerValues(random.nextDouble(1.0))).toInt() + 1
    }

    private fun propagateOnce(remainingPositions: MutableSet<Int>, region: MutableList<Int>) {
        if (region.size == maxRegionSize()) return

        for (position in region.shuffled(random)) {
            for (direction in Direction.entries.shuffled(random)) {
                val propagation = Coordinate.move(direction = direction, position = position, numColumns=size, numRows = size)

                if (propagation != null && remainingPositions.contains(propagation)){
                    region.add(propagation)
                    boardRegions[propagation] = currentID
                    remainingPositions.remove(propagation)
                    return
                }
            }
        }
    }

    private fun latinSquare(size: Int): Array<IntArray> {
        val latinSquare = Array(size) { IntArray(size) }

        for (i in 0 until size) {
            for (j in 0 until size) {
                latinSquare[i][j] = (i + j) % size + 1
            }
        }

        latinSquare.shuffle(random)

        val transposed = Array(size) { IntArray(size) }
        for (i in 0 until size) {
            for (j in 0 until size) {
                transposed[i][j] = latinSquare[j][i]
            }
        }
        transposed.shuffle(random)
        for (i in 0 until size) {
            for (j in 0 until size) {
                latinSquare[i][j] = transposed[j][i]
            }
        }

        val perm = (1..size).shuffled(random)
        for (i in 0 until size) {
            for (j in 0 until size) {
                latinSquare[i][j] = perm[latinSquare[i][j] - 1]
            }
        }

        return latinSquare
    }

    override fun fillPossibleValues(possibleValues: Array<MutableList<Int>>, board: IntArray): Score {
        val scoreResult = KendokuScore()
        for (position in (0..<numPositions())) {
            val regionId = getRegionId(position)
            if (regionIsOneCell(regionId, position) && board[position] == 0) {
                board[position] = operationResultPerRegion[regionId]!!
                scoreResult.addScoreNewValue()
            }
            else if (board[position] == 0) possibleValues[position].addAll(1.. size)
        }
        return scoreResult
    }

    override fun populateValues(
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray
    ): PopulateResult {
        TODO("Not yet implemented")
    }

    override fun boardMeetsRulesStr(board: IntArray): String {
        TODO("Not yet implemented")
    }

    override fun checkValue(position: Int, value: Int, actualValues: IntArray): Set<Int> {
        TODO("Not yet implemented")
    }


    companion object {
        fun create(size: Int, seed: Long, difficulty: Difficulty, printEachBoardState: Boolean = false): Kendoku {
            val kendoku = Kendoku(
                size = size,
                seed = seed,
                printEachBoardState = printEachBoardState
            )

            kendoku.createGame(difficulty)

            return kendoku
        }
    }
}